package com.arealapps.timecalculator.calculation_engine.timeExpression

import com.arealapps.timecalculator.calculation_engine.base.*
import com.arealapps.timecalculator.calculation_engine.symbol.TimeUnit.*
import com.arealapps.timecalculator.calculation_engine.symbol.TimeUnit

interface TimeExpression {
    /**
    timeUnits are all floored except millis, which is rounded to up to 2 decimal points
     */
    val totalMillis: Num
    val timeUnits: TimeVariable<Num>
    fun getAsCollapsed(collapsedUnits: TimeVariable<Boolean>): TimeVariable<Num>
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
                value.round(config.decimalPointsToRoundForMillis, Num.RoundingOptions.Up)
                //TODO right now, when it's smaller that 0.01 (2 decimal points), it just gives this number. I can deliver a message that said it's lower that this, so in display it would be something like ">0.01"
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

