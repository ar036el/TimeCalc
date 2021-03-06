package com.arealapps.timecalculator.calculation_engine.calculatorCoordinator

import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText.ExpressionLayout
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalculator.helpers.native_.checkIfPercentIsLegal

interface DisplayCoordinator {
    fun setResultRevealPercentage(percent: Float)
    var areResultGesturesEnabled: Boolean
    var isExpressionTextEditEnabled: Boolean
}

class DisplayCoordinatorImpl(
    private val expressionLayout: ExpressionLayout,
    private val resultLayout: ResultLayout
) : DisplayCoordinator {
    override fun setResultRevealPercentage(percent: Float) {
        checkIfPercentIsLegal(percent)
        expressionLayout.abilityPercentage = 1f - percent
        resultLayout.abilityPercentage = percent
    }

    override var isExpressionTextEditEnabled: Boolean
        get() = expressionLayout.isTextEditEnabled
        set(value) { expressionLayout.isTextEditEnabled = value}

    override var areResultGesturesEnabled: Boolean
        get() = resultLayout.areGesturesEnabled
        set(value) { resultLayout.areGesturesEnabled = value }


}