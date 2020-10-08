package com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout

import android.animation.ValueAnimator
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.symbol.Symbol
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.helpers.native_.PxPoint
import com.arealapps.timecalc.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.utils.swipeGestureHandler.SwipeGestureHandler
import com.arealapps.timecalc.utils.swipeGestureHandler.SwipeGestureHandlerImpl
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayout.Actions.*
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerBottomPart
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerBottomPartImpl
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerTopPart
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.parts.ButtonsContainerTopPartImpl


interface CalculatorButtonsElasticLayout : HoldsListeners<CalculatorButtonsElasticLayout.Listener>{
    interface Listener {
        fun actionButtonWasPressed(action: Actions)
        fun symbolButtonWasPressed(symbol: Symbol)
    }
    enum class Actions{ Equals, Backspace, Clear }
}


class CalculatorButtonsElasticLayoutImpl(
    private val activity: CalculatorActivity,
    private val listenersMgr: ListenersManager<CalculatorButtonsElasticLayout.Listener> = ListenersManager()
): CalculatorButtonsElasticLayout, HoldsListeners<CalculatorButtonsElasticLayout.Listener> by listenersMgr {


    private val buttonsContainerTopPart: ButtonsContainerTopPart = ButtonsContainerTopPartImpl(activity)
    private val buttonsContainerBottomPart: ButtonsContainerBottomPart = ButtonsContainerBottomPartImpl(activity)
    private val swipeGestureHandler: SwipeGestureHandler = createSwipeGestureHandler()



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

            buttonsContainerTopPart?.setScrollPercent(pct)
            buttonsContainerBottomPart?.setScrollPercent(pct)
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
        val backspaceButton: Button = activity.findViewById(R.id.calculator_actionButton_backspace)
        val equalsButton: Button = activity.findViewById(R.id.calculator_actionButton_equals)

        backspaceButton.setOnClickListener { listenersMgr.notifyAll { it.actionButtonWasPressed(Backspace) } }
        backspaceButton.setOnLongClickListener { listenersMgr.notifyAll { it.actionButtonWasPressed(Clear) }; true }
        equalsButton.setOnClickListener { listenersMgr.notifyAll { it.actionButtonWasPressed(Equals) } }
    }

    private fun onCalculatorSymbolButtonClick(buttonClicked: Button) {
        listenersMgr.notifyAll {
            val symbol = Symbol.charOf(buttonClicked.getTagAsChar())
            it.symbolButtonWasPressed(symbol)
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
    }
}