package el.arn.timecalc.buttons_drawerlike_layout

import android.app.Activity
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import el.arn.timecalc.R
import el.arn.timecalc.android_extensions.heightByLayoutParams
import el.arn.timecalc.dpToPx
import el.arn.timecalc.helperss.percentToValue


interface ButtonsContainerBottomPart { //todo make those two classes into one???
    fun setScrollPercent(percent: Float)
}

class ButtonsContainerBottomPartImpl(activity: Activity) : ButtonsContainerBottomPart {
    private val MIN_LAYOUT_HEIGHT = dpToPx(380) - activity.findViewById<ViewGroup>(R.id.timeUnitButtonsTopRow).height //todo make it as resource and put this at layout xml init
    private val MAX_LAYOUT_HEIGHT = dpToPx(380) //in PX
    private val TEXT_TO_BUTTON_SIZE_RATIO = 12
    private val MIN_BUTTON_TEXT_SIZE = MIN_LAYOUT_HEIGHT / (5 * TEXT_TO_BUTTON_SIZE_RATIO) //in PX
    private val MAX_BUTTON_TEXT_SIZE = MAX_LAYOUT_HEIGHT / (5 * TEXT_TO_BUTTON_SIZE_RATIO)



    private val containerLayout: ViewGroup = activity.findViewById(R.id.buttonNormiesContainer)

    override fun setScrollPercent(percent: Float) { //todo needs to be an interface for every other one
        containerLayout.heightByLayoutParams = percentToValue(percent, MAX_LAYOUT_HEIGHT, MIN_LAYOUT_HEIGHT).toInt()
        setTextSizeForAllButtons(percent)
    }

    private fun setTextSizeForAllButtons(percent: Float) {
        for (i in 0..containerLayout.childCount) {
            val column = containerLayout.getChildAt(i)
            if (column is LinearLayout) {
                for (i in 0..column.childCount) {
                    val button = column.getChildAt(i)
                    if (button is Button) {
                        button.textSize = percentToValue(percent, MAX_BUTTON_TEXT_SIZE, MIN_BUTTON_TEXT_SIZE)
                    }
                }
            }
        }
    }

    init {
        setTextSizeForAllButtons(0f)
    }

}

