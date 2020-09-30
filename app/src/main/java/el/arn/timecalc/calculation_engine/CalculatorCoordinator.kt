package el.arn.timecalc.calculation_engine

import android.animation.Animator
import android.animation.ValueAnimator
import android.app.Activity
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Button
import android.widget.TextView
import el.arn.timecalc.R
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerBottomPart
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerTopPart
import el.arn.timecalc.calculation_engine.expression.*
import el.arn.timecalc.calculation_engine.result.ResultBuilder
import el.arn.timecalc.calculation_engine.result.ResultBuilderImpl
import el.arn.timecalc.calculation_engine.symbol.Symbol
import el.arn.timecalc.mainActivity.custom_views.CustomEditText
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.mainActivity.ui.ResultLayoutManager
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandler
import el.arn.timecalc.organize_later.reveal_maker.RevealMakerImpl
import el.arn.timecalc.rootUtils


interface CalculatorCoordinator {
    fun setActivityComponents(activity: Activity, customEditText: CustomEditText, buttonsContainerTopPart: ButtonsContainerTopPart, buttonsContainerBottomPart: ButtonsContainerBottomPart, swipeGestureHandler: SwipeGestureHandler, resultLayoutManager: ResultLayoutManager)
    fun symbolButtonPressed(buttonView: Button)
}

class CalculatorCoordinatorImpl: CalculatorCoordinator { //todo weird name

    private val expressionBuilder: ExpressionBuilder = ExpressionBuilderImpl()
    private val expressionToStringConverter: ExpressionToStringConverter = ExpressionToStringConverterImpl(expressionBuilder, false, true)
    private val resultBuilder: ResultBuilder = ResultBuilderImpl(rootUtils.timeConverter, TimeExpressionFactory(rootUtils.configManager.getTimeExpressionConfig()))


    override fun symbolButtonPressed(buttonView: Button) {
        insertSymbol(buttonView.getTagAsChar())
    }

    private fun setActionButtonsClickListeners(activity: Activity) {
        val backspaceButton: Button = activity.findViewById(R.id.calculator_actionButton_backspace)
        backspaceButton.apply {
            backspaceButton.setOnClickListener { backspace() }
            setOnLongClickListener { clear(); true }
        }
        val equalsButton: Button = activity.findViewById(R.id.calculator_actionButton_equals)
        equalsButton.setOnClickListener {
            crapMakeBubbleReveal(activity)
            setOfficialResult(); true }
    }

    private fun crapMakeBubbleReveal(activity: Activity) {
        RevealMakerImpl(activity.findViewById(R.id.RevealMakerDrawingSurface),
            500,
            500).startBubbleReveal(
            PxPoint(
                activity.findViewById<ViewGroup>(R.id.RevealMakerDrawingSurface).width.toFloat(),
                activity.findViewById<ViewGroup>(R.id.RevealMakerDrawingSurface).height.toFloat()
            ),
            PxPoint(0f,0f)
        )
    }

    private fun crapMakeVerticalRectReveal(activity: Activity) {
        RevealMakerImpl(activity.findViewById(R.id.RevealMakerDrawingSurface),
            250,
            500).startVerticalRectReveal(
            0f,
            activity.findViewById<ViewGroup>(R.id.RevealMakerDrawingSurface).width.toFloat(),
            activity.findViewById<ViewGroup>(R.id.RevealMakerDrawingSurface).height.toFloat(),
            0f
        )
    }
    
    private fun insertSymbol(symbolAsChar: Char) {
        val selectedSymbol = Symbol.charOf(symbolAsChar)
        val currentLocation = gerExpressionCurrentLocationByExpressionEditText()
        expressionBuilder.insertSymbolAt(selectedSymbol, currentLocation)
    }

    private fun clear() {
        expressionBuilder.clearAll()
        resultLayoutManager?.updateResult(null)
    }
    
    private fun backspace() {
        val currentLocation = gerExpressionCurrentLocationByExpressionEditText()
        expressionBuilder.backspaceSymbolFrom(currentLocation)
    }

    private fun setTempResult() {
        val officialResult = resultBuilder.getTempResult(expressionBuilder.getExpression())
        resultLayoutManager?.updateResult(officialResult)
    }

    private fun setOfficialResult() {
        val officialResult = resultBuilder.getOfficialResult(expressionBuilder.getExpression())
        resultLayoutManager?.updateResult(officialResult)
    }


    private fun gerExpressionCurrentLocationByExpressionEditText() = expressionToStringConverter.stringIndexToExpressionIndex(expressionEditText!!.selectionStart)

    
    private fun View.getTagAsChar(): Char {
        val asString = tag.toString()
        if (asString.length != 1) {
            throw InternalError()
        }
        return asString[0]
    }

    private var activity: Activity? = null
    private var expressionEditText: CustomEditText? = null
    private var buttonsContainerTopPart: ButtonsContainerTopPart? = null
    private var buttonsContainerBottomPart: ButtonsContainerBottomPart? = null
    private var swipeGestureHandler: SwipeGestureHandler? = null
    private var resultLayoutManager: ResultLayoutManager? = null


    override fun setActivityComponents(activity: Activity, customEditText: CustomEditText, buttonsContainerTopPart: ButtonsContainerTopPart, buttonsContainerBottomPart: ButtonsContainerBottomPart, swipeGestureHandler: SwipeGestureHandler, resultLayoutManager: ResultLayoutManager) {
        this.activity = activity

        this.expressionEditText?.listenersHolder?.removeListener(expressionEditTextListener)
        this.expressionEditText = customEditText
        customEditText.listenersHolder.addListener(expressionEditTextListener)

        this.buttonsContainerTopPart = buttonsContainerTopPart
        this.buttonsContainerBottomPart = buttonsContainerBottomPart
        //todo setPercent to both of them

        this.swipeGestureHandler?.removeListener(swipeGestureHandlerListener)
        this.swipeGestureHandler?.additionalGestureListenerForTouchSubject = null
        this.swipeGestureHandler = swipeGestureHandler
        swipeGestureHandler.addListener(swipeGestureHandlerListener)
        swipeGestureHandler.additionalGestureListenerForTouchSubject = swipeGestureHandlerAdditionalGestureListener
        
        this.resultLayoutManager = resultLayoutManager


        setActionButtonsClickListeners(activity)
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
        override fun expressionWasChanged(subject: ExpressionBuilder) {
            setTempResult()
        }

        override fun expressionWasCleared() {
            expressionEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
        }

        override fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {
            expressionEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
            expressionEditText?.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index+1))
        }

        override fun exprTokenWasReplacedAt(token: ExpressionToken, replaced: ExpressionToken, index: Int) {
            expressionEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
            expressionEditText?.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index+1))
        }

        override fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {
            expressionEditText?.setText(expressionToStringConverter.expressionToString(), TextView.BufferType.EDITABLE)
            expressionEditText?.setSelection(expressionToStringConverter.expressionIndexToStringIndex(index))
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