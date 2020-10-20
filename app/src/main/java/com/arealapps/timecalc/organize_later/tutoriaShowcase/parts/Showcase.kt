package com.arealapps.timecalc.organize_later.tutoriaShowcase.parts

import com.arealapps.timecalc.activities.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.organize_later.errorIf
import com.arealapps.timecalc.organize_later.tutoriaShowcase.data.FramesContentData
import com.arealapps.timecalc.organize_later.tutoriaShowcase.data.Script
import com.arealapps.timecalc.organize_later.tutoriaShowcase.data.ScriptInstance

interface Showcase: HoldsListeners<Showcase.Listener> {

    val state: States
    val isRunning: Boolean
    enum class States { Ready, BetweenFrames, WaitingForEvent, Finished }

    fun start()
    fun finish()

    val framesContentData: FramesContentData
    fun notifyEventsWereOccurred(vararg events: Script.Events)

    interface Listener {
        fun stateWasChanged(subject: Showcase, newState: States)
    }
}

class ShowcaseImpl(
    private val activity: CalculatorActivity,
    private val listenersMgr: ListenersManager<Showcase.Listener> = ListenersManager()
) : Showcase, HoldsListeners<Showcase.Listener> by listenersMgr {


    override var state: Showcase.States = Showcase.States.BetweenFrames
        set(value) {
            if (field != value) {
                field = value
                listenersMgr.notifyAll { it.stateWasChanged(this, value) }
            }
        }

    override val isRunning: Boolean
        get() = state != Showcase.States.Finished

    override val framesContentData = FramesContentData(activity)

    private val scriptInstance = Script.createScriptInstance()
    private lateinit var currentFrame: Frame
    private var wasForceFinished = false

    override fun start() {
        if (state == Showcase.States.Ready) return
        showFrame(Script.firstFrame)
    }

    override fun finish() {
        if (state == Showcase.States.Finished) return
        wasForceFinished = true
        if (this::currentFrame.isInitialized) {
            currentFrame.dismiss()
        }
    }

    override fun notifyEventsWereOccurred(vararg events: Script.Events) {
        events.forEach { event ->
            if (scriptInstance.getStateFor(event) == ScriptInstance.EventStates.NotIntroduced) {
                scriptInstance.setEvents(ScriptInstance.EventStates.New, event)
            }
        }

        if (!currentFrame.isRunning) {
            tryToInvokeFrameByNewEvents()
        }
    }

    private fun tryToInvokeFrameByNewEvents() {
        errorIf { currentFrame.isRunning || state != Showcase.States.WaitingForEvent}

        val nextFrameWithNeededActions = Script.getNextFrameWithNeededActionsWhenWaitingForEventsIfMatch(scriptInstance)

        if (nextFrameWithNeededActions != null) {
            nextFrameWithNeededActions.second.invoke(scriptInstance)
            showFrame(nextFrameWithNeededActions.first)
        }
    }

    private fun showFrame(frame: Script.Frames) {
        errorIf { this::currentFrame.isInitialized && currentFrame.isRunning }

        state = Showcase.States.BetweenFrames
        val frameData = framesContentData.getFrameData(frame)
        scriptInstance.setFrames(ScriptInstance.FrameState.Active, frame)
        currentFrame = FrameImpl(frameData, activity) { doOnFrameDismiss(frame) }
        currentFrame.show()
    }

    private fun doOnFrameDismiss(frame: Script.Frames) {
        scriptInstance.setFrames(ScriptInstance.FrameState.Finished, frame)

        if (wasForceFinished) {
            finishShowcase()
        }

        val frameNextAction = Script.getFrameNextAction(frame, scriptInstance)
        when (frameNextAction) {
            is Script.FrameActions.GoToNextFrame -> showFrame(frameNextAction.frame)
            is Script.FrameActions.WaitForEvent -> setToWaitForEvent()
            is Script.FrameActions.FinishShowcase -> finishShowcase()
        }
    }

    private fun finishShowcase() {
        state = Showcase.States.Finished
    }

    private fun setToWaitForEvent() {
        state = Showcase.States.WaitingForEvent
        tryToInvokeFrameByNewEvents()
    }

}