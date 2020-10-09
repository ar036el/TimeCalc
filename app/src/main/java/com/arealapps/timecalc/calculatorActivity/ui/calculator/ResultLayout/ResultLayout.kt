package com.arealapps.timecalc.calculatorActivity.ui.calculator.ResultLayout

import TimeBlock
import TimeBlockImpl
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.HorizontalScrollView
import android.widget.TextView
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.TimeExpression
import com.arealapps.timecalc.calculation_engine.basics.MutableTimeVariable
import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculation_engine.basics.createZero
import com.arealapps.timecalc.calculation_engine.result.*
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit
import com.arealapps.timecalc.helpers.android.*
import com.arealapps.timecalc.helpers.native_.*
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.PercentAnimation
import com.arealapps.timecalc.utils.isRunning
import kotlin.math.min

interface ResultLayout {
    var abilityPercentage: Float
    var areGesturesEnabled: Boolean
    val result: Result?
    fun updateResult(result: Result?, doWhenFinished: (() -> Unit)? = null)


    class Config(
        val autoCollapseTimeValues: TimeVariable<Boolean> //todo auto??
    )
}


class ResultLayoutImpl(
    private val layout: ViewGroup,
    private val layoutContainer: ViewGroup,
    result: Result?,
    private val config: ResultLayout.Config,
    widthThresholdInPx: Float,
    override var areGesturesEnabled: Boolean = true
): ResultLayout {

    override var abilityPercentage: Float = 1f
        set(value) {
            checkIfPercentIsLegal(value)
            if (field != value) {
                field = value
                applyAbilityPercentage(value)
            }
        }

    override var result: Result? = result
        private set

    private enum class TimeBlockStates { Collapsed, HiddenEmpty , Normal, Maximized }
    private var timeBlocks: TimeVariable<TimeBlock> by initOnce()
    private var timeBlocksAsList: List<TimeBlock> by initOnce()

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


    private val autosizeApplier: ResultLayoutAutosizeApplier = initAutosizeApplier(widthThresholdInPx)

    private val scrollViewContainer: HorizontalScrollView by lazy { layout.findViewById(R.id.resultLayout_scrollView) }
    private val textValueTextView: TextView by lazy { layout.findViewById(R.id.resultLayout_textValue) }

    private val TIMEBLOCK_VISIBILITY_THRESHOLD = 0.3
    private val VISIBITITY_ANIMATION_DURATION = 200L
    private var percentAnimation: PercentAnimation? = null




    override fun updateResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        this.result = result
        initLayoutComponentsForNewResult(result, doWhenFinished)
    }


    private fun getResultAsTimeExpression(): TimeExpression? {
        return when (result) {
            is TimeResult -> (result as TimeResult).time
            is MixedResult -> (result as MixedResult).time
            else -> null
        }
    }

    private fun applyAbilityPercentage(percent: Float) {
        layout.alpha = percentToValue(percent, floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled), floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled))
        layoutContainer.heightByLayoutParams = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled)).toInt()
        val newMaxHeight = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), min(dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled), autosizeApplier.getActualMaxHeightForCurrentResult()))
        autosizeApplier.updateLayoutSize(newMaxHeight)
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

        autosizeApplier.updateLayoutSize()
    }

    private fun setAsCollapsed(toCollapse: TimeBlock, maximizedSource: TimeBlock) {
        fun TimeBlock.getUpdatedNumber() = getResultAsTimeExpression()!!.getAsCollapsed(TimeVariable{ timeBlocks[it].state == TimeBlockStates.Collapsed })[this.timeUnit]
        toCollapse.state = TimeBlockStates.Collapsed
        maximizedSource.state = TimeBlockStates.Maximized
        maximizedSource.number = maximizedSource.getUpdatedNumber()
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

        autosizeApplier.updateLayoutSize()
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
        return getResultAsTimeExpression()!!.getAsCollapsed(TimeVariable{ timeBlocks[it].state == TimeBlockStates.Collapsed })[this.timeUnit]
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

    private fun tryToCollapseTimeBlockWithAnimation(target: TimeBlock) {
        if (percentAnimation?.isRunning == true) { return }
        if (target.state equals (TimeBlockStates.Normal or TimeBlockStates.Maximized)
            && target.timeUnit != TimeUnit.Milli) {
            collapseTimeBlock(target, true)
        } else {
            rootUtils.toastManager.showShort("collapse not successful")
        }
    }

    private fun tryToRevealTimeBlockWithAnimation(target: TimeBlock) {
        if (percentAnimation?.isRunning == true) { return }
        if (target.state == TimeBlockStates.Maximized) {
            if (target.timeUnit == TimeUnit.Year) { throw InternalError() }
            revealTimeBlock(target, true)
        } else {
            rootUtils.toastManager.showShort("reveal not successful")
        }
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


    private fun initAutosizeApplier(widthThresholdInPx: Float): ResultLayoutAutosizeApplier {
        return ResultLayoutAutosizeApplierImpl(
            layout.findViewById(R.id.resultLayout_containerForResize),
            layout.findViewById(R.id.resultLayout_containerForScaleAndSourceSize),
            widthThresholdInPx,
            dimenFromResAsPx(R.dimen.resultLayout_minHeight),
            dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled)
        )
    }

    private fun createTimeBlocks() {
        val blockLayouts = TimeVariable(
            R.id.timeResultBlock_millisecond,
            R.id.timeResultBlock_second,
            R.id.timeResultBlock_minute,
            R.id.timeResultBlock_hour,
            R.id.timeResultBlock_day,
            R.id.timeResultBlock_week,
            R.id.timeResultBlock_month,
            R.id.timeResultBlock_year,
        )

        val colors = TimeVariable(
            R.color.timeResultBackground_millisecond,
            R.color.timeResultBackground_second,
            R.color.timeResultBackground_minute,
            R.color.timeResultBackground_hour,
            R.color.timeResultBackground_day,
            R.color.timeResultBackground_week,
            R.color.timeResultBackground_month,
            R.color.timeResultBackground_year,
        )

        val strings = TimeVariable(
            R.string.calculator_timeUnit_millisecond_full,
            R.string.calculator_timeUnit_second_full,
            R.string.calculator_timeUnit_minute_full,
            R.string.calculator_timeUnit_hour_full,
            R.string.calculator_timeUnit_day_full,
            R.string.calculator_timeUnit_week_full,
            R.string.calculator_timeUnit_month_full,
            R.string.calculator_timeUnit_year_full,
        )

        timeBlocks = TimeVariable { timeUnit ->
            TimeBlockImpl(
                layout,
                timeUnit,
                blockLayouts[timeUnit],
                colors[timeUnit],
                strings[timeUnit],
                createZero()
            )
        }
    }

    private val timeBlockListener = object: TimeBlock.Listener {
        override fun onBlockSingleClick(subject: TimeBlock) {
            if (this@ResultLayoutImpl.areGesturesEnabled) {
                tryToCollapseTimeBlockWithAnimation(subject)
            }
        }
        override fun onBlockDoubleClick(subject: TimeBlock) {
            if (this@ResultLayoutImpl.areGesturesEnabled) {
                tryToRevealTimeBlockWithAnimation(subject)
            }
        }
        override fun blockWidthHasChanged(subject: TimeBlock, newWidth: Int) {}
    }

    private fun initLayoutComponentsForNewResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        initTimeBlocksForNewResult(result)
        initTextValueForNewResult(result)
        autosizeApplier.updateLayoutSize {
            setScrollViewToEnd()
            doWhenFinished?.invoke()
        }
    }

    private fun initTimeBlocksForNewResult(result: Result?) {
        val timeValues = when (result) {
            is TimeResult -> result.time.timeUnits
            is MixedResult -> result.time.timeUnits
            else -> TimeVariable{ createZero() }
        }

        for (block in timeBlocksAsList) {
            block.number = timeValues[block.timeUnit]
            block.originalNumber = timeValues[block.timeUnit]

            if (block.isOriginalNumberEmpty) {
                block.visibilityPercentage = 0f
                block.state = TimeBlockStates.HiddenEmpty
            } else {
                block.visibilityPercentage = 1f
                block.state = TimeBlockStates.Normal
            }
        }

        timeBlocksAsList.forEach {block ->
            if (config.autoCollapseTimeValues[block.timeUnit] && block.state equals (TimeBlockStates.Normal or TimeBlockStates.Maximized)) {
                if (block.timeUnit == TimeUnit.Milli) { throw InternalError("cannot collapse milli") }
                block.state = TimeBlockStates.Collapsed
                collapseTimeBlock(block, false)
                    //todo why won't it succeed to collapse?
            }
        }
    }

    private fun initTextValueForNewResult(result: Result?) {
        var textValue = when (result) {
            is NumberResult -> result.number.toStringFormatted(true, true, false)
            is MixedResult -> result.number.toStringFormatted(true, true, true)
            is CantDivideByZeroErrorResult -> stringFromRes(R.string.errorResult_cantDivideBy0)
            is CantMultiplyTimeQuantitiesErrorResult ->stringFromRes(R.string.errorResult_cantMultiplyTimeQuantities)
//            is ExpressionIsEmptyErrorResult -> throw NotImplementedError("todo!")
            is BadFormulaErrorResult -> stringFromRes(R.string.errorResult_badFormula)
            else -> ""
        }

        val textColor = if (result is ErrorResult) R.color.errorResultText else R.color.normalResultText

        textValueTextView.text = textValue
        textValueTextView.setTextColor(colorFromRes(textColor))
    }

    private fun setScrollViewToEnd() {
        //bad code but whatever
        scrollViewContainer.scrollTo(1000000000, 0)
    }

    private fun initResultLayoutContainer() {
        layout.doWhenDynamicVariablesAreReady {
            layoutContainer.heightByLayoutParams = it.height
        }
    }


    init {
        createTimeBlocks()
        timeBlocksAsList = timeBlocks.toList()
        timeBlocksAsList.forEach{ it.addListener(timeBlockListener) }

        initLayoutComponentsForNewResult(result, null)
        initResultLayoutContainer()
        layout.visibility = View.VISIBLE
    }

}