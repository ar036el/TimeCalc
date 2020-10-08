package com.arealapps.timecalc.calculation_engine

import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.expression.*
import com.arealapps.timecalc.calculation_engine.result.ErrorResult
import com.arealapps.timecalc.calculation_engine.result.Result
import com.arealapps.timecalc.calculation_engine.result.ResultBuilder
import com.arealapps.timecalc.calculation_engine.result.ResultBuilderImpl
import com.arealapps.timecalc.calculation_engine.symbol.Symbol
import com.arealapps.timecalc.helpers.android.dimenFromResAsPx
import com.arealapps.timecalc.helpers.android.floatFromRes
import com.arealapps.timecalc.helpers.native_.PxPoint
import com.arealapps.timecalc.helpers.native_.percentToValue
import com.arealapps.timecalc.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.calculatorActivity.ui.calculator.expressionInputText.ExpressionEditTextImpl
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayout
import com.arealapps.timecalc.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayoutImpl
import com.arealapps.timecalc.calculatorActivity.ui.calculator.ResultLayout.ResultLayout
import com.arealapps.timecalc.calculatorActivity.ui.calculator.ResultLayout.ResultLayoutImpl
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.organize_later.errorIf
import com.arealapps.timecalc.utils.RevealManager
import com.arealapps.timecalc.utils.RevealManagerImpl
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.PercentAnimation
import kotlin.math.min

interface CalculatorCoordinator: HoldsListeners<CalculatorCoordinator.Listener> {
    fun loadExpression(expressionAsString: String)
    interface Listener {
        fun calculationPerformed(expression: Expression, result: Result)
    }
}

class CalculatorCoordinatorImpl(
    private val activity: CalculatorActivity,
    private val listenersMgr: ListenersManager<CalculatorCoordinator.Listener> = ListenersManager(),
) : CalculatorCoordinator, HoldsListeners<CalculatorCoordinator.Listener> by listenersMgr {

    private val BUBBLE_REVEAL_EXPAND_DURATION = 400L
    private val BUBBLE_REVEAL_FADE_DURATION = 350L
    private val BUBBLE_REVEAL_DELAY_BEFORE_FADE = 50L

    private val RECT_REVEAL_EXPAND_DURATION = 300L
    private val RECT_REVEAL_FADE_DURATION = 500L
    private val RESULT_REVEAL_DURATION = 500L

    private val expressionBuilder: ExpressionBuilder = ExpressionBuilderImpl()
    private val resultBuilder: ResultBuilder = ResultBuilderImpl(rootUtils.timeConverter,
        TimeExpressionFactory(rootUtils.configManager.getTimeExpressionConfig()))

    private var calculatorButtonsElasticLayout: CalculatorButtonsElasticLayout =
        CalculatorButtonsElasticLayoutImpl(activity)
    private val expressionInputText = ExpressionEditTextImpl(activity, expressionBuilder)
    private val revealManager: RevealManager =
        RevealManagerImpl(activity.findViewById(R.id.RevealManagerDrawingSurface))
    private val resultLayout: ResultLayout = createResultLayoutManager()


    private enum class States { Input, Animation, Result }

    private var state: States = States.Input

    override fun loadExpression(expressionAsString: String) {
        when (state) {
            States.Result -> applyStateToInput()
            States.Animation -> return //todo cancel all animation set state
        }
        clearExpressionAndResult()
        rootUtils.expressionToStringConverter.buildExpressionFromString(expressionBuilder, expressionAsString)
    }

    private fun symbolButtonPressed(symbol: Symbol) {
        if (state == States.Animation) {
            return
        }
        if (state == States.Result) {
            applyStateToInput()
        }
        insertSymbol(symbol)
    }

    private fun clearWasPressed() {
        when (state) {
            States.Animation -> return
            States.Result -> applyStateToInput()
            States.Input -> clearExpressionAndResult()
        }
    }

    private fun backspaceWasPressed() {
        when (state) {
            States.Animation -> return
            States.Result -> applyStateToInput()
            States.Input -> {
                val currentLocation = expressionInputText.getExpressionBuilderIndexByInputTextLocation()
                expressionBuilder.backspaceSymbolFrom(currentLocation)
            }
        }
    }

    private fun equalsWasPressed() {
        if (state == States.Input) {
            val expression = expressionBuilder.getExpression()
            val officialResult = resultBuilder.getOfficialResult(expression) ?: return
            if (officialResult is ErrorResult) {
                startErrorResultRevealAnimation(officialResult) {
                    state = States.Result
                    resultLayout.areGesturesEnabled = true
                }
            } else {
                expandOfficialResult(officialResult, true) {
                    state = States.Result
                    resultLayout.areGesturesEnabled = true
                    listenersMgr.notifyAll { it.calculationPerformed(expression, officialResult) }
                }
            }
        }
    }

    private fun clearExpressionAndResult() {
        expressionBuilder.clearAll()
        resultLayout.updateResult(null)
    }

    private fun insertSymbol(symbol: Symbol) {
        val currentLocation = expressionInputText.getExpressionBuilderIndexByInputTextLocation()
        expressionBuilder.insertSymbolAt(symbol, currentLocation)
    }

    private fun setTempResult() {
        val officialResult = resultBuilder.getTempResult(expressionBuilder.getExpression())
        resultLayout.updateResult(officialResult)
    }


    private val expressionBuilderListener = object : ExpressionBuilder.Listener {
        override fun expressionWasChanged(subject: ExpressionBuilder) {
            setTempResult()
        }
    }


    private val calculatorButtonsElasticLayoutListener =
        object : CalculatorButtonsElasticLayout.Listener {
            override fun actionButtonWasPressed(action: CalculatorButtonsElasticLayout.Actions) {
                when (action) {
                    CalculatorButtonsElasticLayout.Actions.Equals -> equalsWasPressed()
                    CalculatorButtonsElasticLayout.Actions.Backspace -> backspaceWasPressed()
                    CalculatorButtonsElasticLayout.Actions.Clear -> clearWasPressed()
                }
            }

            override fun symbolButtonWasPressed(symbol: Symbol) {
                symbolButtonPressed(symbol)
            }

        }


    private fun addListeners() {
        expressionBuilder.addListener(expressionBuilderListener)
        calculatorButtonsElasticLayout.addListener(calculatorButtonsElasticLayoutListener)
    }

    private var currentPercentAnimation: PercentAnimation? = null
        set(value) {
            if (field?.state == PercentAnimation.States.Running) {
                throw InternalError()
            }
            field = value
        }

    private fun startErrorResultRevealAnimation(officialResult: ErrorResult, doOnFinish: () -> Unit) {
        state = States.Animation
        expressionInputText.isEnabled = false
        startBubbleReveal(
            RevealManager.RevealStyles.Error,
            { expandOfficialResult(officialResult, false, null) },
            {
                state = States.Result
                doOnFinish()
            }
        )
    }

    private fun startBubbleReveal(
        revealStyle: RevealManager.RevealStyles,
        doWhenFinishedExpanding: () -> Unit,
        doWhenFinishedFading: () -> Unit,
    ) {
        val drawingSurface = activity.findViewById<ViewGroup>(R.id.RevealManagerDrawingSurface)
        val listener = object : RevealManager.Listener {
            override fun stateHasChanged(
                subject: RevealManager,
                oldState: RevealManager.States,
                newState: RevealManager.States,
            ) {
                when (oldState) {
                    RevealManager.States.Inactive -> Unit
                    RevealManager.States.IsExpanding -> doWhenFinishedExpanding()
                    RevealManager.States.IsFading -> {
                        doWhenFinishedFading()
                        revealManager.removeListener(this)
                    }
                }
            }
        }

        revealManager.addListener(listener)
        revealManager.startBubbleReveal(
            PxPoint(drawingSurface.width.toFloat(), drawingSurface.height.toFloat()),
            PxPoint(0f, 0f),
            BUBBLE_REVEAL_EXPAND_DURATION,
            BUBBLE_REVEAL_DELAY_BEFORE_FADE,
            BUBBLE_REVEAL_FADE_DURATION,
            revealStyle
        )
    }

    private fun expandOfficialResult(officialResult: Result, withAnimation: Boolean, doOnFinish: (() -> Unit)?) {


        resultLayout.updateResult(officialResult) {

            if (withAnimation) {
                state = States.Animation
                expressionInputText.isEnabled = false

                currentPercentAnimation = PercentAnimation(
                    RESULT_REVEAL_DURATION,
                    AccelerateDecelerateInterpolator(),
                    { percent ->
                        expressionInputText.abilityPercentage = 1f - percent
                        resultLayout.abilityPercentage = percent
                     },
                    { doOnFinish?.invoke() }
                )
                currentPercentAnimation!!.start()
            } else {
                expressionInputText.abilityPercentage = 0f
                resultLayout.abilityPercentage = 1f
                doOnFinish?.invoke()
            }
        }
    }

    //todo search all println and remove them all

    private fun applyStateToInput() {
        if (state == States.Result) {
            clearExpressionAndResult()
        }
        errorIf(null) {state == States.Animation}

        state = States.Input

        expressionInputText.isEnabled = true
        expressionInputText.abilityPercentage = 1f
        resultLayout.areGesturesEnabled = false
        resultLayout.abilityPercentage = 0f
    }

    private fun createResultLayoutManager(): ResultLayout {
        return ResultLayoutImpl(
            activity.findViewById(R.id.resultLayout),
            activity.findViewById(R.id.resultLayoutContainer),
            null,
            rootUtils.configManager.getConfigForTimeResultLayoutManager(),
            activity.findViewById<View>(R.id.resultLayout).width.toFloat(),
            dimenFromResAsPx(R.dimen.resultLayout_minHeight),
            dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled)
        )
    }

    init {
        addListeners()
        applyStateToInput()
    }

}