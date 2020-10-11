package com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.resultLayout

import TimeBlock
import TimeBlockImpl
import android.view.View
import android.view.ViewGroup
import android.widget.HorizontalScrollView
import android.widget.TextView
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculation_engine.basics.createZero
import com.arealapps.timecalc.calculation_engine.result.*
import com.arealapps.timecalc.helpers.android.*
import com.arealapps.timecalc.helpers.native_.*
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

    private var timeBlocks: TimeVariable<TimeBlock> by initOnce()
    private val autosizeApplier: AutosizeApplier = initAutosizeApplier(widthThresholdInPx)
    private var collapseMechanism: CollapseMechanism by initOnce()

    private val scrollViewContainer: HorizontalScrollView by lazy { layout.findViewById(R.id.resultLayout_scrollView) }
    private val textValueTextView: TextView by lazy { layout.findViewById(R.id.resultLayout_textValue) }


    override fun updateResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        this.result = result
        initLayoutComponentsForNewResult(result, doWhenFinished)
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
                collapseMechanism.tryToCollapseTimeBlockWithAnimation(subject)
            }
        }
        override fun onBlockDoubleClick(subject: TimeBlock) {
            if (this@ResultLayoutImpl.areGesturesEnabled) {
                collapseMechanism.tryToRevealTimeBlockWithAnimation(subject)
            }
        }
        override fun blockWidthHasChanged(subject: TimeBlock, newWidth: Int) {}
    }

    private fun initLayoutComponentsForNewResult(result: Result?, doWhenFinished: (() -> Unit)?) {
        collapseMechanism.initTimeBlocksForNewResult(result, config.autoCollapseTimeValues)
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
        timeBlocks.toList().forEach{ it.addListener(timeBlockListener) }

        collapseMechanism = CollapseMechanismImpl(timeBlocks, result) {
            autosizeApplier.updateLayoutSize()
        }

        initLayoutComponentsForNewResult(result, null)
        initResultLayoutContainer()
        layout.visibility = View.VISIBLE
    }

}