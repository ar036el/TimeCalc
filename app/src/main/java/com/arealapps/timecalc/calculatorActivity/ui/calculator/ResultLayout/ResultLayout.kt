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
import kotlin.math.max
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
    private val resultLayout: ViewGroup,
    private val resultLayoutContainer: ViewGroup,
    result: Result?,
    private val config: ResultLayout.Config,
    private val desiredWidth: Float,
    private val minHeight: Float,
    private var maxHeight: Float,
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

    private val scrollViewContainer: HorizontalScrollView by lazy { resultLayout.findViewById(R.id.scrollViewContainer) }
    private val containerResizable: ViewGroup by lazy { resultLayout.findViewById(R.id.containerForResize) }
    private val containerSource: ViewGroup by lazy { resultLayout.findViewById(R.id.containerForScaleAndSourceSize) }
    private var layoutSizeScale = 1f
    private var prevUnscaledWidth: Float? = null
    private var prevUnscaledHeight: Float? = null


    private fun applyAbilityPercentage(percent: Float) {
        maxHeight = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), min(dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled), measureActualMaxHeightForCurrentResult()))
        containerResizable.alpha = percentToValue(percent, floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled), floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled))
        resultLayoutContainer.heightByLayoutParams = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled)).toInt()
        updateLayoutSize()
    }

    private fun measureActualMaxHeightForCurrentResult(): Float {
        val unscaledWidth = containerSource.width.toFloat()
        val unscaledHeight = containerSource.height.toFloat()

        if (unscaledWidth == 0f || unscaledHeight == 0f) {
            return 0f
        }
        val unboundedScale = desiredWidth / unscaledWidth
        return max(unboundedScale * unscaledHeight, minHeight)
    }

    private fun updateLayoutSize(doWhenFinished: (() -> Unit)? = null) {
        containerResizable.doWhenDynamicVariablesAreReady {
            containerSource.doWhenDynamicVariablesAreReady {

                //containerSource.width and containerSource.height are never affected by scaleX/scaleY changes. been tested! :#
                var unscaledWidth = containerSource.width.toFloat().let { if (it == 0f) 1f else it } //making it 1f for no error in calculation (divide by 0)
                val unscaledHeight = containerSource.height.toFloat().let { if (it == 0f) 1f else it }

                val unboundedScale = desiredWidth / unscaledWidth
                val unboundedHeight = unboundedScale * unscaledHeight
                val boundedHeight = min(max(unboundedScale * unscaledHeight, minHeight), maxHeight)
                val boundedScale = unboundedScale * (boundedHeight / unboundedHeight)


                if (layoutSizeScale == boundedScale && prevUnscaledWidth == unscaledWidth && prevUnscaledHeight == unscaledHeight) {
                    doWhenFinished?.invoke()
                    return@doWhenDynamicVariablesAreReady
                }
                prevUnscaledWidth = unscaledWidth
                prevUnscaledHeight = unscaledHeight

                layoutSizeScale = boundedScale

                containerSource.scaleX = layoutSizeScale
                containerSource.scaleY = layoutSizeScale
                containerResizable.widthByLayoutParams = (unscaledWidth*layoutSizeScale).toInt() + containerResizable.paddingX
                containerResizable.heightByLayoutParams = (unscaledHeight*layoutSizeScale).toInt() + containerResizable.paddingY
                containerSource.invalidate()
                containerSource.requestLayout()
                    containerResizable.doWhenDynamicVariablesAreReady {
                        containerSource.doWhenDynamicVariablesAreReady {
                            doWhenFinished?.invoke()
                        }
                    }
            }
        }
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

    private val textValueTextView: TextView by lazy { resultLayout.findViewById(R.id.resultLayout_textValue) }

    private val TimeBlock.extensionField get() = timeBlocksExtensionFields[this.timeUnit]

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

        updateLayoutSize()
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

        val zero = createZero()

        timeBlocks = TimeVariable(
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Milli,
                R.id.timeResultBlock_millisecond,
                R.color.timeResultBackground_millisecond,
                R.string.calculator_timeUnit_millisecond_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Second,
                R.id.timeResultBlock_second,
                R.color.timeResultBackground_second,
                R.string.calculator_timeUnit_second_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Minute,
                R.id.timeResultBlock_minute,
                R.color.timeResultBackground_minute,
                R.string.calculator_timeUnit_minute_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Hour,
                R.id.timeResultBlock_hour,
                R.color.timeResultBackground_hour,
                R.string.calculator_timeUnit_hour_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Day,
                R.id.timeResultBlock_day,
                R.color.timeResultBackground_day,
                R.string.calculator_timeUnit_day_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Week,
                R.id.timeResultBlock_week,
                R.color.timeResultBackground_week,
                R.string.calculator_timeUnit_week_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Month,
                R.id.timeResultBlock_month,
                R.color.timeResultBackground_month,
                R.string.calculator_timeUnit_month_full,
                zero
            )
            ,
            TimeBlockImpl(
                resultLayout,
                TimeUnit.Year,
                R.id.timeResultBlock_year,
                R.color.timeResultBackground_year,
                R.string.calculator_timeUnit_year_full,
                zero
            )
        )

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

        textValueTextView.text = textValue
        textValueTextView.setTextColor(colorFromRes(textColor))
    }

    private fun setLayoutComponentsForResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        setTimeBlocksState(result)
        textValue(result)
        updateLayoutSize {
            setScrollViewToEnd()
            doWhenFinished?.invoke()
        }

    }


    private fun setScrollViewToEnd() {
        //bad code but whatever
        scrollViewContainer.scrollTo(1000000000, 0)
    }

    private fun initResultLayoutContainer() {
        resultLayout.doWhenDynamicVariablesAreReady {
            resultLayoutContainer.heightByLayoutParams = it.height
        }
    }


    init {
        if (minHeight > maxHeight) { throw InternalError("minWidth[$minHeight] > maxWidth[$maxHeight]")}

        initTimeBlocks()
        setLayoutComponentsForResult(result, null)
        initResultLayoutContainer()
        resultLayout.visibility = View.VISIBLE
    }

}