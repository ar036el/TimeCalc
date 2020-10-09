package com.arealapps.timecalc.calculation_engine

import com.arealapps.timecalc.calculation_engine.basics.*
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit.*
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit

interface TimeExpression {
    /**
    timeUnits are all floored except millis, which is rounded to up to 2 decimal points
     */
    val totalMillis: Num
    val timeUnits: TimeVariable<Num>
    fun getAsCollapsed(collapsedUnits: TimeVariable<Boolean>): TimeVariable<Num>
}

class TimeExpressionFactory(var timeExpressionConfig: TimeExpressionConfig) {
    fun createTimeExpression(totalMillis: Num): TimeExpression = TimeExpressionImpl(timeExpressionConfig, totalMillis)
}

class TimeExpressionImpl(
    private val config: TimeExpressionConfig,
    override val totalMillis: Num
) : TimeExpression {

    override val timeUnits = initTimeUnits()

    private fun initTimeUnits(): TimeVariable<Num> {
        var totalMillisBuffer = totalMillis

        val result = MutableTimeVariable { createZero() }

        result.toListPaired().asReversed().forEach {
            val timeUnit = it.first
            var value = totalMillisBuffer.convertTo(Milli, timeUnit, false)
            value = if (timeUnit == Milli) {
                value.round(config.decimalPointsToRoundForMillis, Num.RoundingOptions.Even)
            } else {
                value.floor()
            }
            totalMillisBuffer -= value.convertTo(timeUnit, Milli, false)
            result[timeUnit] = value
        }

        if (totalMillisBuffer > toNum(0.1)) { throw InternalError("totalMillisBuffer[$totalMillisBuffer] > 0.1") }
        return result
    }

    override fun getAsCollapsed(collapsedUnits: TimeVariable<Boolean>): TimeVariable<Num> {
        if (collapsedUnits[Milli]) {
            throw InternalError("Millis cannot be collapsed")
        }

        val result = MutableTimeVariable<Num> { createZero() }
        val nonCollapsedItemsWithTheirCollapsedItemsIfAny = mutableMapOf<TimeUnit, List<TimeUnit>>()

        val buffer = mutableListOf<TimeUnit>()
        collapsedUnits.toListPaired().reversed().forEach {
            val timeUnit = it.first
            val isCollapsed = it.second
            if (isCollapsed) {
                buffer.add(timeUnit)
            } else {
                nonCollapsedItemsWithTheirCollapsedItemsIfAny[timeUnit] = buffer.toList()
                buffer.clear()
            }
        }
        if (buffer.isNotEmpty()) { throw InternalError() }

        result.toListPaired().forEach {
            if (!collapsedUnits[it.first]) {
                result[it.first] = timeUnits[it.first]
            }
        }

        nonCollapsedItemsWithTheirCollapsedItemsIfAny.forEach { entry ->
            val maximizedItem = entry.key
            val collapsedItemsInside = entry.value

            collapsedItemsInside.forEach {collapsedItem ->
                val collapsedItemValue = timeUnits[collapsedItem]
                val a = collapsedItemValue.convertTo(collapsedItem, maximizedItem, true)
                result[maximizedItem] += collapsedItemValue.convertTo(collapsedItem, maximizedItem, true)
            }
        }

        return result.toTimeVariable()
    }

    @JvmName("convertTo1")
    private fun Num.convertTo(from: TimeUnit, to: TimeUnit, floored: Boolean): Num = convertTo(this, from, to, floored)
    private fun convertTo(value: Num, from: TimeUnit, to: TimeUnit, floored: Boolean): Num {
        val result = (value * config.millisInX[from]) / config.millisInX[to]
        return if (floored) {
            result.floor()
        } else {
            result
        }
    }

}


class TimeExpressionConfig(
    daysInAMonth: Float,
    daysInAYear: Float
) {

//    val daysInAMonth = toNum(daysInAMonth)
//    val daysInAYear = toNum(daysInAYear)

    private val millisInMillis = toNum("1") // :P
    private val millisInSec = toNum("1000")
    private val millisInMin = toNum("60000")
    private val millisInHour = toNum("3600000")
    private val millisInDay = toNum("86400000")
    private val millisInWeek = toNum("604800000")
    private val millisInMonth = (toNum(daysInAMonth) * millisInDay).floor()
    private val millisInYear = (toNum(daysInAYear) * millisInDay).floor()

    private val DAYS_IN_WEEK = toNum("7")
    private val WEEKS_IN_MONTH = (toNum("4"))


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