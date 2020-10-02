package el.arn.timecalc.calculatorActivity.ui.calculatorButtonsElasticLayout

import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import el.arn.timecalc.helpers.android.PixelConverter.pxToSp
import el.arn.timecalc.helpers.android.doWhenDynamicVariablesAreReady
import el.arn.timecalc.helpers.android.measureTextWidth
import el.arn.timecalc.helpers.native_.boundByMinAndMax


class EditTextFontAutosizeMaker(
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
            updateEditTextTextSize()
        }

    private var wasEffectStopped = false
    fun stopEffect() {
        wasEffectStopped = true
    }

    private fun updateEditTextTextSize() {
        val currentTextWidth = editText.measureTextWidth()
        if (currentTextWidth != 0f) {
            val unboundedResizeFactor = textWidthThresholdInPx / currentTextWidth
            var newTextSizeInPx = editText.textSize * unboundedResizeFactor
            newTextSizeInPx = boundByMinAndMax(newTextSizeInPx, minTextSizeInPx, maxTextSizeInPx)
            editText.textSize = pxToSp(newTextSizeInPx) * textSizeAdditionalScale
        }
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