package el.arn.timecalc

import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.mainActivity.ui.TimeResultLayoutManager

interface ConfigManager {
    fun getConfigForTimeResultLayoutManager(): TimeResultLayoutManager.Config
}


class ConfigManagerImpl : ConfigManager {
    override fun getConfigForTimeResultLayoutManager(): TimeResultLayoutManager.Config {
        return TimeResultLayoutManager.Config(true, TimeVariable{ false }) //todo
    }

}