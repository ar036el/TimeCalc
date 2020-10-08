package com.arealapps.timecalc.utils.config

import com.arealapps.timecalc.calculation_engine.TimeExpressionConfig
import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculatorActivity.ui.calculator.ResultLayout.ResultLayoutManager

interface ConfigManager {
    fun getConfigForTimeResultLayoutManager(): ResultLayoutManager.Config
    fun getTimeExpressionConfig(): TimeExpressionConfig
}


class ConfigManagerImpl : ConfigManager {
    override fun getConfigForTimeResultLayoutManager(): ResultLayoutManager.Config {
        return ResultLayoutManager.Config(TimeVariable{ false }) //todo
    }

    override fun getTimeExpressionConfig(): TimeExpressionConfig {
        return TimeExpressionConfig(30f, 365f)
    }


}