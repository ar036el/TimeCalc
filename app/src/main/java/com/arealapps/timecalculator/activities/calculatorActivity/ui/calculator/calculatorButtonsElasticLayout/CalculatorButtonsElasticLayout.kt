package com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout

import android.animation.ValueAnimator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.calculation_engine.symbol.Symbol
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalculator.helpers.native_.PxPoint
import com.arealapps.timecalculator.activities.calculatorActivity.CalculatorActivity
import com.arealapps.timecalculator.utils.swipeGestureHandler.SwipeGestureHandler
import com.arealapps.timecalculator.utils.swipeGestureHandler.SwipeGestureHandlerImpl
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayout.ButtonActionTypes.*
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerBottomPart
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerBottomPartImpl
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerTopPart
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerTopPartImpl
import com.arealapps.timecalculator.utils.VibrationManager


interface CalculatorButtonsElasticLayout : HoldsListeners<CalculatorButtonsElasticLayout.Listener>{
    var isClearButtonEnabled: Boolean
    var areButtonsClickHapticsEnabled: Boolean
    interface Listener {
        fun actionButtonWasPressed(action: ButtonActionTypes)
        fun symbolButtonWasPressed(symbol: Symbol)
    }
    enum class ButtonActionTypes{ Equals, Backspace, Clear }
}


class CalculatorButtonsElasticLayoutImpl(
    private val activity: CalculatorActivity,
    isClearButtonEnabled: Boolean,
    override var areButtonsClickHapticsEnabled: Boolean,
    private val vibrationManager: VibrationManager,
    private val listenersMgr: ListenersManager<CalculatorButtonsElasticLayout.Listener> = ListenersManager()
): CalculatorButtonsElasticLayout, HoldsListeners<CalculatorButtonsElasticLayout.Listener> by listenersMgr {


    override var isClearButtonEnabled = false //lateinit
        set(value) {
            field = value
            updateCleanButtonAbility()
        }

    private val buttonsContainerTopPart: ButtonsContainerTopPart = ButtonsContainerTopPartImpl(activity)
    private val buttonsContainerBottomPart: ButtonsContainerBottomPart = ButtonsContainerBottomPartImpl(activity)
    private val swipeGestureHandler: SwipeGestureHandler = createSwipeGestureHandler()


    private fun updateCleanButtonAbility() {
        val backspaceVisual = activity.findViewById<View>(R.id.calculator_actionButton_backspaceClear_backspaceVisual)
        val clearVisual = activity.findViewById<View>(R.id.calculator_actionButton_backspaceClear_clearVisual)

        clearVisual.visibility = if (isClearButtonEnabled) View.VISIBLE else View.INVISIBLE
        backspaceVisual.visibility = if (isClearButtonEnabled) View.INVISIBLE else View.VISIBLE
    }

    private fun createSwipeGestureHandler(): SwipeGestureHandler {
        return SwipeGestureHandlerImpl(
            activity,
            activity.findViewById(R.id.touchSurface),
            activity.findViewById(R.id.calculatorButtonsDrawerlikeLayout),
            SwipeGestureHandler.Bound.Static,
            SwipeGestureHandler.Bound.Range(buttonsContainerTopPart.minHeight, buttonsContainerTopPart.maxHeight),
            PxPoint(0f, buttonsContainerTopPart.minHeight),
            false,
            1.1f
        )
    }

    private val swipeGestureHandlerListener = object : SwipeGestureHandler.Listener {
        override fun pointHasChanged(
            subject: SwipeGestureHandler,
            lastPoint: PxPoint,
            newPoint: PxPoint
        ) {
            val pct = subject.toYPercent(newPoint.y)

            buttonsContainerTopPart.setScrollPercent(pct)
            buttonsContainerBottomPart.setScrollPercent(pct)
        }

        override fun swipeStateHasChanged(
            subject: SwipeGestureHandler,
            state: SwipeGestureHandler.SwipeState,
            lastState: SwipeGestureHandler.SwipeState
        ) {
            if (state == SwipeGestureHandler.SwipeState.Static) {
                if (swipeGestureHandler.currentYPercent > 0f && swipeGestureHandler.currentYPercent <= 0.5f) {
                    collapseTimeUnitButtonsDrawer()
                } else if (swipeGestureHandler.currentYPercent > 0.5f && swipeGestureHandler.currentYPercent < 1f) {
                    expandTimeUnitButtonsDrawer()
                }
            }
        }
    }

    private fun expandTimeUnitButtonsDrawer() {
        setTimeUnitButtonsDrawerTo(1f)
    }
    private fun collapseTimeUnitButtonsDrawer() {
        setTimeUnitButtonsDrawerTo(0f)
    }

    private fun setTimeUnitButtonsDrawerTo(value: Float) {
        val SWIPE_DURATION = 150L //in millis

        val currentDrawerScrollPercent = swipeGestureHandler.toYPercent(swipeGestureHandler.currentPoint.y)
        val valueAnimator = ValueAnimator.ofFloat(currentDrawerScrollPercent, value)
        valueAnimator.apply {
            addUpdateListener {animation ->
                swipeGestureHandler.updatePointFromPercent(null, animation.animatedValue as Float)
            }
            duration = SWIPE_DURATION/2
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
    }

    private val swipeGestureHandlerAdditionalGestureListener = object: GestureDetector.SimpleOnGestureListener() {
        override fun onSingleTapUp(e: MotionEvent?): Boolean {
            //todo probably remove all this. hint is lame and collides with buttons click events
//            val swipeGestureHandler = swipeGestureHandler ?: return true
//            if (swipeGestureHandler.toYPercent(swipeGestureHandler.currentPoint.y) == 0f) {
//                animateSwipeGestureHint()
//            }
            return true
        }
    }

    private fun setActionButtonsClickListeners() {
        val backspaceClearButton: Button = activity.findViewById(R.id.calculator_actionButton_backspaceClear)
        val equalsButton: Button = activity.findViewById(R.id.calculator_actionButton_equals)

        backspaceClearButton.setOnClickListener { onCalculatorSymbolButtonClick(it, Backspace) }
        backspaceClearButton.setOnLongClickListener { onCalculatorSymbolButtonClick(it, Clear); true }
        equalsButton.setOnClickListener { onCalculatorSymbolButtonClick(it, Equals) }
    }

    private fun onCalculatorSymbolButtonClick(button: View, actionType: CalculatorButtonsElasticLayout.ButtonActionTypes) {
        tryToVibrateClick(button)
        listenersMgr.notifyAll { it.actionButtonWasPressed(actionType) }
    }

    private fun onCalculatorSymbolButtonClick(button: Button) {
        tryToVibrateClick(button)
        listenersMgr.notifyAll {
            val symbol = Symbol.charOf(button.getTagAsChar())
            it.symbolButtonWasPressed(symbol)
        }
    }

    private fun tryToVibrateClick(button: View) {
        if (areButtonsClickHapticsEnabled) {
            vibrationManager.vibrateAsSimpleClick(button)
        }
    }

    private fun View.getTagAsChar(): Char {
        val asString = tag.toString()
        if (asString.length != 1) {
            throw InternalError()
        }
        return asString[0]
    }

    init {
        swipeGestureHandler.addListener(swipeGestureHandlerListener)
        swipeGestureHandler.additionalGestureListenerForTouchSubject = swipeGestureHandlerAdditionalGestureListener

        setActionButtonsClickListeners()
        activity.doOnCalculatorSymbolButtonClick = ::onCalculatorSymbolButtonClick
        this.isClearButtonEnabled = isClearButtonEnabled
    }
}