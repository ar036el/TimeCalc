package com.arealapps.timecalc.calculatorActivity.ui.calculator.resultLayout

import TimeBlock
import android.view.animation.AccelerateDecelerateInterpolator
import com.arealapps.timecalc.calculation_engine.TimeExpression
import com.arealapps.timecalc.calculation_engine.basics.MutableTimeVariable
import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculation_engine.basics.createZero
import com.arealapps.timecalc.calculation_engine.result.MixedResult
import com.arealapps.timecalc.calculation_engine.result.Result
import com.arealapps.timecalc.calculation_engine.result.TimeResult
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit
import com.arealapps.timecalc.helpers.native_.*
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.PercentAnimation
import com.arealapps.timecalc.utils.isRunning

interface CollapseMechanism {
    fun initTimeBlocksForNewResult(result: Result?, blocksToAutoCollapseOnInit: TimeVariable<Boolean>)
    fun tryToCollapseTimeBlockWithAnimation(target: TimeBlock)
    fun tryToRevealTimeBlockWithAnimation(target: TimeBlock)
}

class CollapseMechanismImpl(
    private val timeBlocks: TimeVariable<TimeBlock>,
    private var currentResult: Result?,
    private val doWhenChanged: () -> Unit
) : CollapseMechanism {

    private val TIMEBLOCK_VISIBILITY_THRESHOLD = 0.3
    private val VISIBITITY_ANIMATION_DURATION = 200L
    private var percentAnimation: PercentAnimation? = null


    private enum class TimeBlockStates { Collapsed, HiddenEmpty , Normal, Maximized }
    private var timeBlocksAsList = timeBlocks.toList()

    private val _timeBlocks = object {
        val blockStates = MutableTimeVariable{ TimeBlockStates.Normal }
        val originalNumber = MutableTimeVariable{ createZero() }
    }
    private var TimeBlock.state
        get() = _timeBlocks.blockStates[this.timeUnit]
        set(value) {
            _timeBlocks.blockStates[this.timeUnit] = value
            this.isMaximizedSymbolVisible = (value == TimeBlockStates.Maximized)
        }
    private var TimeBlock.originalNumber
        get() = _timeBlocks.originalNumber[this.timeUnit]
        set(value) { _timeBlocks.originalNumber[this.timeUnit] = value }
    private val TimeBlock.isOriginalNumberEmpty get() = originalNumber.isZero()


    override fun initTimeBlocksForNewResult(result: Result?, blocksToAutoCollapseOnInit: TimeVariable<Boolean>) {
        currentResult = result

        val timeValuesOfCurrentResult =  getCurrentResultAsTimeExpression()?.timeUnits ?: TimeVariable{ createZero() }

        for (block in timeBlocksAsList) {
            block.number = timeValuesOfCurrentResult[block.timeUnit]
            block.originalNumber = timeValuesOfCurrentResult[block.timeUnit]

            if (block.isOriginalNumberEmpty) {
                block.visibilityPercentage = 0f
                block.state = TimeBlockStates.HiddenEmpty
            } else {
                block.visibilityPercentage = 1f
                block.state = TimeBlockStates.Normal
            }
        }

        timeBlocksAsList.forEach {block ->
            if (blocksToAutoCollapseOnInit[block.timeUnit] && block.state equals (TimeBlockStates.Normal or TimeBlockStates.Maximized)) {
                if (block.timeUnit == TimeUnit.Milli) { throw InternalError("cannot collapse milli") }
                block.state = TimeBlockStates.Collapsed
                collapseTimeBlock(block, false)
            }
        }
    }


    override fun tryToCollapseTimeBlockWithAnimation(target: TimeBlock) {
        if (percentAnimation?.isRunning == true) { return }
        if (target.state equals (TimeBlockStates.Normal or TimeBlockStates.Maximized)
            && target.timeUnit != TimeUnit.Milli) {
            collapseTimeBlock(target, true)
        } else {
            //todo change later
            rootUtils.toastManager.showShort("collapse not successful")
        }
    }

    override fun tryToRevealTimeBlockWithAnimation(target: TimeBlock) {
        if (percentAnimation?.isRunning == true) { return }
        if (target.state == TimeBlockStates.Maximized) {
            if (target.timeUnit == TimeUnit.Year) { throw InternalError() }
            revealTimeBlock(target, true)
        } else {
            rootUtils.toastManager.showShort("reveal not successful")
        }
    }


    private fun collapseTimeBlock(toCollapse: TimeBlock, animate: Boolean) {
        if (toCollapse.state notEquals (TimeBlockStates.Normal or TimeBlockStates.Maximized)) { throw InternalError() }

        val maximizedSource = getMaximizedBlockForToBeCollapsedBlock(toCollapse)

        val allCurrentlyCollapsedBlocksInsideMaximized = getAllCollapsedBlocksForMaximizedBlockNotStrict(maximizedSource)
        val treatSourceAsHidden = (maximizedSource.isOriginalNumberEmpty && (allCurrentlyCollapsedBlocksInsideMaximized.isEmpty()))

        if (animate) {
            startCollapseAnimation {
                setCollapseStep(toCollapse, maximizedSource, it, treatSourceAsHidden)
            }
        } else {
            setCollapseStep(toCollapse, maximizedSource, 0f, treatSourceAsHidden)
        }
    }

    private fun revealTimeBlock(maximizedSource: TimeBlock, animate: Boolean) {
        if (maximizedSource.state != TimeBlockStates.Maximized) { throw InternalError() }

        val toReveal = getLastCollapsedBlocksForMaximizedBlock(maximizedSource)

        fun getAllCollapsedBlockInsideMaximizedAfterReveal(): List<TimeBlock> {
            val emptyBlocksNextToTheRevealedOne = (getAllCollapsedBlocksForMaximizedBlockNotStrict(maximizedSource) - toReveal).asReversed().takeWhile { it.isOriginalNumberEmpty }
            return getAllCollapsedBlocksForMaximizedBlockNotStrict(maximizedSource) - toReveal - emptyBlocksNextToTheRevealedOne
        }

        val treatSourceAsHidden = (maximizedSource.isOriginalNumberEmpty && (getAllCollapsedBlockInsideMaximizedAfterReveal().isEmpty()))

        if (animate) {
            startRevealAnimation {
                setRevealStep(toReveal, maximizedSource, it, treatSourceAsHidden)
            }
        } else {
            setRevealStep(toReveal, maximizedSource, 1f, treatSourceAsHidden)
        }
    }


    private fun setCollapseStep(toCollapse: TimeBlock, maximizedSource: TimeBlock, visibilityPercentage: Float, treatSourceAsHidden: Boolean) {

        val lastVisibilityPercentage = toCollapse.visibilityPercentage

        toCollapse.visibilityPercentage = visibilityPercentage
        if (treatSourceAsHidden) {
            maximizedSource.visibilityPercentage = 1f - visibilityPercentage
        }

        if (visibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD && lastVisibilityPercentage >= TIMEBLOCK_VISIBILITY_THRESHOLD) {
            setAsCollapsed(toCollapse, maximizedSource)
        }

        if (visibilityPercentage == 0f) {
            toCollapse.number = toCollapse.originalNumber
        }

        doWhenChanged()
    }

    private fun setRevealStep(toReveal: TimeBlock, maximizedSource: TimeBlock, visibilityPercentage: Float, treatSourceAsHidden: Boolean) {

        val lastVisibilityPercentage = toReveal.visibilityPercentage

        toReveal.visibilityPercentage = visibilityPercentage
        if (treatSourceAsHidden) {
            maximizedSource.visibilityPercentage = 1f - visibilityPercentage
        }

        if (visibilityPercentage >= TIMEBLOCK_VISIBILITY_THRESHOLD && lastVisibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD){
            setAsRevealed(toReveal, maximizedSource)
        }

        if (visibilityPercentage == 0f) {
            toReveal.number = toReveal.originalNumber
        }

        doWhenChanged()
    }


    private fun setAsCollapsed(toCollapse: TimeBlock, maximizedSource: TimeBlock) {
        fun TimeBlock.getUpdatedNumber() = getCurrentResultAsTimeExpression()!!.getAsCollapsed(TimeVariable{ timeBlocks[it].state == TimeBlockStates.Collapsed })[this.timeUnit]
        toCollapse.state = TimeBlockStates.Collapsed
        maximizedSource.state = TimeBlockStates.Maximized
        maximizedSource.number = maximizedSource.getUpdatedNumber()
    }

    private fun setAsRevealed(toReveal: TimeBlock, maximizedSource: TimeBlock) {
        val allCollapsedBlocksInMaximizedBlockReversed = getAllCollapsedBlocksForMaximizedBlock(maximizedSource).asReversed()
        if (allCollapsedBlocksInMaximizedBlockReversed.first() != toReveal) { throw InternalError() } //just checking
        if (timeBlocksAsList.next(toReveal)?.state == TimeBlockStates.Collapsed) { throw InternalError() } //just checking
        if (toReveal.isOriginalNumberEmpty) { throw InternalError() } //todo can it be conflicted with the auto collapse in init?

        toReveal.state = TimeBlockStates.Normal
        toReveal.number = toReveal.originalNumber //todo is this neccesary also here?

        //uncollapse all items next to the revealed one that are empty
        allCollapsedBlocksInMaximizedBlockReversed.apply {
            forEachIndexed { index, timeBlock ->
                if (index == 0) { return@forEachIndexed }
                if (!timeBlock.isOriginalNumberEmpty) { return@apply }
                timeBlock.state = TimeBlockStates.HiddenEmpty
            }
        }
        if (getAllCollapsedBlocksForMaximizedBlockNotStrict(maximizedSource).isEmpty()) {
            maximizedSource.state = if (maximizedSource.isOriginalNumberEmpty) TimeBlockStates.HiddenEmpty else TimeBlockStates.Normal
        }
        maximizedSource.number = maximizedSource.getUpdatedNumberConsideringCollapsedItems()
    }

    private fun TimeBlock.getUpdatedNumberConsideringCollapsedItems(): Num {
        return getCurrentResultAsTimeExpression()!!.getAsCollapsed(TimeVariable{ timeBlocks[it].state == TimeBlockStates.Collapsed })[this.timeUnit]
    }




    private fun startCollapseAnimation(setBlockVisibilityFun: (Float) -> Unit) {
        percentAnimation = PercentAnimation(
            VISIBITITY_ANIMATION_DURATION,
            AccelerateDecelerateInterpolator(),
            { setBlockVisibilityFun(it) },
            null,
            true,
            PercentAnimation.Directions.OneToZero
        )
    }

    private fun startRevealAnimation(setBlockVisibilityFun: (Float) -> Unit) {
        percentAnimation = PercentAnimation(
            VISIBITITY_ANIMATION_DURATION,
            AccelerateDecelerateInterpolator(),
            { setBlockVisibilityFun(it) },
            null,
            true,
            PercentAnimation.Directions.ZeroToOne
        )
    }


    private fun getAllCollapsedBlocksForMaximizedBlock(maximizedBlock: TimeBlock): List<TimeBlock> {
        if (maximizedBlock.state != TimeBlockStates.Maximized) { throw InternalError() }
        val allNextTimeBlocks = timeBlocksAsList.allNext(maximizedBlock)
        if (allNextTimeBlocks.isEmpty()) { throw InternalError() }

        return allNextTimeBlocks
            .takeWhile { it.state == TimeBlockStates.Collapsed }
            .ifEmpty { throw InternalError() }
    }

    private fun getAllCollapsedBlocksForMaximizedBlockNotStrict(maximizedBlock: TimeBlock): List<TimeBlock> {
        val allNextTimeBlocks = timeBlocksAsList.allNext(maximizedBlock)
        if (allNextTimeBlocks.isEmpty()) { throw InternalError() }

        return allNextTimeBlocks.takeWhile { it.state == TimeBlockStates.Collapsed }
    }

    private fun getLastCollapsedBlocksForMaximizedBlock(maximizedBlock: TimeBlock): TimeBlock {
        return getAllCollapsedBlocksForMaximizedBlock(maximizedBlock).last()
    }

    private fun getMaximizedBlockForToBeCollapsedBlock(toBeCollapsed: TimeBlock): TimeBlock {
        if (toBeCollapsed.state notEquals (TimeBlockStates.Normal or TimeBlockStates.Maximized)) { throw InternalError() }
        val allPrevTimeBlocks = timeBlocksAsList.allPrev(toBeCollapsed)
        if (allPrevTimeBlocks.isEmpty()) { throw InternalError() }

        allPrevTimeBlocks.asReversed().forEach {
            val a = it.timeUnit
            val b = it.state
            if (it.state equals (TimeBlockStates.Normal or TimeBlockStates.Maximized or TimeBlockStates.HiddenEmpty)) {
                return it
            }
        }
        throw InternalError() //if reached to end of list and no maximized block found
    }

    private fun getCurrentResultAsTimeExpression(): TimeExpression? {
        return when (currentResult) {
            is TimeResult -> (currentResult as TimeResult).time
            is MixedResult -> (currentResult as MixedResult).time
            else -> null
        }
    }


}