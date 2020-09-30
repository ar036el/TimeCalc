package el.arn.timecalc.mainActivity.ui

import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import el.arn.timecalc.helpers.android.PixelConverter.pxToSp
import el.arn.timecalc.helpers.android.doWhenDynamicVariablesAreReady
import el.arn.timecalc.helpers.android.measureTextWidth
import el.arn.timecalc.helpers.native_.boundByMinAndMax

class EditTextFontAutosizeMaker(
    val editText: EditText,
    val minTextSizeInPx: Float,
    val maxTextSizeInPx: Float,
    textWidthThresholdInPx: Float? = null //todo needs to be obligatory??
) {

    private val textWidthThresholdInPx: Float by lazy { textWidthThresholdInPx ?: getMaxTextAreaWidth() }
    private var isStopped = false

    fun stopEffect() {
        isStopped = true
    }

    private fun updateEditTextTextSize() {
        val currentTextWidth = editText.measureTextWidth()
        if (currentTextWidth != 0f) {
            val unboundedResizeFactor = textWidthThresholdInPx / currentTextWidth
            var newTextSizeInPx = editText.textSize * unboundedResizeFactor
            newTextSizeInPx = boundByMinAndMax(newTextSizeInPx, minTextSizeInPx, maxTextSizeInPx)
            editText.textSize = pxToSp(newTextSizeInPx)
        }
    }

    private fun getMaxTextAreaWidth(): Float {
        val res = editText.width - editText.paddingStart - editText.paddingEnd
        return res.toFloat()
    }


    init {
        if (minTextSizeInPx > maxTextSizeInPx) {
            throw InternalError("minTextSize[$minTextSizeInPx] cannot be bigger than maxTextSize[$maxTextSizeInPx]")
        }
        editText.addTextChangedListener{
            if (!isStopped) {
                updateEditTextTextSize()
            }
        }
        editText.doWhenDynamicVariablesAreReady { updateEditTextTextSize() }
    }

}