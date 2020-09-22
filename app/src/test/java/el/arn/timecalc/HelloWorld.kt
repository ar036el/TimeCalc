package el.arn.timecalc

import el.arn.timecalc.calculator_core.calculation_engine.*
import java.lang.StringBuilder


private fun exprTokenSequenceToString(exprTokens: List<ExpressionToken>): String {
    val strBuilder = StringBuilder()
    exprTokens.forEach {
        if (it is DigitExprToken && it.hasGroupingPrefix) {
            strBuilder.append(",")
        }
        strBuilder.append(it.symbol.asChar)
    }
    return strBuilder.toString()
}

fun stringToExpressionTokens(string: String): List<ExpressionToken> {
    val expressionTokens = mutableListOf<ExpressionToken>()
    string.forEach { char ->
        val expressionToken = when {
            Digit.values().map { it.asChar }.contains(char) -> DigitExprToken(Digit.charOf(char), false, true)
            DecimalPoint.asChar == char -> DecimalPointExprToken(true)
            Operator.values().map { it.asChar }.contains(char) -> OperatorExprToken(Operator.charOf(char))
            Bracket.values().map { it.asChar }.contains(char) -> BracketExprToken(Bracket.charOf(char))
            TimeUnit.asList.map { it.asChar }.contains(char) -> TimeUnitExprToken(TimeUnit.charOf(char))
            else -> throw NotImplementedError()
        }
        expressionTokens.add(expressionToken)
    }
    return expressionTokens.toList()
}


fun main(args: Array<String>) {
    val resultBuilder: ResultBuilder = ResultBuilderImpl()

    val tokens = stringToExpressionTokens("14y(3+1)+1m")

    val result = resultBuilder.build(tokens)

    when (result) {
        is NumberResult -> println("NumberResult: ${result.number.toStringWithGroupingFormatting()}")
        is TimeResult -> {
            println("TimeResult: ${timeVariableToMillis(result.timeVariable)} total millis")
            println("years: ${result.timeVariable.years}")
            println("months: ${result.timeVariable.months}")
            println("weeks: ${result.timeVariable.weeks}")
            println("days: ${result.timeVariable.days}")
            println("hours: ${result.timeVariable.hours}")
            println("minutes: ${result.timeVariable.minutes}")
            println("seconds: ${result.timeVariable.seconds}")
            println("milliseconds: ${result.timeVariable.millis}")
        }
        is MixedResult -> {
            println("MixedResult:")
            println("Number: ${result.number.toStringWithGroupingFormatting()}")
            println("Time: ${timeVariableToMillis(result.time)} total millis")
            println("years: ${result.time.years}")
            println("months: ${result.time.months}")
            println("weeks: ${result.time.weeks}")
            println("days: ${result.time.days}")
            println("hours: ${result.time.hours}")
            println("minutes: ${result.time.minutes}")
            println("seconds: ${result.time.seconds}")
            println("milliseconds: ${result.time.millis}")
        }
        is ErrorResult -> {
            println("ErrorResult: $result")
        }
        else -> throw InternalError()

    }
}


fun lalala() {
    val expressionBuilder: ExpressionBuilder = ExpressionBuilderImpl()
    var inputAction: InputAction? = null
    while (inputAction != InputAction.Quit) {
        println(exprTokenSequenceToString(expressionBuilder.expressionTokens))
        print("command: ")
        val stringInput = readLine()!!
        inputAction = getInputAction(stringInput)
        when (inputAction) {
            is InputAction.Insert -> expressionBuilder.insertSymbolAt(inputAction.symbol, expressionBuilder.expressionTokens.lastIndex + 1)
            is InputAction.Backspace -> expressionBuilder.backspaceSymbolFrom(expressionBuilder.expressionTokens.lastIndex + 1)
        }

    }
    println("bye!")
}

sealed class InputAction {
    class Insert(val symbol: Symbol): InputAction()
    object Backspace: InputAction()
    object Quit: InputAction()
}

fun getInputAction(stringInput: String): InputAction? {
    if (stringInput.length == 1) {
        if (stringInput[0] == 'z') {
            return InputAction.Backspace
        } else if (stringInput[0] == 'q') {
            return InputAction.Quit
        }
        return try { InputAction.Insert(Symbol.charOf(stringInput[0])) } catch (e: NoSuchElementException) { null }
    }
    return null
}