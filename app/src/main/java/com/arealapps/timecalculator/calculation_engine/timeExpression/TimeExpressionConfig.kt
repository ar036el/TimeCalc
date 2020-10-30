package com.arealapps.timecalculator.calculation_engine.timeExpression

import com.arealapps.timecalculator.calculation_engine.base.TimeVariable
import com.arealapps.timecalculator.calculation_engine.base.toNum
import com.arealapps.timecalculator.helpers.native_.EnumWithId


class TimeExpressionConfig(
    daysInAMonth: Int,
    daysInAYear: Int
) {

    enum class DaysInAMonthOptions(
        override val id: String,
        val numberValue: Int
    ): EnumWithId {
        _28("28", 28),
        _29("29", 29),
        _30("30", 30),
        _31("31", 31),
        Average("average", 30) //fake
    }

    enum class DaysInAYearOptions(
        override val id: String,
        val value: Int
    ): EnumWithId {
        _365("365", 365),
        _366("366", 366),
        Average("average", 365) //fake
        }

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