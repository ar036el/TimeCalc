package com.arealapps.timecalculator.calculation_engine.converters

import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpression
import com.arealapps.timecalculator.calculation_engine.result.*
import com.arealapps.timecalculator.calculation_engine.symbol.*
import com.arealapps.timecalculator.helpers.android.stringFromRes
import java.lang.StringBuilder

interface ResultToReadableStringConverter {
    fun resultToString(result: Result, useTimeUnitsFullSuffix: Boolean = false): String
//    fun stringToResult(databaseString: String): Result
}

class ResultToReadableStringConverterImpl : ResultToReadableStringConverter {


    private val timeUnitsWithStrings = mutableMapOf<TimeUnit, String>()
    private var useTimeUnitsFullSuffix = false
        set(value) {
            if (field != value) {
                field = value
                updateStringResourceData()
            }
        }

    override fun resultToString(result: Result, useTimeUnitsFullSuffix: Boolean): String {
        return when (result) {
            is TimeResult -> timeExpressionToString(result.time)
            is NumberResult -> result.number.toStringWithGroupingFormatting()
            is MixedResult -> timeExpressionToString(result.time) + result.number.toStringFormatted(true, true, true)
            is CantDivideByZeroErrorResult -> stringFromRes(R.string.errorResult_cantDivideBy0)
            is CantMultiplyTimeQuantitiesErrorResult ->stringFromRes(R.string.errorResult_cantMultiplyTimeQuantities)
//            is ExpressionIsEmptyErrorResult -> throw NotImplementedError("todo!")
            is BadFormulaErrorResult -> stringFromRes(R.string.errorResult_badFormula)
            else -> throw NotImplementedError()
        }
    }

    private fun updateStringResourceData() {
        timeUnitsWithStrings[TimeUnit.Milli] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_millisecond_abbrev else R.string.calculator_timeUnit_millisecond_full)
        timeUnitsWithStrings[TimeUnit.Second] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_second_abbrev else R.string.calculator_timeUnit_second_full)
        timeUnitsWithStrings[TimeUnit.Minute] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_minute_abbrev else R.string.calculator_timeUnit_minute_full)
        timeUnitsWithStrings[TimeUnit.Hour] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_hour_abbrev else R.string.calculator_timeUnit_hour_full)
        timeUnitsWithStrings[TimeUnit.Day] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_day_abbrev else R.string.calculator_timeUnit_day_full)
        timeUnitsWithStrings[TimeUnit.Week] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_week_abbrev else R.string.calculator_timeUnit_week_full)
        timeUnitsWithStrings[TimeUnit.Month] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_month_abbrev else R.string.calculator_timeUnit_month_full)
        timeUnitsWithStrings[TimeUnit.Year] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_year_abbrev else R.string.calculator_timeUnit_year_full)
    }

    private fun timeExpressionToString(timeExpression: TimeExpression): String {
        val stringBuilder = StringBuilder()
        timeExpression.timeUnits.toListPaired().forEach {
            if (!it.second.isZero()) {
                stringBuilder.append(it.second.toStringWithGroupingFormatting())
                stringBuilder.append(timeUnitsWithStrings[it.first])
            }
        }
        return stringBuilder.toString()
    }

    init {
        updateStringResourceData()
    }

}