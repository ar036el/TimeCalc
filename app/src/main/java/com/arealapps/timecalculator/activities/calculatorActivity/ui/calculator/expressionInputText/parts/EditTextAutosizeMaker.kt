package com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText.parts

import android.util.TypedValue
import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import com.arealapps.timecalculator.helpers.android.doWhenDynamicVariablesAreReady
import com.arealapps.timecalculator.helpers.android.measureTextWidth
import com.arealapps.timecalculator.helpers.native_.boundByMinAndMax


class EditTextAutosizeMaker(
    val editText: EditText,
    minTextSizeInPx: Float,
    maxTextSizeInPx: Float,
    textWidthThresholdInPx: Float? = null //todo needs to be obligatory??
) {

    var minTextSizeInPx = minTextSizeInPx
        private set(value) {
            field = value
            checkIfMinAndMaxIsLegal()
            updateEditTextTextSize()
        }
    var maxTextSizeInPx = maxTextSizeInPx
        private set(value) {
            field = value
            checkIfMinAndMaxIsLegal()
            updateEditTextTextSize()
        }


    private val _textWidthThresholdInPx = textWidthThresholdInPx
    private val maxTextAreaWidth: Float by lazy { (editText.width - editText.paddingStart - editText.paddingEnd).toFloat() }
    val textWidthThresholdInPx: Float get() = _textWidthThresholdInPx ?: maxTextAreaWidth

    var textSizeAdditionalScale = 1f
        set(value) {
            if (value < 0) { throw InternalError() }
            field = value
            _setTextSizeAdditionalScale(value)
        }

    private var wasEffectStopped = false
    fun stopEffect() {
        wasEffectStopped = true
    }

    var theoreticalCurrentTextSize = editText.textSize * textSizeAdditionalScale

    private fun updateEditTextTextSize() {
        val currentTextWidth = editText.measureTextWidth()
        if (currentTextWidth != 0f) {
            val unboundedResizeFactor = textWidthThresholdInPx / currentTextWidth
            var newTextSizeInPx = editText.textSize * unboundedResizeFactor
            newTextSizeInPx = boundByMinAndMax(newTextSizeInPx, minTextSizeInPx, maxTextSizeInPx) * textSizeAdditionalScale
            theoreticalCurrentTextSize = newTextSizeInPx
            editText.setTextSize(TypedValue.COMPLEX_UNIT_PX , newTextSizeInPx)
        }
    }

    private fun _setTextSizeAdditionalScale(scale: Float) {
        editText.setTextSize(TypedValue.COMPLEX_UNIT_PX , theoreticalCurrentTextSize * scale)
    }

    private fun checkIfMinAndMaxIsLegal() {
        if (minTextSizeInPx > maxTextSizeInPx) {
            throw InternalError("minTextSize[$minTextSizeInPx] cannot be bigger than maxTextSize[$maxTextSizeInPx]")
        }
    }

    init {
        checkIfMinAndMaxIsLegal()
        editText.addTextChangedListener{
            if (!wasEffectStopped) {
                updateEditTextTextSize()
            }
        }
        editText.doWhenDynamicVariablesAreReady { updateEditTextTextSize() }
    }

}