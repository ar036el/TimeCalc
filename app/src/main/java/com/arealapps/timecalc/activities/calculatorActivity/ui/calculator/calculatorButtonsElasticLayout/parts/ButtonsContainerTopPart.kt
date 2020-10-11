package com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts

import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import androidx.constraintlayout.widget.ConstraintLayout
import com.arealapps.timecalc.activities.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.R
import com.arealapps.timecalc.helpers.android.heightByLayoutParams
import com.arealapps.timecalc.helpers.native_.checkIfPercentIsLegal
import com.arealapps.timecalc.helpers.native_.percentToValue


interface ButtonsContainerTopPart {
    val minHeight: Float
    val maxHeight: Float
    fun setScrollPercent(percent: Float)
}

class ButtonsContainerTopPartImpl(activity: CalculatorActivity, initScrollPercent: Float = 0f) :
    ButtonsContainerTopPart {
    private val topRow = TopRow(activity)
    private val bottomRow = BottomRow(activity)

    private val rootContainer: ViewGroup = activity.findViewById(R.id.timeUnitButtonsExpandableLayout)
    private val firstRowContainer: FrameLayout = activity.findViewById(R.id.timeUnitButtonsTopRowContainer)
    private val secondRowContainer: FrameLayout = activity.findViewById(R.id.timeUnitButtonsBottomRowContainer)
    private val gestureBar: View = activity.findViewById(R.id.timeUnitButtonsBottomGestureBar)

    override val minHeight = rootContainer.height.toFloat()
    override val maxHeight = rootContainer.height.toFloat() + firstRowContainer.height

    override fun setScrollPercent(percent: Float) {
        rootContainer.heightByLayoutParams = percentToValue(percent, minHeight, maxHeight).toInt()
        gestureBar.y = percentToValue(percent, minHeight - gestureBar.height, maxHeight - gestureBar.height)

        topRow.setScrollPercent(percent)
        bottomRow.setScrollPercent(percent)
    }

    init {
        secondRowContainer.y = firstRowContainer.height.toFloat()
        rootContainer.heightByLayoutParams = minHeight.toInt()
        setScrollPercent(initScrollPercent)
    }


    private class TopRow(activity: CalculatorActivity) {
        private val container: ConstraintLayout = activity.findViewById(R.id.timeUnitButtonsTopRow)
        private val hiddenButtonRight: Button = activity.findViewById(R.id.timeUnitButtonMillisecond)
        private val hiddenButtonLeft: Button = activity.findViewById(R.id.timeUnitButtonDay1)

        private val buttonsContainerMinX: Float
        private val buttonsContainerMaxX: Float
        private val hiddenButtonRightMinX: Float
        private val hiddenButtonRightMaxX: Float

        fun setScrollPercent(percent: Float) {
            container.x = percentToValue(percent, buttonsContainerMinX, buttonsContainerMaxX)
            hiddenButtonRight.x = percentToValue(percent, hiddenButtonRightMinX, hiddenButtonRightMaxX)
            hiddenButtonLeft.alpha = 1 - percent
        }

        init {
            val button3 = activity.findViewById<Button>(R.id.timeUnitButtonMinute)
            val button4 = activity.findViewById<Button>(R.id.timeUnitButtonSecond)
            val buttonWidth = button3.width
            val distanceBetweenButtons = button4.x - button3.x - buttonWidth

            hiddenButtonRight.visibility = View.VISIBLE
            hiddenButtonRight.x = button4.x + buttonWidth + distanceBetweenButtons
            hiddenButtonRight.y = button4.y

            buttonsContainerMinX = container.x
            buttonsContainerMaxX = container.x - buttonWidth - distanceBetweenButtons
            hiddenButtonRightMinX = hiddenButtonRight.x
            hiddenButtonRightMaxX = hiddenButtonRight.x - buttonWidth - distanceBetweenButtons
        }
    }

    private class BottomRow(activity: CalculatorActivity) {
        private val buttonsContainer: ConstraintLayout = activity.findViewById(R.id.timeUnitButtonsBottomRow)
        private val minX: Float
        private val maxX: Float

        fun setScrollPercent(percent: Float) {
            checkIfPercentIsLegal(percent)
//                buttonsContainer.x = toPercentedValue(minX, maxX, percent)
            buttonsContainer.alpha = percent
        }

        init {
            minX = buttonsContainer.width.toFloat()
            maxX = buttonsContainer.x
//                buttonsContainer.x = minX
        }
    }
}
