package el.arn.timecalc

import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl

interface RootUtils {
    val timeConverter: TimeConverter
}

class RootUtilsImpl : RootUtils {
    override val timeConverter: TimeConverter = TimeConverterImpl()
}