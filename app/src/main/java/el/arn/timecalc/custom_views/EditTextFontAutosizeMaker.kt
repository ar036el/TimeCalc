package el.arn.timecalc.custom_views

import android.widget.EditText
import androidx.core.widget.addTextChangedListener
import el.arn.timecalc.android_extensions.doWhenDynamicVariablesAreReady
import el.arn.timecalc.measureTextWidth
import el.arn.timecalc.pxToSp
import kotlin.math.max
import kotlin.math.min

class EditTextFontAutosizeMaker(
    val editText: EditText,
    val minTextSize: Float,
    val maxTextSize: Float,
    private val maxTextWidthThreshold: Float? = null //todo needs to be obligatory??
) {

    val textWidthThreshold: Float by lazy { maxTextWidthThreshold ?: getInnerTextArea() }
    private var isStopped = false

    fun stopEffect() {
        isStopped = true
    }

    private fun updateTextSize() {
        val currentTextWidth = editText.measureTextWidth()
        if (currentTextWidth != 0f) {
            val resizeTextBy = textWidthThreshold / currentTextWidth
            var newTextSize = pxToSp(editText.textSize) * resizeTextBy
            newTextSize = max(newTextSize, minTextSize)
            newTextSize = min(newTextSize, maxTextSize)
            editText.textSize = newTextSize
        }
    }

    private fun getInnerTextArea(): Float {
//        val res = editText.measuredWidth - editText.marginStart - editText.marginEnd - editText.paddingStart - editText.paddingEnd
        val res = editText.width - editText.paddingStart - editText.paddingEnd
        return res.toFloat()
    }


    init {
        if (minTextSize > maxTextSize) {
            throw InternalError("minTextSize[$minTextSize] cannot be bigger than maxTextSize[$maxTextSize]")
        }
        editText.addTextChangedListener{
            if (!isStopped) {
                updateTextSize()
            }
        }
        editText.doWhenDynamicVariablesAreReady { updateTextSize() }
    }

}