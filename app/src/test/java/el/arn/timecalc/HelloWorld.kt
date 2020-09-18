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


fun main(args: Array<String>) {
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