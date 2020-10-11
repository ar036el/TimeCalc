package com.arealapps.timecalc.utils.config

import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionConfig
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayout

interface ConfigManager {
    fun getConfigForTimeResultLayoutManager(): ResultLayout.Config
    fun getTimeExpressionConfig(): TimeExpressionConfig
}


class ConfigManagerImpl : ConfigManager {
    override fun getConfigForTimeResultLayoutManager(): ResultLayout.Config {
        return ResultLayout.Config(TimeVariable{ false }) //todo
    }

    override fun getTimeExpressionConfig(): TimeExpressionConfig {
        return TimeExpressionConfig(31, 365)
    }


}