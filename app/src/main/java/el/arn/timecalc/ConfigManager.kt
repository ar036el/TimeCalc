package el.arn.timecalc

import el.arn.timecalc.calculation_engine.TimeExpressionConfig
import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.mainActivity.ui.ResultLayoutManager

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