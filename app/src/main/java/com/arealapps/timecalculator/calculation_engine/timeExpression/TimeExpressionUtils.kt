package com.arealapps.timecalculator.calculation_engine.timeExpression

import com.arealapps.timecalculator.calculation_engine.base.Num
import com.arealapps.timecalculator.calculation_engine.symbol.TimeUnit

interface TimeExpressionUtils {
    var config: TimeExpressionConfig
    fun createTimeExpression(totalMillis: Num): TimeExpression
    fun convertTimeValues(fromValue: Num, from: TimeUnit, to: TimeUnit): Num
}

class TimeExpressionUtilsImpl(
    override var config: TimeExpressionConfig
): TimeExpressionUtils {
    override fun createTimeExpression(totalMillis: Num): TimeExpression {
        return TimeExpressionImpl(config, totalMillis)
    }
    override fun convertTimeValues(fromValue: Num, from: TimeUnit, to: TimeUnit): Num {
        return (fromValue * config.millisInX[from]) / config.millisInX[to]
    }
}