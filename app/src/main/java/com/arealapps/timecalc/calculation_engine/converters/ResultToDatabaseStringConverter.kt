package com.arealapps.timecalc.calculation_engine.converters

import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.toNum
import com.arealapps.timecalc.calculation_engine.result.*
import com.arealapps.timecalc.calculation_engine.symbol.*
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpression
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionUtils

interface ResultToDatabaseStringConverter {
    var timeExpressionUtils: TimeExpressionUtils
    fun resultToString(result: Result): String
    fun stringToResult(string: String): Result
}

class ResultToDatabaseStringConverterImpl(
    override var timeExpressionUtils: TimeExpressionUtils
) : ResultToDatabaseStringConverter {

    object Prefixes {
        val numberResult = 'N'
        val timeResult = 'T'
        val mixedResult = 'M'
        val cantDivideByZeroErrorResult = '0'
        val cantMultiplyTimeQuantitiesErrorResult = '1'
        val expressionIsEmptyErrorResult = '2'
        val badFormulaErrorResult = '3'
    }
    val divider = '|'

    private val symbolsWithStrings = mutableMapOf<Symbol, String>()
    private lateinit var groupingPrefixString: String

    private var useTimeUnitsFullSuffix = false


    override fun resultToString(result: Result): String {
        return when (result) {
            is NumberResult -> Prefixes.numberResult + result.number.toStringUnformatted()
            is TimeResult -> Prefixes.timeResult + result.time.totalMillis.toStringUnformatted()
            is MixedResult -> Prefixes.mixedResult + result.number.toStringUnformatted() + divider +  result.time.totalMillis.toStringUnformatted()
            is CantDivideByZeroErrorResult -> Prefixes.cantDivideByZeroErrorResult.toString()
            is CantMultiplyTimeQuantitiesErrorResult -> Prefixes.cantMultiplyTimeQuantitiesErrorResult.toString()
            is ExpressionIsEmptyErrorResult -> Prefixes.expressionIsEmptyErrorResult.toString()
            is BadFormulaErrorResult -> Prefixes.badFormulaErrorResult.toString()
            else -> throw NotImplementedError()
        }
    }

    override fun stringToResult(string: String): Result {
        val prefix = string[0]
        val content = string.substring(1..string.lastIndex)
        return when (prefix) {
            Prefixes.numberResult -> {
                NumberResult(content.toNum())
            }
            Prefixes.timeResult -> {
                TimeResult(content.toNum().toTimeExpression())
            }
            Prefixes.mixedResult -> {
                val indexOfDivider = content.indexOf(divider)
                val number = content.substring(0, indexOfDivider)
                val time = content.substring(indexOfDivider+1, content.length)
                MixedResult(number.toNum(), time.toNum().toTimeExpression())
            }
            Prefixes.cantDivideByZeroErrorResult -> {
                CantDivideByZeroErrorResult()
            }
            Prefixes.cantMultiplyTimeQuantitiesErrorResult -> {
                CantMultiplyTimeQuantitiesErrorResult()
            }
            Prefixes.expressionIsEmptyErrorResult -> {
                ExpressionIsEmptyErrorResult() //todo I dont thing this resultIsEverUsed. remove it??
            }
            Prefixes.badFormulaErrorResult ->{
                BadFormulaErrorResult()
            }
            else -> throw NotImplementedError()
        }
    }

    private fun Num.toTimeExpression(): TimeExpression {
        return timeExpressionUtils.createTimeExpression(this)
    }
}