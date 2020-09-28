package el.arn.timecalc.calculation_engine

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.TextView
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerBottomPart
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerTopPart
import el.arn.timecalc.calculation_engine.expression.*
import el.arn.timecalc.mainActivity.custom_views.CustomEditText
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.mainActivity.ui.TimeResultLayoutManager
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandler


interface CalculatorCoordinator {
    fun setActivityComponents(activity: Activity, customEditText: CustomEditText, buttonsContainerTopPart: ButtonsContainerTopPart, buttonsContainerBottomPart: ButtonsContainerBottomPart, swipeGestureHandler: SwipeGestureHandler, timeResultLayoutManager: TimeResultLayoutManager)
    val expressionBuilder: ExpressionBuilder //todo remove after
    val expressionToStringConverter: ExpressionToStringConverter //todo remove after
}

class CalculatorCoordinatorImpl: CalculatorCoordinator { //todo weird name

    override val expressionBuilder: ExpressionBuilder = ExpressionBuilderImpl()
    override val expressionToStringConverter: ExpressionToStringConverter = ExpressionToStringConverterImpl(expressionBuilder, false, true)

    private var activity: Activity? = null
    private var customEditText: CustomEditText? = null
    private var buttonsContainerTopPart: ButtonsContainerTopPart? = null
    private var buttonsContainerBottomPart: ButtonsContainerBottomPart? = null
    private var swipeGestureHandler: SwipeGestureHandler? = null
    private var timeResultLayoutManager: TimeResultLayoutManager? = null


    override fun setActivityComponents(activity: Activity, customEditText: CustomEditText, buttonsContainerTopPart: ButtonsContainerTopPart, buttonsContainerBottomPart: ButtonsContainerBottomPart, swipeGestureHandler: SwipeGestureHandler, timeResultLayoutManager: TimeResultLayoutManager) {
        this.activity = activity

        this.customEditText?.listenersHolder?.removeListener(expressionEditTextListener)
        this.customEditText = customEditText
        customEditText.listenersHolder.addListener(expressionEditTextListener)

        this.buttonsContainerTopPart = buttonsContainerTopPart
        this.buttonsContainerBottomPart = buttonsContainerBottomPart
        //todo setPercent to both of them

        this.swipeGestureHandler?.removeListener(swipeGestureHandlerListener)
        this.swipeGestureHandler?.additionalGestureListenerForTouchSubject = null
        this.swipeGestureHandler = swipeGestureHandler
        swipeGestureHandler.addListener(swipeGestureHandlerListener)
        swipeGestureHandler.additionalGestureListenerForTouchSubject = swipeGestureHandlerAdditionalGestureListener
        
        this.timeResultLayoutManager = timeResultLayoutManager
    }

    private val expressionEditTextListener = object: CustomEditText.Listener {
        override fun onSelectionChanged(subject: CustomEditText, selectionStart: Int, selectionEnd: Int) {
            val fixedSelectionStart = fixSelectionPositionByConvertingToExpressionAndBackToString(selectionStart)
            val fixedSelectionEnd = fixSelectionPositionByConvertingToExpressionAndBackToString(selectionEnd)

            if (fixedSelectionStart != selectionStart || fixedSelectionEnd != selectionEnd) {
                subject.setSelection(fixedSelectionStart, fixedSelectionEnd)
            }
        }
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
            val swipeGestureHandler = swipeGestureHandler ?: return
            if (state == SwipeGestureHandler.SwipeState.Static) {
                if (swipeGestureHandler.currentYPercent > 0f && swipeGestureHandler.currentYPercent <= 0.5f) {
                    closeTimeUnitsDrawer()
                } else if (swipeGestureHandler.currentYPercent > 0.5f && swipeGestureHandler.currentYPercent < 1f) {
                    openTimeUnitsDrawer()
                }
            }
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


    private val expressionBuilderListener = object: ExpressionBuilder.Listener {
        override fun expressionWasCleared() {
            customEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
        }

        override fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {
            customEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
            customEditText?.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index+1))
        }

        override fun exprTokenWasReplacedAt(token: ExpressionToken, replaced: ExpressionToken, index: Int) {
            customEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
            customEditText?.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index+1))
        }

        override fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {
            customEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
            customEditText?.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index))
        }
    }

    private fun openTimeUnitsDrawer() {
        setTimeUnitsDrawerTo(1f)
    }
    private fun closeTimeUnitsDrawer() {
        setTimeUnitsDrawerTo(0f)
    }

    private fun setTimeUnitsDrawerTo(value: Float) {
        val SWIPE_DURATION = 150L //in millis

        val swipeGestureHandler = swipeGestureHandler ?: return

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

    private fun animateSwipeGestureHint() {
        val MIN_VALUE = 0f
        val MAX_VALUE = 0.35f
        val SWIPE_DURATION = 300L //in millis

        val swipeGestureHandler = swipeGestureHandler ?: return

        swipeGestureHandler.updatePoint(swipeGestureHandler.minXPos, swipeGestureHandler.minYPos)
        val valueAnimator = ValueAnimator.ofFloat(MIN_VALUE, MAX_VALUE)
        valueAnimator.apply {
            addUpdateListener {animation ->
                swipeGestureHandler.updatePointFromPercent(null, animation.animatedValue as Float)
            }
            duration = SWIPE_DURATION/2
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }

        val animateSwipeGestureHintListener = object : Animator.AnimatorListener {
            override fun onAnimationEnd(animation: Animator?) {
                val valueAnimator = ValueAnimator.ofFloat(MAX_VALUE, MIN_VALUE)
                valueAnimator.apply {
                    addUpdateListener { animation ->
                        swipeGestureHandler.updatePointFromPercent(null, animation.animatedValue as Float)
                    }
                    duration = SWIPE_DURATION/2
                    interpolator = AccelerateDecelerateInterpolator()
                    start()
                }
            }
            override fun onAnimationStart(animation: Animator?) {}; override fun onAnimationCancel(animation: Animator?) {}; override fun onAnimationRepeat(animation: Animator?) {}
        }
        valueAnimator.addListener(animateSwipeGestureHintListener)
        valueAnimator.start()

    }

    private fun fixSelectionPositionByConvertingToExpressionAndBackToString(selectionPosition: Int): Int {
        return expressionToStringConverter.expressionIndexToStringIndex(
            expressionToStringConverter.stringIndexToExpressionIndex(selectionPosition))
    }

    init {
        expressionBuilder.addListener(expressionBuilderListener)

    }

}