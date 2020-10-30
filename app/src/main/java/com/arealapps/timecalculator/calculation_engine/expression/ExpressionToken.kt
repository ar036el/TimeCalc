package com.arealapps.timecalculator.calculation_engine.expression

import com.arealapps.timecalculator.calculation_engine.symbol.*


interface ExpressionToken {
    val symbol: Symbol
    fun asString() = symbol.asChar.toString()
}

interface NumberExpressionToken : ExpressionToken {
    var isLegalNumber: Boolean
}

class DigitExprToken(
    var digit: Digit,
    var hasGroupingPrefix: Boolean,
    override var isLegalNumber: Boolean
) : ExpressionToken, NumberExpressionToken {
    override val symbol get() = digit
    override fun asString(): String {
        val groupingPrefix = if (hasGroupingPrefix) DecimalPoint.asChar.toString() else ""
        val digit = digit.asChar.toString()
        return "${groupingPrefix}${digit}"
    }
}

class DecimalPointExprToken(
    override var isLegalNumber: Boolean
) : ExpressionToken, NumberExpressionToken {
    var decimalPoint = DecimalPoint
    override val symbol get() = decimalPoint
}

class OperatorExprToken(
    var operator: Operator
) : ExpressionToken {
    override val symbol get() = operator
}

class BracketExprToken(
    var bracket: Bracket
) : ExpressionToken {
    override val symbol get() = bracket
}

class TimeUnitExprToken(
    var timeUnit: TimeUnit
) : ExpressionToken {
    override val symbol get() = timeUnit
}

