package com.arealapps.timecalculator.utils.config

import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionConfig
import com.arealapps.timecalculator.calculation_engine.base.TimeVariable
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalculator.utils.preferences_managers.CalculatorPreferencesManager

interface ConfigManager {
    fun getConfigForResultLayoutManager(): ResultLayout.Config
    fun getTimeExpressionConfig(): TimeExpressionConfig
}


class ConfigManagerImpl(
    private val prefsManager: CalculatorPreferencesManager
) : ConfigManager {
    override fun getConfigForResultLayoutManager(): ResultLayout.Config {
        val toAutoCollapse = TimeVariable(
            prefsManager.autoCollapseMillis.value,
            prefsManager.autoCollapseSeconds.value,
            prefsManager.autoCollapseMinutes.value,
            prefsManager.autoCollapseHours.value,
            prefsManager.autoCollapseDays.value,
            prefsManager.autoCollapseWeeks.value,
            prefsManager.autoCollapseMonths.value,
            prefsManager.autoCollapseYears.value,
        )
        return ResultLayout.Config(toAutoCollapse)
    }

    override fun getTimeExpressionConfig(): TimeExpressionConfig {
        val daysInAMonth = prefsManager.daysInAMonth.value.numberValue
        val daysInAYear = prefsManager.daysInAYear.value.value
        return TimeExpressionConfig(daysInAMonth, daysInAYear)
    }


}