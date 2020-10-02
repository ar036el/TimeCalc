package el.arn.timecalc.calculatorActivity.ui.expressionInputText

import android.view.ViewGroup
import android.widget.TextView
import el.arn.timecalc.R
import el.arn.timecalc.calculation_engine.expression.ExpressionBuilder
import el.arn.timecalc.calculation_engine.expression.ExpressionToStringConverter
import el.arn.timecalc.calculation_engine.expression.ExpressionToStringConverterImpl
import el.arn.timecalc.calculation_engine.expression.ExpressionToken
import el.arn.timecalc.helpers.android.*
import el.arn.timecalc.helpers.native_.checkIfPercentIsLegal
import el.arn.timecalc.helpers.native_.percentToValue
import el.arn.timecalc.helpers.native_.valueToPercent
import el.arn.timecalc.calculatorActivity.CalculatorActivity
import el.arn.timecalc.calculatorActivity.ui.expressionInputText.parts.EditTextAutosizeMaker
import el.arn.timecalc.calculatorActivity.ui.expressionInputText.parts.HookedEditText
import el.arn.timecalc.helpers.native_.initOnce
import kotlin.math.min


interface ExpressionEditText {
    var abilityPercentage: Float
    var isEnabled: Boolean
    fun getExpressionBuilderIndexByInputTextLocation(): Int
}

class ExpressionEditTextImpl(
    private val activity: CalculatorActivity,
    private val expressionBuilder: ExpressionBuilder,
) : ExpressionEditText {

    override var abilityPercentage: Float = 0f //lateinit
        set(value) {
            checkIfPercentIsLegal(value)
            field = value
            _setAbilityPercentage(value)
        }

    override var isEnabled: Boolean
        get() = editText.isEnabled
        set(value) { editText.isEnabled = value }

    private val expressionToStringConverter: ExpressionToStringConverter = ExpressionToStringConverterImpl(expressionBuilder, false, true)


    private val editText: HookedEditText = activity.findViewById(R.id.calculator_expressionEditText)
    private val container: ViewGroup = activity.findViewById(R.id.calculator_expressionEditTextContainer)
    private var editTextAutosizeMaker: EditTextAutosizeMaker by initOnce()



    private val minTextSize = dimenFromResAsPx(R.dimen.expressionEditText_minTextSize)
    private val maxTextSizeWhenFullyEnabled = dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize_fullyEnabled)
    private val maxTextSizeWhenFullyDisabled = dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize_fullyDisabled)
    private val alphaFullyDisabled = floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled)
    private val alphaFullyEnabled = floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled)
    private var heightFullyEnabled: Float by initOnce()
    private var heightFullyDisabled: Float by initOnce()


    private fun _setAbilityPercentage(percent: Float) {
        editText.doWhenDynamicVariablesAreReady {
            editText.alpha = percentToValue(percent, alphaFullyDisabled, alphaFullyEnabled)
            val newHeight = percentToValue(percent, heightFullyDisabled, heightFullyEnabled)
            container.heightByLayoutParams = newHeight.toInt()
            val scale = percentToValue(percent, min(maxTextSizeWhenFullyDisabled / editText.textSize, 1f), 1f)
            editTextAutosizeMaker.textSizeAdditionalScale = scale
            editText.setSelection(editText.text?.length ?: 0)
        }
    }

    override fun getExpressionBuilderIndexByInputTextLocation(): Int {
        return expressionToStringConverter.stringIndexToExpressionIndex(editText.selectionStart)
    }

    private fun initEditText() {
        editTextAutosizeMaker = EditTextAutosizeMaker(
            editText,
            minTextSize,
            maxTextSizeWhenFullyEnabled
        )
        editText.setText("") //means textSize will be 'maxTextSizeWhenFullyEnabled'
        editText.showSoftInputOnFocus = false
    }


    private val editTextViewListener = object : HookedEditText.Listener {
        override fun onSelectionChanged(
            subject: HookedEditText,
            selectionStart: Int,
            selectionEnd: Int,
        ) {
            val fixedSelectionStart =
                fixSelectionPositionByConvertingToExpressionAndBackToString(selectionStart)
            val fixedSelectionEnd =
                fixSelectionPositionByConvertingToExpressionAndBackToString(selectionEnd)

            if (fixedSelectionStart != selectionStart || fixedSelectionEnd != selectionEnd) {
                subject.setSelection(fixedSelectionStart, fixedSelectionEnd)
            }
        }

        private fun fixSelectionPositionByConvertingToExpressionAndBackToString(selectionPosition: Int): Int {
            return expressionToStringConverter.expressionIndexToStringIndex(
                expressionToStringConverter.stringIndexToExpressionIndex(selectionPosition))
        }
    }

    private val expressionBuilderListener = object : ExpressionBuilder.Listener {
        override fun expressionWasCleared() {
            editText.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
        }
        override fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {
            editText.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
            editText.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index + 1))
        }
        override fun exprTokenWasReplacedAt(
            token: ExpressionToken,
            replaced: ExpressionToken,
            index: Int,
        ) {
            editText.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
            editText.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index + 1))
        }
        override fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {
            editText.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
            editText.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index))
        }
    }

    init {
        initEditText()
        editText.listenersHolder.addListener(editTextViewListener)
        expressionBuilder.addListener(expressionBuilderListener)

        editText.doWhenDynamicVariablesAreReady {
            heightFullyEnabled = editText.height.toFloat()
            heightFullyDisabled = editText.height - maxTextSizeWhenFullyEnabled + maxTextSizeWhenFullyDisabled
            abilityPercentage = 1f
        }
    }
}