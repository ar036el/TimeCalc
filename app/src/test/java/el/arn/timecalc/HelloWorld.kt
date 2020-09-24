package el.arn.timecalc

import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.calculation_engine.expression.*
import el.arn.timecalc.calculation_engine.result.*
import el.arn.timecalc.calculation_engine.symbol.*
import el.arn.timecalc.helpers.native_.random


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

val timeConverter: TimeConverter = TimeConverterImpl()
val resultBuilder: ResultBuilder = ResultBuilderImpl(timeConverter)


fun ExpressionBuilder.asString(showGrouping: Boolean): String {
    val expressionTokens = getExpression().expressionTokens
    val stringBuilder = java.lang.StringBuilder()

    expressionTokens.forEach {
        if (it is DigitExprToken && it.hasGroupingPrefix && showGrouping) {
            stringBuilder.append(',')
        }
        stringBuilder.append(it.symbol.asChar)
    }
    return stringBuilder.toString()
}

fun main() {
    val aa= listOf(1,2,3,4,5,6)

    println(aa.firstOrNull { aa.indexOf(it) > aa.indexOf(5) })
}

fun buildRandomExpressionFromExpressionBuilder(minSymbols: Int, maxSymbols: Int) {

    val expressionBuilder = ExpressionBuilderImpl()

    val digitsFactor = random(5f, 10f)
    val decimalPointFactor = 1f
    val operatorsFactor = random(1f, 5f)
    val bracketsFactor = random(1f, 3f)
    val timeUnitsFactor = random(1f, 10f)


    val digitsRange = digitsFactor
    val decimalPointRange = decimalPointFactor + digitsRange
    val operatorsRange = operatorsFactor + decimalPointRange
    val bracketsRange = bracketsFactor + operatorsRange
    val timeUnitsRange = timeUnitsFactor + bracketsRange

    fun putRandomSymbol() {
        val random = Math.random() * timeUnitsRange
        when {
            random <= digitsFactor ->
                expressionBuilder.insertSymbolAt(Digit.values().toList().shuffled()[0], ExpressionBuilder.END_OF_EXPRESSION)
            random <= decimalPointRange ->
                expressionBuilder.insertSymbolAt(DecimalPoint, ExpressionBuilder.END_OF_EXPRESSION)
            random <= operatorsRange ->
                expressionBuilder.insertSymbolAt(Operator.values().toList().shuffled()[0], ExpressionBuilder.END_OF_EXPRESSION)
            random <= bracketsRange ->
                expressionBuilder.insertSymbolAt(Bracket.values().toList().shuffled()[0], ExpressionBuilder.END_OF_EXPRESSION)
            random <= timeUnitsRange ->
                expressionBuilder.insertSymbolAt(TimeUnit.asList.toList().shuffled()[0], ExpressionBuilder.END_OF_EXPRESSION)
        }
    }

    repeat(random(minSymbols, maxSymbols)) { putRandomSymbol() }

    println("expression:'${expressionBuilder.asString(false)}'")

    val result = ResultBuilderImpl(TimeConverterImpl()).solveAndGetResult(expressionBuilder.getExpression())
    when (result) {
        is NumberResult -> println("number result:'${result.number}")
        is TimeResult -> println("number result:'${
            timeConverter.millisToTimeVariable(result.totalMillis)}")
        is MixedResult -> println("mixed result: number:'${result.number}, time:'${timeConverter.millisToTimeVariable(result.totalMillis)}")
        else -> println("result:'${result}")
    }

}


fun mainOld(args: Array<String>) {



    println(toNum("0000").plus(toNum(0)))




    return

//    val tokens = stringToExpressionTokens("33.3455556234l")
//
//    val result: ? = null
//
//    when (result) {
//        is NumberResult -> println("NumberResult: ${result.number.toStringWithGroupingFormatting()}")
//        is TimeResult -> {
//            println("TimeResult: ${timeConverter.timeVariableToMillis(result.time)} total millis")
//            println("years: ${result.time.years}")
//            println("months: ${result.time.months}")
//            println("weeks: ${result.time.weeks}")
//            println("days: ${result.time.days}")
//            println("hours: ${result.time.hours}")
//            println("minutes: ${result.time.minutes}")
//            println("seconds: ${result.time.seconds}")
//            println("milliseconds: ${result.time.millis}")
//        }
//        is MixedResult -> {
//            println("MixedResult:")
//            println("Number: ${result.number.toStringWithGroupingFormatting()}")
//            println("Time: ${timeConverter.timeVariableToMillis(result.time)} total millis")
//            println("years: ${result.time.years}")
//            println("months: ${result.time.months}")
//            println("weeks: ${result.time.weeks}")
//            println("days: ${result.time.days}")
//            println("hours: ${result.time.hours}")
//            println("minutes: ${result.time.minutes}")
//            println("seconds: ${result.time.seconds}")
//            println("milliseconds: ${result.time.millis}")
//        }
//        is ErrorResult -> {
//            println("ErrorResult: $result")
//        }
//        else -> throw InternalError()
//
//    }
}


fun lalala() {
    val expressionBuilder: ExpressionBuilder = ExpressionBuilderImpl()
    var inputAction: InputAction? = null
    while (inputAction != InputAction.Quit) {
        println(exprTokenSequenceToString(expressionBuilder.getExpression().expressionTokens))
        print("command: ")
        val stringInput = readLine()!!
        inputAction = getInputAction(stringInput)
        when (inputAction) {
            is InputAction.Insert -> expressionBuilder.insertSymbolAt(
                inputAction.symbol,
                expressionBuilder.getExpression().expressionTokens.lastIndex + 1
            )
            is InputAction.Backspace -> expressionBuilder.backspaceSymbolFrom(expressionBuilder.getExpression().expressionTokens.lastIndex + 1)
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