package com.arealapps.timecalc.calculation_engine.timeExpression

import com.arealapps.timecalc.calculation_engine.basics.TimeVariable
import com.arealapps.timecalc.calculation_engine.basics.toNum


class TimeExpressionConfig(
    daysInAMonth: Int,
    daysInAYear: Int
) {

    private val millisInMillis = toNum("1") // :P
    private val millisInSec = toNum("1000")
    private val millisInMin = toNum("60000")
    private val millisInHour = toNum("3600000")
    private val millisInDay = toNum("86400000")
    private val millisInWeek = toNum("604800000")
    private val millisInMonth = (toNum(daysInAMonth) * millisInDay).floor()
    private val millisInYear = (toNum(daysInAYear) * millisInDay).floor()


    val decimalPointsToRoundForMillis = 2
    val millisInX = TimeVariable(
        millisInMillis,
        millisInSec,
        millisInMin,
        millisInHour,
        millisInDay,
        millisInWeek,
        millisInMonth,
        millisInYear
    )

}