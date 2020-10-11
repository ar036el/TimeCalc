package com.arealapps.timecalc.calculation_engine.timeExpression

import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit

interface TimeExpressionUtils {
    fun createTimeExpression(totalMillis: Num): TimeExpression
    fun convertTimeValues(fromValue: Num, from: TimeUnit, to: TimeUnit): Num
}

class TimeExpressionUtilsImpl(
    var config: TimeExpressionConfig
): TimeExpressionUtils {
    override fun createTimeExpression(totalMillis: Num): TimeExpression {
        return TimeExpressionImpl(config, totalMillis)
    }
    override fun convertTimeValues(fromValue: Num, from: TimeUnit, to: TimeUnit): Num {
        return (fromValue * config.millisInX[from]) / config.millisInX[to]
    }
}