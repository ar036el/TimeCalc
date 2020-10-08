package com.arealapps.timecalc.calculatorActivity.ui.calculator.ResultLayout

import TimeBlock
import TimeBlockImpl
import android.animation.Animator
import android.animation.ValueAnimator
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.HorizontalScrollView
import android.widget.TextView
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.basics.MutableTimeVariable
import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculation_engine.basics.createZero
import com.arealapps.timecalc.calculation_engine.result.*
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit
import com.arealapps.timecalc.helpers.android.*
import com.arealapps.timecalc.helpers.native_.*
import com.arealapps.timecalc.rootUtils
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

    private val TIMEBLOCK_VISIBILITY_THRESHOLD = 0.3
    private val VISIBITITY_ANIMATION_DURATION = 200L

    override var abilityPercentage: Float = 1f
        set(value) {
            checkIfPercentIsLegal(value)
            if (field != value) {
                field = value
                applyAbilityPercentage(value)
            }
        }

    private val collapsedTimeBlocks = mutableListOf<TimeBlock>()
    private val timeBlocksOriginalNumber = MutableTimeVariable { createZero() }

    private val scrollViewContainer: HorizontalScrollView by lazy { layout.findViewById(R.id.resultLayout_scrollView) }

    private val autosizeApplier: ResultLayoutAutosizeApplier = ResultLayoutAutosizeApplierImpl(
        layout.findViewById(R.id.resultLayout_containerForResize),
        layout.findViewById(R.id.resultLayout_containerForScaleAndSourceSize),
        widthThresholdInPx,
        dimenFromResAsPx(R.dimen.resultLayout_minHeight),
        dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled)
    )


    private fun applyAbilityPercentage(percent: Float) {
        layout.alpha = percentToValue(percent, floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled), floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled))
        layoutContainer.heightByLayoutParams = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled)).toInt()
        val newMaxHeight = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), min(dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled), autosizeApplier.getActualMaxHeightForCurrentResult()))
        autosizeApplier.updateLayoutSize(newMaxHeight)
    }


    override var result: Result? = result
        private set

    override fun updateResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        this.result = result
        setLayoutComponentsForResult(result, doWhenFinished)
    }


    var timeBlocks: TimeVariable<TimeBlock> by initOnce()
    var timeBlocksAsList: List<TimeBlock> by initOnce()
    var timeBlocksExtensionFields: TimeVariable<DynamicFieldsDispatcher<TimeBlock>> by initOnce()

    private val numberTextView: TextView by lazy { layout.findViewById(R.id.resultLayout_textValue) }

    private val TimeBlock.isHidden get() = visibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD
    private val TimeBlock.isAllegHidden get() = timeBlocksOriginalNumber[this.timeUnit].isZero()
    private val TimeBlock.isCollapsed get() = collapsedTimeBlocks.contains(this)
    private val TimeBlock.originalNumber get() = timeBlocksOriginalNumber[this.timeUnit]

    private fun List<TimeBlock>.getAllCollapsedIn(timeBlock: TimeBlock): List<TimeBlock>? {
        var firstVisibleBlockAfterThis = indexOfFirst {
            indexOf(it) > indexOf(timeBlock)
                    && !it.isHidden }
        val untilIndex = if (firstVisibleBlockAfterThis == -1) lastIndex+1 else firstVisibleBlockAfterThis

        return filter {
            indexOf(it) > indexOf(timeBlock)
                    && indexOf(it) < untilIndex
                    && it.isCollapsed }.ifEmpty { null }

    }

    private fun setTimeBlocksVisibilityPercentage(subject: TimeBlock, source: TimeBlock, visibilityPercentage: Float, treatSourceAsHidden: Boolean) {

        val lastVisibilityPercentage = subject.visibilityPercentage
        subject.visibilityPercentage = visibilityPercentage

        if (treatSourceAsHidden) {
             source.visibilityPercentage = 1f - visibilityPercentage
        }

        if (visibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD && lastVisibilityPercentage >= TIMEBLOCK_VISIBILITY_THRESHOLD) {
            updateTimeBlockMaximizationState(source)
            Log.v("TimeResultUI", "${subject.timeUnit} was collapsed into ${source.timeUnit}")
        } else if (visibilityPercentage >= TIMEBLOCK_VISIBILITY_THRESHOLD && lastVisibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD){
            updateTimeBlockMaximizationState(source)
            Log.v("TimeResultUI", "${subject.timeUnit} was revealed from ${source.timeUnit}")
        }
        if (visibilityPercentage == 0f) {
            updateTimeBlockMaximizationState(subject)
        }

        autosizeApplier.updateLayoutSize()
    }

    private var valueAnimator: ValueAnimator? = null

    private fun getSourceForCollapsedBlockIfAny(toCollapse: TimeBlock): TimeBlock? {
        var buffer: TimeBlock? = toCollapse
        while (true) {
            buffer = timeBlocksAsList.prev(buffer)
            if (buffer == null || !buffer.isCollapsed) {
                val buf = buffer?.timeUnit
                return buffer
            }
        }
    }

    private fun tryToCollapseTimeBlock(toCollapse: TimeBlock, animate: Boolean): Boolean {
        val source = getSourceForCollapsedBlockIfAny(toCollapse)
        if (toCollapse.isHidden || toCollapse.isCollapsed || source == null || valueAnimator?.isRunning == true) { return false }
        val treatSourceAsHidden = (source.isAllegHidden && (timeBlocksAsList.getAllCollapsedIn(source).isNullOrEmpty()))

        collapsedTimeBlocks.add(toCollapse)

        if (animate) {
            startTimeBlockVisibilityAnimation(1f, 0f) { setTimeBlocksVisibilityPercentage(toCollapse, source, it, treatSourceAsHidden) }
        } else {
            setTimeBlocksVisibilityPercentage(toCollapse, source, 0f, treatSourceAsHidden)
        }
        return true

    }


    private fun tryToRevealTimeBlock(source: TimeBlock, animate: Boolean): Boolean {
        val toReveal = timeBlocksAsList.getAllCollapsedIn(source)?.last()
        if (source.isHidden || toReveal == null || !toReveal.isCollapsed || valueAnimator?.isRunning == true) { return false }
        val treatSourceAsHidden = (source.isAllegHidden && (timeBlocksAsList.getAllCollapsedIn(source).orEmpty() - toReveal).isEmpty())

        collapsedTimeBlocks.remove(toReveal)

        if (animate) {
            startTimeBlockVisibilityAnimation(0f, 1f) { setTimeBlocksVisibilityPercentage(toReveal, source, it, treatSourceAsHidden) }
        } else {
            setTimeBlocksVisibilityPercentage(toReveal, source, 1f, treatSourceAsHidden)
        }
        return true
    }

    private fun startTimeBlockVisibilityAnimation(minValue: Float, maxValue: Float, setBlockVisibilityFun: (Float) -> Unit) {
        valueAnimator = ValueAnimator.ofFloat(minValue, maxValue)
        valueAnimator!!.apply {
            addUpdateListener { animation ->
                setBlockVisibilityFun(animatedValue as Float)
            }
            addListener(object: AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
//                    updateContainerSize()
                }
            })
            duration = VISIBITITY_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()

        }
    }

    private fun updateTimeBlockMaximizationState(timeBlock: TimeBlock) {
        timeBlocksAsList.forEach {
            if (timeBlocksAsList.getAllCollapsedIn(timeBlock).isNullOrEmpty()) {
                //set as normal
                timeBlock.number = timeBlock.originalNumber
                timeBlock.isMaximizedSymbolVisible = false
            } else {
                //set as maximized
                val allCollapsedInBlock = timeBlocksAsList.getAllCollapsedIn(timeBlock)
                var number: Num = timeBlock.originalNumber
                allCollapsedInBlock?.forEach {
                    number += rootUtils.timeConverter.convertTimeUnit(timeBlock.originalNumber, it.timeUnit, timeBlock.timeUnit)
                }
                timeBlock.number = number
                timeBlock.isMaximizedSymbolVisible = true
            }
        }
    }


    private fun collapseTimeBlockInAnimation(block: TimeUnit) { //todo remove later
        val successful = tryToCollapseTimeBlock(timeBlocks[block], true)
        if (!successful) {
            rootUtils.toastManager.showShort("collapse not successful")
        }
    }

    private fun revealTimeBlockInAnimation(fromBlock: TimeUnit) { //todo remove later
        val successful = tryToRevealTimeBlock(timeBlocks[fromBlock], true)
        if (!successful) {
            rootUtils.toastManager.showShort("reveal not successful")
        }
    }

    private fun initTimeBlocks() {

        val blocklayouts = TimeVariable(
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
                blocklayouts[timeUnit],
                colors[timeUnit],
                strings[timeUnit],
                createZero()
            )
        }

        timeBlocksAsList = timeBlocks.toList()
        timeBlocksExtensionFields = TimeVariable { DynamicFieldsDispatcher(timeBlocks[it]) }

        timeBlocksAsList.forEach{
            it.addListener(timeBlockListener)
        }
    }

    private val timeBlockListener = object: TimeBlock.Listener {
        override fun onBlockSingleClick(subject: TimeBlock) {
            if (this@ResultLayoutImpl.areGesturesEnabled) {
                collapseTimeBlockInAnimation(subject.timeUnit)
            }
        }
        override fun onBlockDoubleClick(subject: TimeBlock) {
            if (this@ResultLayoutImpl.areGesturesEnabled) {
                revealTimeBlockInAnimation(subject.timeUnit)
            }
        }
        override fun blockWidthHasChanged(subject: TimeBlock, newWidth: Int) {
        }
    }

    private fun setTimeBlocksState(result: Result?) {
        val timeValues = when (result) {
            is TimeResult -> result.time.timeUnits
            is MixedResult -> result.time.timeUnits
            else -> TimeVariable{ createZero() }
        }

        for (block in timeBlocksAsList) {
            block.number = timeValues[block.timeUnit]
            block.isMaximizedSymbolVisible = false
            timeBlocksOriginalNumber[block.timeUnit] = block.number

            if (block.isAllegHidden) {
                block.visibilityPercentage = 0f
            } else {
                block.visibilityPercentage = 1f
                if (config.autoCollapseTimeValues[block.timeUnit]) {
                    val successful = tryToCollapseTimeBlock(block, false)
                    if (!successful) {
                        Log.w("TimeResultUI", "cannot auto collapse ${block.timeUnit}")
                    }
                }
            }
        }
    }

    private fun textValue(result: Result?) {
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

        numberTextView.text = textValue
        numberTextView.setTextColor(colorFromRes(textColor))
    }

    private fun setLayoutComponentsForResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        setTimeBlocksState(result)
        textValue(result)
        autosizeApplier.updateLayoutSize {
            setScrollViewToEnd()
            doWhenFinished?.invoke()
        }

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

        initTimeBlocks()
        setLayoutComponentsForResult(result, null)
        initResultLayoutContainer()
        layout.visibility = View.VISIBLE
    }

}