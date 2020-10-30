package com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText

import android.view.ViewGroup
import android.widget.TextView
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.calculation_engine.expression.ExpressionBuilder
import com.arealapps.timecalculator.calculation_engine.converters.ExpressionToStringConverter
import com.arealapps.timecalculator.calculation_engine.expression.ExpressionToken
import com.arealapps.timecalculator.helpers.android.*
import com.arealapps.timecalculator.helpers.native_.checkIfPercentIsLegal
import com.arealapps.timecalculator.helpers.native_.percentToValue
import com.arealapps.timecalculator.activities.calculatorActivity.CalculatorActivity
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText.parts.EditTextAutosizeMaker
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText.parts.HookedEditText
import com.arealapps.timecalculator.helpers.native_.initOnce
import com.arealapps.timecalculator.helpers.android.setTimeoutUiCompat
import com.arealapps.timecalculator.rootUtils
import kotlin.math.min


interface ExpressionLayout {
    var abilityPercentage: Float
    var isTextEditEnabled: Boolean
    fun getExpressionBuilderIndexByInputTextLocation(): Int
}

class ExpressionLayoutImpl(
    private val activity: CalculatorActivity,
    private val expressionBuilder: ExpressionBuilder,
) : ExpressionLayout {

    override var abilityPercentage: Float = 0f //lateinit
        set(value) {
            checkIfPercentIsLegal(value)
            field = value
            _setAbilityPercentage(value)
        }

    override var isTextEditEnabled: Boolean
        get() = editText.isEnabled
        set(value) {
            if (editText.isEnabled != value) {
                editText.isEnabled = value
            }
        }

    private val expressionToStringConverter: ExpressionToStringConverter = rootUtils.expressionToStringConverter


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
        return expressionToStringConverter.stringIndexToExpressionIndex(expressionBuilder.getExpression(), editText.selectionStart)
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

        override fun onTextPaste(textBeforePaste: String) {
            restoreToLastTextAfterPasteWithALittleDelayForThisToWork(textBeforePaste)
        }

        private fun fixSelectionPositionByConvertingToExpressionAndBackToString(selectionPosition: Int): Int {
            val expression = expressionBuilder.getExpression()
            return expressionToStringConverter.expressionIndexToStringIndex(expression,
                expressionToStringConverter.stringIndexToExpressionIndex(expression, selectionPosition))
        }

        private fun restoreToLastTextAfterPasteWithALittleDelayForThisToWork(textBeforePaste: String) {
            setTimeoutUiCompat(activity, 30) {
                editText.setText(textBeforePaste, TextView.BufferType.EDITABLE)
            }
        }
    }

    private val expressionBuilderListener = object : ExpressionBuilder.Listener {
        override fun expressionWasCleared() {
            editText.setText(expressionToStringConverter.expressionToString(expressionBuilder.getExpression()),
                TextView.BufferType.EDITABLE)
        }
        override fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {
            editText.setText(expressionToStringConverter.expressionToString(expressionBuilder.getExpression()),
                TextView.BufferType.EDITABLE)
            editText.setSelection(expressionToStringConverter.expressionIndexToStringIndex(expressionBuilder.getExpression(), index + 1))
        }
        override fun exprTokenWasReplacedAt(
            token: ExpressionToken,
            replaced: ExpressionToken,
            index: Int,
        ) {
            editText.setText(expressionToStringConverter.expressionToString(expressionBuilder.getExpression()),
                TextView.BufferType.EDITABLE)
            editText.setSelection(expressionToStringConverter.expressionIndexToStringIndex(expressionBuilder.getExpression(), index + 1))
        }
        override fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {
            editText.setText(expressionToStringConverter.expressionToString(expressionBuilder.getExpression()),
                TextView.BufferType.EDITABLE)
            editText.setSelection(expressionToStringConverter.expressionIndexToStringIndex(expressionBuilder.getExpression(), index))
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