package com.arealapps.timecalc.calculation_engine.calculatorCoordinator

import android.view.View
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.calcAnimation.CalculatorAnimation
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.calcAnimation.ClearAnimation
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.calcAnimation.ErrorResultRevealAnimation
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.calcAnimation.NormalResultRevealAnimation
import com.arealapps.timecalc.calculation_engine.expression.Expression
import com.arealapps.timecalc.calculation_engine.expression.ExpressionBuilder
import com.arealapps.timecalc.calculation_engine.expression.ExpressionBuilderImpl
import com.arealapps.timecalc.calculation_engine.result.ErrorResult
import com.arealapps.timecalc.calculation_engine.result.Result
import com.arealapps.timecalc.calculation_engine.result.ResultBuilder
import com.arealapps.timecalc.calculation_engine.result.ResultBuilderImpl
import com.arealapps.timecalc.calculation_engine.symbol.Symbol
import com.arealapps.timecalc.activities.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayout
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.calculatorButtonsElasticLayout.CalculatorButtonsElasticLayoutImpl
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.expressionInputText.ExpressionLayout
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.expressionInputText.ExpressionLayoutImpl
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayoutImpl
import com.arealapps.timecalc.appRoot
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.helpers.native_.notEquals
import com.arealapps.timecalc.helpers.native_.or
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.RevealManager
import com.arealapps.timecalc.utils.RevealManagerImpl
import com.arealapps.timecalc.utils.preferences_managers.parts.Preference


interface CalculatorCoordinator: HoldsListeners<CalculatorCoordinator.Listener> {
    fun loadExpression(expressionAsString: String)
    fun reset()

    interface Listener {
        fun officialCalculationPerformed(expression: Expression, result: Result)
    }
}

class CalculatorCoordinatorImpl (
    private val activity: CalculatorActivity,
    private val listenersMgr: ListenersManager<CalculatorCoordinator.Listener> = ListenersManager(),
) : CalculatorCoordinator, HoldsListeners<CalculatorCoordinator.Listener> by listenersMgr {

    private val expressionBuilder: ExpressionBuilder = ExpressionBuilderImpl()
    private val resultBuilder: ResultBuilder = ResultBuilderImpl(rootUtils.timeExpressionUtils)


    private val expressionLayout: ExpressionLayout = ExpressionLayoutImpl(activity, expressionBuilder)
    private val resultLayout: ResultLayout = createResultLayoutManager()
    private val displayCoordinator: DisplayCoordinator = DisplayCoordinatorImpl(expressionLayout, resultLayout)

    private var calculatorButtonsElasticLayout: CalculatorButtonsElasticLayout = CalculatorButtonsElasticLayoutImpl(activity, false, rootUtils.calculatorPreferencesManager.vibrateKeys.value, rootUtils.vibrationManager)
    private val revealManager: RevealManager = RevealManagerImpl(activity.findViewById(R.id.RevealManagerDrawingSurface))


    private enum class States { Input, Animation, Result }
    private var state: States = States.Input

    private var currentCalculatorAnimation: CalculatorAnimation? = null

    override fun reset() {
        if (state == States.Animation) {
            finishCurrentAnimation()
        }
        resultLayout.config = rootUtils.configManager.getConfigForResultLayoutManager()
        quickResetDisplayAndSetStateToInput()
    }

    override fun loadExpression(expressionAsString: String) {
        when (state) {
            States.Result -> quickResetDisplayAndSetStateToInput()
            States.Animation -> finishCurrentAnimation()
            States.Input -> quickResetDisplayAndSetStateToInput()
        }
        rootUtils.expressionToStringConverter.buildExpressionFromString(expressionBuilder, expressionAsString)
    }

    //------

    private fun symbolButtonPressed(symbol: Symbol) {
        when (state) {
            States.Input -> insertSymbol(symbol)
            States.Animation -> finishCurrentAnimation()
            States.Result -> {
                quickResetDisplayAndSetStateToInput()
                insertSymbol(symbol)
            }
        }
    }

    private fun equalsButtonPressed() {
        when (state) {
            States.Input -> showResultInAnimationIfCanProduceOfficialResult()
            States.Animation -> finishCurrentAnimation()
            States.Result -> Unit
        }
    }

    private fun backspaceButtonPressed() {
        when (state) {
            States.Input -> backspace()
            States.Animation -> finishCurrentAnimation()
            States.Result -> clear()
        }
    }

    private fun clearButtonPressed() {
        when (state) {
            States.Input -> clear()
            States.Animation -> finishCurrentAnimation()
            States.Result -> clear()
        }
    }

    //------

    private fun clear() {
        if (state notEquals (States.Input or States.Result)) { throw InternalError() }
        state = States.Animation
        startClearAnimation()
    }

    private fun backspace() {
        if (state != States.Input) { throw InternalError() }
        val currentLocation = expressionLayout.getExpressionBuilderIndexByInputTextLocation()
        expressionBuilder.backspaceSymbolFrom(currentLocation)
    }

    private fun insertSymbol(symbol: Symbol) {
        if (state != States.Input) { throw InternalError() }
        val currentLocation = expressionLayout.getExpressionBuilderIndexByInputTextLocation()
        expressionBuilder.insertSymbolAt(symbol, currentLocation)
    }

    private fun showResultInAnimationIfCanProduceOfficialResult() {
        if (state != States.Input) { throw InternalError() }
        val expression = expressionBuilder.getExpression()
        val officialResult = resultBuilder.getOfficialResult(expression) ?: return

        state = States.Animation
        if (officialResult is ErrorResult) {
            startErrorResultAnimation(officialResult)
        } else {
            startNormalResultAnimation(expression, officialResult)
        }
    }

    private fun finishCurrentAnimation() {
        currentCalculatorAnimation?.finish()
    }

    private fun quickResetDisplayAndSetStateToInput() {
        if (state == States.Animation) {
            finishCurrentAnimation()
        }
        clearDisplay()
        displayCoordinator.setResultRevealPercentage(0f)
        displayCoordinator.areResultGesturesEnabled = false
        displayCoordinator.isExpressionTextEditEnabled = true
        state = States.Input
        calculatorButtonsElasticLayout.isClearButtonEnabled = false
    }

    //------

    private fun startErrorResultAnimation(errorResult: ErrorResult) {
        if (currentCalculatorAnimation?.isRunning == true) { throw InternalError() }
        currentCalculatorAnimation = ErrorResultRevealAnimation(
            revealManager,
            activity.findViewById(R.id.RevealManagerDrawingSurface),
            displayCoordinator,
            errorResult,
            resultLayout
        ) { setStateToResult() }
    }

    private fun startNormalResultAnimation(expression: Expression, normalOfficialResult: Result) {
        if (currentCalculatorAnimation?.isRunning == true) { throw InternalError() }
        currentCalculatorAnimation = NormalResultRevealAnimation(displayCoordinator) {
            setStateToResult()
            listenersMgr.notifyAll { it.officialCalculationPerformed(expression, normalOfficialResult) }
        }
    }

    private fun startClearAnimation() {
        if (currentCalculatorAnimation?.isRunning == true) { throw InternalError() }
        currentCalculatorAnimation = ClearAnimation(
            revealManager,
            activity.findViewById(R.id.RevealManagerDrawingSurface),
            ::clearDisplay,
            displayCoordinator
        ) { quickResetDisplayAndSetStateToInput() }
    }

    private fun clearDisplay() {
        expressionBuilder.clearAll()
        resultLayout.updateResult(null)
    }

    private fun createResultLayoutManager(): ResultLayout {
        return ResultLayoutImpl(
            activity.findViewById(R.id.resultLayout),
            activity.findViewById(R.id.resultLayoutContainer),
            null,
            rootUtils.configManager.getConfigForResultLayoutManager(),
            activity.findViewById<View>(R.id.resultLayout).width.toFloat(),
            rootUtils.timeExpressionUtils
        )
    }

    //------

    private fun setStateToResult() {
        state = States.Result
        calculatorButtonsElasticLayout.isClearButtonEnabled = true
    }

    private fun updateTempResult() {
        val officialResult = resultBuilder.getTempResult(expressionBuilder.getExpression())
        resultLayout.updateResult(officialResult)
    }

    private fun addListeners() {
        expressionBuilder.addListener(listeners.expressionBuilder)
        calculatorButtonsElasticLayout.addListener(listeners.calculatorButtonsElasticLayout)

        rootUtils.calculatorPreferencesManager.vibrateKeys.addListener(listeners.vibrateKeysPref)


    }

    //------

    private val listeners = object {

        val expressionBuilder = object : ExpressionBuilder.Listener {
            override fun expressionWasChanged(subject: ExpressionBuilder) {
                updateTempResult()
            }
        }

        val calculatorButtonsElasticLayout = object : CalculatorButtonsElasticLayout.Listener {
            override fun actionButtonWasPressed(action: CalculatorButtonsElasticLayout.ButtonActionTypes) {
                when (action) {
                    CalculatorButtonsElasticLayout.ButtonActionTypes.Equals -> equalsButtonPressed()
                    CalculatorButtonsElasticLayout.ButtonActionTypes.Backspace -> backspaceButtonPressed()
                    CalculatorButtonsElasticLayout.ButtonActionTypes.Clear -> clearButtonPressed()
                }
            }
            override fun symbolButtonWasPressed(symbol: Symbol) {
                symbolButtonPressed(symbol)
            }
        }

        val vibrateKeysPref = object: Preference.Listener<Boolean> {
            override fun prefHasChanged(preference: Preference<Boolean>, value: Boolean) {
                this@CalculatorCoordinatorImpl.calculatorButtonsElasticLayout.areButtonsClickHapticsEnabled = value
            }
        }
    }


    init {
        addListeners()
        quickResetDisplayAndSetStateToInput()
    }

}