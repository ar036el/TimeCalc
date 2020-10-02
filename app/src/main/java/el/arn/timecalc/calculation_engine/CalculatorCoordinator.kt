package el.arn.timecalc.calculation_engine

import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import el.arn.timecalc.R
import el.arn.timecalc.calculation_engine.expression.*
import el.arn.timecalc.calculation_engine.result.ErrorResult
import el.arn.timecalc.calculation_engine.result.Result
import el.arn.timecalc.calculation_engine.result.ResultBuilder
import el.arn.timecalc.calculation_engine.result.ResultBuilderImpl
import el.arn.timecalc.calculation_engine.symbol.Symbol
import el.arn.timecalc.helpers.android.dimenFromResAsPx
import el.arn.timecalc.helpers.android.floatFromRes
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.helpers.native_.percentToValue
import el.arn.timecalc.calculatorActivity.CalculatorActivity
import el.arn.timecalc.calculatorActivity.ui.expressionInputText.ExpressionEditTextImpl
import el.arn.timecalc.calculatorActivity.ui.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayout
import el.arn.timecalc.calculatorActivity.ui.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayoutImpl
import el.arn.timecalc.calculatorActivity.ui.ResultLayoutManager.ResultLayoutManager
import el.arn.timecalc.utils.RevealManager
import el.arn.timecalc.utils.RevealManagerImpl
import el.arn.timecalc.rootUtils
import el.arn.timecalc.utils.PercentAnimation
import kotlin.math.min

interface CalculatorCoordinator {
    //???
}

class CalculatorCoordinatorImpl(
    private val activity: CalculatorActivity,
) : CalculatorCoordinator { //todo weird name

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
    private val resultLayoutManager: ResultLayoutManager = createResultLayoutManager()


    private enum class States { Input, Animation, Result }

    private lateinit var state: States


    fun symbolButtonPressed(symbol: Symbol) {
        if (state == States.Animation) {
            return
        }
        if (state == States.Result) {
            setStateToInputFromResult()
        }
        insertSymbol(symbol)
    }

    private fun clearWasPressed() {
        when (state) {
            States.Animation -> return
            States.Result -> setStateToInputFromResult()
            States.Input -> clearExpressionAndResult()
        }
    }

    private fun backspaceWasPressed() {
        when (state) {
            States.Animation -> return
            States.Result -> setStateToInputFromResult()
            States.Input -> {
                val currentLocation = expressionInputText.getExpressionBuilderIndexByInputTextLocation()
                expressionBuilder.backspaceSymbolFrom(currentLocation)
            }
        }
    }

    private fun equalsWasPressed() {
        if (state == States.Input) {
            val officialResult = resultBuilder.getOfficialResult(expressionBuilder.getExpression()) ?: return
            if (officialResult is ErrorResult) {
                startErrorResultRevealAnimation(officialResult) {
                    resultLayoutManager.areGesturedEnabled = true
                }
            } else {
                expandOfficialResult(officialResult, true) {
                    state = States.Result
                    resultLayoutManager.areGesturedEnabled = true
                }
            }
        }
    }

    private fun clearExpressionAndResult() {
        expressionBuilder.clearAll()
        resultLayoutManager.updateResult(null)
    }

    private fun insertSymbol(symbol: Symbol) {
        val currentLocation = expressionInputText.getExpressionBuilderIndexByInputTextLocation()
        expressionBuilder.insertSymbolAt(symbol, currentLocation)
    }

    private fun setTempResult() {
        val officialResult = resultBuilder.getTempResult(expressionBuilder.getExpression())
        resultLayoutManager.updateResult(officialResult)
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


        resultLayoutManager.updateResult(officialResult) {

            if (withAnimation) {
                state = States.Animation
                expressionInputText.isEnabled = false

                currentPercentAnimation = PercentAnimation(
                    RESULT_REVEAL_DURATION,
                    AccelerateDecelerateInterpolator(),
                    { percent ->
                        expressionInputText.abilityPercentage = 1f - percent
                        resultLayoutManager.setAbilityPercentage(percent)
                     },
                    { doOnFinish?.invoke() }
                )
                currentPercentAnimation!!.start()
            } else {
                expressionInputText.abilityPercentage = 0f
                resultLayoutManager.setAbilityPercentage(1f)
                doOnFinish?.invoke()
            }
        }
    }

    private fun ResultLayoutManager.setAbilityPercentage(percent: Float) {
        resultLayoutManager.maxHeight = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), min(dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled), resultLayoutManager.actualMaxHeightForCurrentResult))
        resultLayoutManager.alpha = percentToValue(percent, floatFromRes(R.dimen.calculatorDisplayComponentAlpha_disabled), floatFromRes(R.dimen.calculatorDisplayComponentAlpha_enabled))
        resultLayoutManager.containerHeight = percentToValue(percent, dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyDisabled), dimenFromResAsPx(R.dimen.resultLayout_maxHeight_fullyEnabled)).toInt()

    }

    //todo search all println and remove them all

    private fun setStateToInputFromResult() {
        if (state != States.Result) {
            throw InternalError()
        }
        clearExpressionAndResult()
        setStateToInput()
    }

    private fun setStateToInput() {
        state = States.Input

        expressionInputText.isEnabled = true
        expressionInputText.abilityPercentage = 1f

        resultLayoutManager.areGesturedEnabled = false
        resultLayoutManager.setAbilityPercentage(0f)
    }

    private fun createResultLayoutManager(): ResultLayoutManager {
        return ResultLayoutManager(
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
        setStateToInput()
    }

}