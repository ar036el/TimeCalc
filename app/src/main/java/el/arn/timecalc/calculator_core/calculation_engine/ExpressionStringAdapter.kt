package el.arn.timecalc.calculator_core.calculation_engine

import el.arn.timecalc.R
import el.arn.timecalc.stringFromRes
import java.lang.StringBuilder

interface ExpressionStringAdapter {
    fun expressionToString(): String
    //fun stringToExpression??? todo if needed..
    fun stringIndexToExpressionIndex(stringPosIndex: Int): Int
    fun expressionIndexToStringIndex(expressionPosIndex: Int): Int
    fun updateStringResourceData() //todo don't forget to use it when changing language or when activity resumes or something..
    var expressionBuilder: ExpressionBuilder
    var useTimeUnitsFullSuffix: Boolean
    var showGrouping: Boolean
}


class ExpressionStringAdapterImpl(
    override var expressionBuilder: ExpressionBuilder,
    override var useTimeUnitsFullSuffix: Boolean,
    override var showGrouping: Boolean
): ExpressionStringAdapter {

    private val symbolsWithStrings = mutableMapOf<Symbol, String>()
    private lateinit var groupingPrefixString: String

    override fun expressionToString(): String {
        val expressionTokens = expressionBuilder.expressionTokens
        val stringBuilder = StringBuilder()

        expressionTokens.forEach {
            if (it is DigitExprToken && it.hasGroupingPrefix && showGrouping) {
                stringBuilder.append(groupingPrefixString)
            }
            stringBuilder.append(symbolsWithStrings[it.symbol]!!)
        }
        return stringBuilder.toString()
    }

    override fun stringIndexToExpressionIndex(stringPosIndex: Int): Int {
        var expressionPosIndex = 0
        var totalCharsPassed = 0

        for (exprToken in expressionBuilder.expressionTokens) {
            if (exprToken is DigitExprToken && exprToken.hasGroupingPrefix && showGrouping) {
                totalCharsPassed++
            }
            if (totalCharsPassed >= stringPosIndex) {
                break
            }
            totalCharsPassed += symbolsWithStrings[exprToken.symbol]!!.length
            expressionPosIndex++
        }
        return expressionPosIndex
    }

    override fun expressionIndexToStringIndex(expressionPosIndex: Int): Int {
        var stringPosIndex = 0
        for (i in 0 until expressionPosIndex ) {
            val exprToken = expressionBuilder.expressionTokens[i]
            stringPosIndex += symbolsWithStrings[exprToken.symbol]!!.length
            if (exprToken is DigitExprToken && exprToken.hasGroupingPrefix && showGrouping) {
                stringPosIndex++
            }
        }
        return stringPosIndex
    }

    override fun updateStringResourceData() {
        symbolsWithStrings[Digit.Zero] = stringFromRes(R.string.calculator_digit_0)
        symbolsWithStrings[Digit.One] = stringFromRes(R.string.calculator_digit_1)
        symbolsWithStrings[Digit.Two] = stringFromRes(R.string.calculator_digit_2)
        symbolsWithStrings[Digit.Three] = stringFromRes(R.string.calculator_digit_3)
        symbolsWithStrings[Digit.Four] = stringFromRes(R.string.calculator_digit_4)
        symbolsWithStrings[Digit.Five] = stringFromRes(R.string.calculator_digit_5)
        symbolsWithStrings[Digit.Six] = stringFromRes(R.string.calculator_digit_6)
        symbolsWithStrings[Digit.Seven] = stringFromRes(R.string.calculator_digit_7)
        symbolsWithStrings[Digit.Eight] = stringFromRes(R.string.calculator_digit_8)
        symbolsWithStrings[Digit.Nine] = stringFromRes(R.string.calculator_digit_9)

        groupingPrefixString = stringFromRes(R.string.calculator_groupingPrefix)

        symbolsWithStrings[DecimalPoint] = stringFromRes(R.string.calculator_decimalPoint)

        symbolsWithStrings[Operator.Plus] = stringFromRes(R.string.calculator_operator_plus)
        symbolsWithStrings[Operator.Minus] = stringFromRes(R.string.calculator_operator_minus)
        symbolsWithStrings[Operator.Multiplication] = stringFromRes(R.string.calculator_operator_multiplication)
        symbolsWithStrings[Operator.Division] = stringFromRes(R.string.calculator_operator_division)
        symbolsWithStrings[Operator.Percent] = stringFromRes(R.string.calculator_operator_percent)

        symbolsWithStrings[Bracket.Opening] = stringFromRes(R.string.calculator_bracket_opening)
        symbolsWithStrings[Bracket.Closing] = stringFromRes(R.string.calculator_bracket_closing)

        symbolsWithStrings[TimeUnit.Milli] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_millisecond_abbrev else R.string.calculator_timeUnit_millisecond_full)
        symbolsWithStrings[TimeUnit.Second] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_second_abbrev else R.string.calculator_timeUnit_second_full)
        symbolsWithStrings[TimeUnit.Minute] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_minute_abbrev else R.string.calculator_timeUnit_minute_full)
        symbolsWithStrings[TimeUnit.Hour] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_hour_abbrev else R.string.calculator_timeUnit_hour_full)
        symbolsWithStrings[TimeUnit.Day] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_day_abbrev else R.string.calculator_timeUnit_day_full)
        symbolsWithStrings[TimeUnit.Week] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_week_abbrev else R.string.calculator_timeUnit_week_full)
        symbolsWithStrings[TimeUnit.Month] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_month_abbrev else R.string.calculator_timeUnit_month_full)
        symbolsWithStrings[TimeUnit.Year] = stringFromRes(if (!useTimeUnitsFullSuffix) R.string.calculator_timeUnit_year_abbrev else R.string.calculator_timeUnit_year_full)
    }

    init {
        updateStringResourceData()
    }

}