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
import el.arn.timecalc.calculatorActivity.ui.calculatorButtonsElasticLayout.EditTextFontAutosizeMaker
import el.arn.timecalc.calculatorActivity.ui.expressionInputText.parts.HookedEditText
import el.arn.timecalc.helpers.android.PixelConverter.pxToSp
import el.arn.timecalc.helpers.native_.initOnce


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
            applyAbilityPercentage()
        }

    private val expressionToStringConverter: ExpressionToStringConverter = ExpressionToStringConverterImpl(expressionBuilder, false, true)
    private var autosizeMakerForEditText: EditTextFontAutosizeMaker by initOnce()

    private val editTextView: HookedEditText = activity.findViewById(R.id.calculator_expressionEditText)
    private val editTextContainer: ViewGroup = activity.findViewById(R.id.calculator_expressionEditTextContainer)

    private val alphaFullyDisabled = floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled)
    private val alphaFullyEnabled = floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled)

    private val minTextSize = dimenFromResAsPx(R.dimen.expressionEditText_minTextSize)
    private val maxTextSizeWhenFullyEnabled = dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize_fullyEnabled)
    private val maxTextSizeWhenFullyDisabled = dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize_fullyDisabled)

    override var isEnabled: Boolean
        get() = editTextView.isEnabled
        set(value) { editTextView.isEnabled = value }

    override fun getExpressionBuilderIndexByInputTextLocation(): Int {
        return expressionToStringConverter.stringIndexToExpressionIndex(editTextView.selectionStart)
    }

    private fun applyAbilityPercentage() {
        editTextView.textSize = pxToSp(editTextView.textSize-0.1f)
        editTextView.setSelection(editTextView.text?.length ?: 0)
        return

        editTextView.doWhenDynamicVariablesAreReady {
            editTextView.alpha = percentToValue(abilityPercentage, alphaFullyDisabled, alphaFullyEnabled)

            val newHeight = percentToValue(abilityPercentage, editTextHeightFullyDisabled, editTextHeightFullyEnabled)
            editTextContainer.heightByLayoutParams = newHeight.toInt()
            autosizeMakerForEditText.textSizeAdditionalScale = valueToPercent(newHeight, 0f, editTextHeightFullyEnabled)
        }
    }

    private var editTextHeightFullyEnabled: Float by initOnce()
    private var editTextHeightFullyDisabled: Float by initOnce()

    private fun initEditText() {
        autosizeMakerForEditText = EditTextFontAutosizeMaker(
            editTextView,
            minTextSize,
            maxTextSizeWhenFullyEnabled
        )
        editTextView.showSoftInputOnFocus = false
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
            editTextView.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
        }
        override fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {
            editTextView.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
            editTextView.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index + 1))
        }
        override fun exprTokenWasReplacedAt(
            token: ExpressionToken,
            replaced: ExpressionToken,
            index: Int,
        ) {
            editTextView.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
            editTextView.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index + 1))
        }
        override fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {
            editTextView.setText(expressionToStringConverter.expressionToString(),
                TextView.BufferType.EDITABLE)
            editTextView.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index))
        }
    }

    init {
        initEditText()
        editTextView.listenersHolder.addListener(editTextViewListener)
        expressionBuilder.addListener(expressionBuilderListener)
        editTextView.doWhenDynamicVariablesAreReady {
            editTextHeightFullyEnabled = editTextView.height.toFloat()
            editTextHeightFullyDisabled = editTextView.height - maxTextSizeWhenFullyEnabled + maxTextSizeWhenFullyDisabled
            abilityPercentage = 1f
        }
    }
}