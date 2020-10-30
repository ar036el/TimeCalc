package com.arealapps.timecalculator.utils.tutoriaShowcase.data

import com.arealapps.timecalculator.helpers.native_.errorIf

object Script {

    fun createScriptInstance(): ScriptInstance {
        return ScriptInstanceImpl()
    }

    enum class Frames {
        //wfe - waiting for event

        _0,
        _1a, _1b, _1c,
        _2a1 /*wfe*/, _2a2,
        _2b1 /*wfe*/, _2b2,
        _2c1 /*wfe*/, _2c2,

        /*wfe: time block appears*/
        _3a,
        _3b1 /*wfe*/, _3b2,
        _3c1 /*wfe*/, _3c2,

        _4a, _4b,
        _5
    }
    val firstFrame = Frames._0
    val lastFrame = Frames._5

    enum class Events {
        CalculatedNumberResult,
        CalculatedTimeResult,
        CalculatedMixedResult,
        TimeBlockAppeared,
        CollapsedTimeBlock,
        RevealedTimeBlock
    }

    sealed class FrameActions {
        class GoToNextFrame(val frame: Frames): FrameActions()
        object WaitForEvent : FrameActions()
        object FinishShowcase: FrameActions()
    }

    fun getFrameNextAction(frame: Frames, scriptInstance: ScriptInstance): FrameActions {
        return when (frame) {
            Frames._0 -> FrameActions.GoToNextFrame(Frames._1a)
            Frames._1a -> FrameActions.GoToNextFrame(Frames._1b)
            Frames._1b -> FrameActions.GoToNextFrame(Frames._1c)
            Frames._1c -> FrameActions.GoToNextFrame(Frames._2a1)

            Frames._2a1 -> FrameActions.WaitForEvent
            Frames._2a2 -> FrameActions.GoToNextFrame(Frames._2b1)
            Frames._2b1 -> FrameActions.WaitForEvent
            Frames._2b2 -> {
                if (scriptInstance.getStateFor(Events.TimeBlockAppeared) == ScriptInstance.EventStates.New) {
                    FrameActions.WaitForEvent
                } else {
                    FrameActions.GoToNextFrame(Frames._2c1)
                }
            }
            Frames._2c1 -> FrameActions.WaitForEvent
            Frames._2c2 -> {
                if (scriptInstance.getStateFor(Events.TimeBlockAppeared) == ScriptInstance.EventStates.New) {
                    FrameActions.WaitForEvent
                } else {
                    FrameActions.GoToNextFrame(Frames._4a)
                }
            }

            Frames._3a -> FrameActions.GoToNextFrame(Frames._3b1)
            Frames._3b1 -> FrameActions.WaitForEvent
            Frames._3b2 -> FrameActions.GoToNextFrame(Frames._3c1)
            Frames._3c1 -> FrameActions.WaitForEvent
            Frames._3c2 -> {
                if (scriptInstance.getStateFor(Events.CalculatedNumberResult) == ScriptInstance.EventStates.New
                    || scriptInstance.getStateFor(Events.CalculatedTimeResult) == ScriptInstance.EventStates.New
                    || scriptInstance.getStateFor(Events.CalculatedMixedResult) == ScriptInstance.EventStates.New) {
                    FrameActions.WaitForEvent
                } else if (scriptInstance.getStateFor(Events.CalculatedNumberResult) == ScriptInstance.EventStates.NotIntroduced) {
                    FrameActions.GoToNextFrame(Frames._2a1)
                } else if (scriptInstance.getStateFor(Events.CalculatedTimeResult) == ScriptInstance.EventStates.NotIntroduced) {
                    FrameActions.GoToNextFrame(Frames._2b1)
                } else if (scriptInstance.getStateFor(Events.CalculatedMixedResult) == ScriptInstance.EventStates.NotIntroduced) {
                    FrameActions.GoToNextFrame(Frames._2c1)
                } else {
                    FrameActions.GoToNextFrame(Frames._4a)
                }
            }

            Frames._4a -> FrameActions.GoToNextFrame(Frames._4b)
            Frames._4b -> FrameActions.GoToNextFrame(Frames._5)
            Frames._5 -> FrameActions.FinishShowcase
        }
    }

    fun getNextFrameWithNeededActionsWhenWaitingForEventsIfMatch(scriptInstance: ScriptInstance): Pair<Frames, (ScriptInstance) -> Unit>? {
        val newEvents = scriptInstance.getAllEventsFor(ScriptInstance.EventStates.New)
        val neededActionsBefore: (ScriptInstance) -> Unit
        val nextFrame: Frames
        when {

            newEvents.contains(Events.CalculatedMixedResult) -> {
                neededActionsBefore = {
                    it.setEvents(ScriptInstance.EventStates.Consumed, Events.CalculatedNumberResult, Events.CalculatedTimeResult, Events.CalculatedMixedResult)
                }
                nextFrame = Frames._2c2
            }
            newEvents.contains(Events.CalculatedTimeResult) -> {
                neededActionsBefore = {
                    it.setEvents(ScriptInstance.EventStates.Consumed, Events.CalculatedNumberResult, Events.CalculatedTimeResult)
                }
                nextFrame = Frames._2b2
            }
            newEvents.contains(Events.CalculatedNumberResult) -> {
                neededActionsBefore = {
                    it.setEvents(ScriptInstance.EventStates.Consumed, Events.CalculatedNumberResult)
                }
                nextFrame = Frames._2a2
            }


            newEvents.contains(Events.TimeBlockAppeared) -> {
                neededActionsBefore = {
                    it.setEvents(ScriptInstance.EventStates.NotIntroduced, Events.CollapsedTimeBlock, Events.RevealedTimeBlock)
                    it.setEvents(ScriptInstance.EventStates.Consumed, Events.TimeBlockAppeared)
                }
                nextFrame = Frames._3a
            }
            newEvents.contains(Events.CollapsedTimeBlock) -> {
                neededActionsBefore = {
                    errorIf{ it.getStateFor(Events.TimeBlockAppeared) != ScriptInstance.EventStates.Consumed }
                    it.setEvents(ScriptInstance.EventStates.Consumed, Events.CollapsedTimeBlock)
                }
                nextFrame = Frames._3b2
            }
            newEvents.contains(Events.RevealedTimeBlock) -> {
                neededActionsBefore = {
                    errorIf{ it.getStateFor(Events.TimeBlockAppeared) != ScriptInstance.EventStates.Consumed }
                    it.setEvents(ScriptInstance.EventStates.Consumed, Events.RevealedTimeBlock)
                }
                nextFrame = Frames._3c2
            }


            else -> {
                return null
            }
        }
        return Pair(nextFrame, neededActionsBefore)
    }

}

interface ScriptInstance {
    fun setEvents(state: EventStates, vararg events: Script.Events)
    fun setFrames(state: FrameState, vararg frames: Script.Frames)
    fun getAllEventsFor(state: EventStates): Set<Script.Events>
    fun getAllFramesFor(state: FrameState): Set<Script.Frames>
    fun getStateFor(event: Script.Events): EventStates
    fun getStateFor(frame: Script.Frames): FrameState

    val currentFrame: Script.Frames

    enum class EventStates { NotIntroduced, New, Consumed }
    enum class FrameState { NotIntroduced, Active, Finished }
}

class ScriptInstanceImpl : ScriptInstance {

    private val eventsWithStates = Script.Events.values().map { Pair(it,
        ScriptInstance.EventStates.NotIntroduced) }.toMap().toMutableMap()
    private val framesWithStates = Script.Frames.values().map { Pair(it,
        ScriptInstance.FrameState.NotIntroduced) }.toMap().toMutableMap()

    override val currentFrame: Script.Frames
        get() {
            val activeFrames = getAllFramesFor(ScriptInstance.FrameState.Active)
            errorIf { activeFrames.size != 1 }
            return activeFrames.first()
        }

    override fun setEvents(state: ScriptInstance.EventStates, vararg events: Script.Events) {
        events.forEach { eventsWithStates[it] = state }
    }
    override fun setFrames(state: ScriptInstance.FrameState, vararg frames: Script.Frames) {
        frames.forEach { framesWithStates[it] = state }
    }
    override fun getAllEventsFor(state: ScriptInstance.EventStates): Set<Script.Events> {
        return eventsWithStates.filter { it.value == state }.keys
    }
    override fun getAllFramesFor(state: ScriptInstance.FrameState): Set<Script.Frames> {
        return framesWithStates.filter { it.value == state }.keys
    }
    override fun getStateFor(event: Script.Events): ScriptInstance.EventStates {
        return eventsWithStates[event]!!
    }
    override fun getStateFor(frame: Script.Frames): ScriptInstance.FrameState {
        return framesWithStates[frame]!!
    }
}