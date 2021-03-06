package com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.resultLayout

import TimeBlock
import TimeBlockImpl
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.TextView
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.calculation_engine.base.TimeVariable
import com.arealapps.timecalculator.calculation_engine.base.createZero
import com.arealapps.timecalculator.calculation_engine.result.*
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpression
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionUtils
import com.arealapps.timecalculator.helpers.android.*
import com.arealapps.timecalculator.helpers.native_.*
import kotlin.math.min

interface ResultLayout {
    var abilityPercentage: Float
    var areGesturesEnabled: Boolean
    val result: Result?
    var config: Config
    fun updateResult(result: Result?, doWhenFinished: (() -> Unit)? = null)

    class Config(
        val autoCollapseTimeValues: TimeVariable<Boolean> //todo auto??
    )
}


class ResultLayoutImpl(
    private val layout: ViewGroup,
    private val layoutContainer: ViewGroup,
    result: Result?,
    config: ResultLayout.Config,
    widthThresholdInPx: Float,
    private val timeExpressionUtils: TimeExpressionUtils,
    override var areGesturesEnabled: Boolean = true
): ResultLayout {

    override var config: ResultLayout.Config = config
        set(value) {
            if (field != value) {
                field = value
                resetLayout()
            }
        }

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

    private val resultAsTimeExpression: TimeExpression get() {
        return when (result) {
            is TimeResult -> (result as TimeResult).time
            is MixedResult -> (result as MixedResult).time
            else -> timeExpressionUtils.createTimeExpression(createZero())
        }
    }

    private var timeBlocks: TimeVariable<TimeBlock> by initOnce()
    private val autosizeApplier: AutosizeApplier = initAutosizeApplier(widthThresholdInPx)
    private var timeBlocksCollapseMechanism: TimeBlocksCollapseMechanism by initOnce()

    private val scrollViewContainer: HorizontalScrollView by lazy { layout.findViewById(R.id.resultLayout_scrollView) }
    private val textValueTextView: TextView by lazy { layout.findViewById(R.id.resultLayout_textValue) }
    private val textValueTextViewInitialTextColor: Int by lazy { textValueTextView.currentTextColor }


    override fun updateResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        this.result = result
        initLayoutComponentsForNewResult(doWhenFinished)
    }

    private fun applyAbilityPercentage(percent: Float) {
        layout.alpha = percentToValue(percent, floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled), floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled))
        layoutContainer.heightByLayoutParams = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled)).toInt()
        val newMaxHeight = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), min(dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled), autosizeApplier.getActualMaxHeightForCurrentResult()))
        autosizeApplier.updateLayoutSize(newMaxHeight)
    }

    private fun initAutosizeApplier(widthThresholdInPx: Float): AutosizeApplier {
        return AutosizeApplierImpl(
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
            R.id.timeResultBlock_year)

        val colors = TimeVariable(
            R.color.timeResultBackground_millisecond,
            R.color.timeResultBackground_second,
            R.color.timeResultBackground_minute,
            R.color.timeResultBackground_hour,
            R.color.timeResultBackground_day,
            R.color.timeResultBackground_week,
            R.color.timeResultBackground_month,
            R.color.timeResultBackground_year)

        val strings = TimeVariable(
            R.string.calculator_timeUnit_millisecond_full,
            R.string.calculator_timeUnit_second_full,
            R.string.calculator_timeUnit_minute_full,
            R.string.calculator_timeUnit_hour_full,
            R.string.calculator_timeUnit_day_full,
            R.string.calculator_timeUnit_week_full,
            R.string.calculator_timeUnit_month_full,
            R.string.calculator_timeUnit_year_full)

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
                timeBlocksCollapseMechanism.tryToCollapseTimeBlockWithAnimation(subject)
            }
        }
        override fun onBlockDoubleClick(subject: TimeBlock) {
            if (this@ResultLayoutImpl.areGesturesEnabled) {
                timeBlocksCollapseMechanism.tryToRevealTimeBlockWithAnimation(subject)
            }
        }
        override fun blockWidthHasChanged(subject: TimeBlock, newWidth: Int) {}
    }

    private fun resetLayout() {
        initLayoutComponentsForNewResult()
    }

    private fun initLayoutComponentsForNewResult(doWhenFinished: (() -> Unit)? = null) {
        timeBlocksCollapseMechanism.initTimeBlocksForNewResult(resultAsTimeExpression, config.autoCollapseTimeValues)
        initTextValueForNewResult(result)
        autosizeApplier.updateLayoutSize {
            setScrollViewToEnd()
            doWhenFinished?.invoke()
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

        val textColor = if (result is ErrorResult) colorFromRes(R.color.errorResultText) else textValueTextViewInitialTextColor

        textValueTextView.text = textValue
        textValueTextView.setTextColor(textColor)
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
        timeBlocks.toList().forEach{ it.addListener(timeBlockListener) }

        timeBlocksCollapseMechanism = TimeBlocksCollapseMechanismImpl(timeBlocks, resultAsTimeExpression) {
            autosizeApplier.updateLayoutSize()
        }

        initLayoutComponentsForNewResult()
        initResultLayoutContainer()
        layout.visibility = View.VISIBLE
    }

}