package com.arealapps.timecalculator.calculation_engine.expression

import com.arealapps.timecalculator.helpers.meta.TestedPrivateFunctionWithNoSideEffects
import com.arealapps.timecalculator.calculation_engine.base.Num
import com.arealapps.timecalculator.calculation_engine.symbol.*
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager
import java.lang.NumberFormatException

interface ExpressionBuilder : HoldsListeners<ExpressionBuilder.Listener> {

    fun insertSymbolAt(symbol: Symbol, index: Int): InsertionAction
    fun insertSymbolAtEnd(symbol: Symbol): InsertionAction
    fun backspaceSymbolFrom(index: Int)
    fun backspaceSymbolFromEnd(index: Int)
    fun clearAll()

    fun getExpression(): Expression


    enum class InsertionAction { Add, Replace, NoAction }

    interface Listener {
        fun expressionWasChanged(subject: ExpressionBuilder) {}
        fun expressionWasCleared() {}
        fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {}
        fun exprTokenWasReplacedAt(token: ExpressionToken, replaced: ExpressionToken, index: Int) {}
        fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {}

    }

    companion object {
        const val END_OF_EXPRESSION = -1
    }

}


class ExpressionBuilderImpl(
    private val listenersMgr: ListenersManager<ExpressionBuilder.Listener> = ListenersManager()
): ExpressionBuilder, HoldsListeners<ExpressionBuilder.Listener> by listenersMgr {

    private val expressionTokens: MutableList<ExpressionToken> = mutableListOf()

    //todo private val expressionTokens to make this as the source of expression?

    override fun getExpression(): Expression {
        return Expression(expressionTokens.toList())
    }

    override fun insertSymbolAtEnd(symbol: Symbol): ExpressionBuilder.InsertionAction {
        return insertSymbolAt(symbol, ExpressionBuilder.END_OF_EXPRESSION)
    }

    override fun insertSymbolAt(symbol: Symbol, index: Int): ExpressionBuilder.InsertionAction {
        val index = if (index == ExpressionBuilder.END_OF_EXPRESSION) expressionTokens.lastIndex + 1 else index
        isIndexValid(index)

        val tokenToInsert = createTokenFromSymbol(symbol)
        val tokenBefore = if (index == 0) null else expressionTokens[index-1]

        val actionToApply = getInsertionActionForGivenInsertionState(tokenBefore, tokenToInsert, index, expressionTokens)
        when (actionToApply) {
            ExpressionBuilder.InsertionAction.Add -> {
                expressionTokens.add(index, tokenToInsert)
                updateGroupingForAllNumbers()
                listenersMgr.notifyAll { it.exprTokenWasAddedAt(tokenToInsert, index) }
                listenersMgr.notifyAll { it.expressionWasChanged(this) }
            }
            ExpressionBuilder.InsertionAction.Replace -> {
                val replaced = expressionTokens.set(index-1, tokenToInsert)
                updateGroupingForAllNumbers()
                listenersMgr.notifyAll { it.exprTokenWasReplacedAt(tokenToInsert, replaced, index-1) }
                listenersMgr.notifyAll { it.expressionWasChanged(this) }
            }
            ExpressionBuilder.InsertionAction.NoAction -> Unit //do nothing
        }
        return actionToApply
    }

    override fun backspaceSymbolFromEnd(index: Int) {
        return backspaceSymbolFrom(ExpressionBuilder.END_OF_EXPRESSION)
    }

    override fun backspaceSymbolFrom(index: Int) {
        val index = if (index == ExpressionBuilder.END_OF_EXPRESSION) expressionTokens.lastIndex else index
        isIndexValid(index)
        if (index == 0) {
            return
        }
        val removed = expressionTokens.removeAt(index-1)
        updateGroupingForAllNumbers()
        listenersMgr.notifyAll { it.exprTokenWasRemovedAt(removed, index-1) }
        listenersMgr.notifyAll { it.expressionWasChanged(this) }
    }

    override fun clearAll() {
        if (expressionTokens.isNotEmpty()) {
            expressionTokens.clear()
            listenersMgr.notifyAll { it.expressionWasCleared() }
            listenersMgr.notifyAll { it.expressionWasChanged(this) }

        }
    }


    private fun isIndexValid(index: Int) {
        if (index > expressionTokens.lastIndex + 1 || index < 0) {
            throw InternalError("invalid index")
        }
    }

    @TestedPrivateFunctionWithNoSideEffects //yeah?? sure??
    private fun getInsertionActionForGivenInsertionState(tokenBefore: ExpressionToken?, tokenToInsert: ExpressionToken, insertAtIndex: Int, expression: List<ExpressionToken>): ExpressionBuilder.InsertionAction {
        val action: ExpressionBuilder.InsertionAction

        when (tokenToInsert) {
            is DigitExprToken -> {
                action = ExpressionBuilder.InsertionAction.Add
            }
            is DecimalPointExprToken -> {
                if (willPuttingADecimalPointAtThisIndexBreakANumber(insertAtIndex, expression)) {
                    action = ExpressionBuilder.InsertionAction.NoAction
                } else {
                    action = ExpressionBuilder.InsertionAction.Add
                }
            }
            is OperatorExprToken -> {
                if (tokenBefore is OperatorExprToken) {
                    if (isTryingToPutAMinusBeforeAMultiplicativeOperation(tokenBefore, tokenToInsert)) {
                        action = ExpressionBuilder.InsertionAction.Add
                    } else {
                        action = ExpressionBuilder.InsertionAction.Replace
                    }
                }
                else if (tokenBefore == null || (tokenBefore is BracketExprToken && tokenBefore.bracket == Bracket.Opening)) {
                    if (tokenToInsert.operator == Operator.Minus) {
                        action = ExpressionBuilder.InsertionAction.Add
                    } else {
                        action = ExpressionBuilder.InsertionAction.NoAction
                    }
                } else {
                    action = ExpressionBuilder.InsertionAction.Add
                }
            }
            is BracketExprToken -> {
                if (tokenBefore == null && tokenToInsert.bracket == Bracket.Closing) {
                    action = ExpressionBuilder.InsertionAction.NoAction
                } else if (tokenBefore is BracketExprToken) {
                    if (tokenToInsert.bracket == Bracket.Closing && tokenBefore.bracket == Bracket.Opening) {
                        action = ExpressionBuilder.InsertionAction.NoAction
                    } else {
                        action = ExpressionBuilder.InsertionAction.Add
                    }
                } else {
                    action = ExpressionBuilder.InsertionAction.Add
                }
            }
            is TimeUnitExprToken -> {
                if (tokenBefore is NumberExpressionToken) {
                    action = ExpressionBuilder.InsertionAction.Add
                } else if (tokenBefore is TimeUnitExprToken) {
                    action = ExpressionBuilder.InsertionAction.Replace
                } else {
                    action = ExpressionBuilder.InsertionAction.NoAction
                }
            }
            else -> throw InternalError()
        }

        return action
    }

    private fun isTryingToPutAMinusBeforeAMultiplicativeOperation(before: OperatorExprToken, toInsert: OperatorExprToken) = toInsert.operator == Operator.Minus && before.operator in setOf(
        Operator.Multiplication,
        Operator.Division,
        Operator.Percent
    )


    @TestedPrivateFunctionWithNoSideEffects
    private fun willPuttingADecimalPointAtThisIndexBreakANumber(index: Int, expression: List<ExpressionToken>): Boolean {
        isIndexValid(index)

        val tokenBefore = if (index != 0) expression[index - 1] else null
        val tokenAfter = if (index != expression.lastIndex + 1) expression[index] else null
        fun ExpressionToken?.isPartOfANumber(): Boolean = this is NumberExpressionToken


        //if there is no number next to this insertion point, so it doesn't break anything
        if (!tokenBefore.isPartOfANumber() && !tokenAfter.isPartOfANumber()) {
            return false
        }

        //find number starting location
        val numberIsStartingAtIndex: Int
        if (tokenBefore.isPartOfANumber()) {
            var indexBuffer = expression.indexOf(tokenBefore!!)
            while (expression.getOrNull(indexBuffer-1).isPartOfANumber()) {
                indexBuffer--
            }
            numberIsStartingAtIndex = indexBuffer
        } else {
            numberIsStartingAtIndex = expression.indexOf(tokenAfter!!)
        }

        //find number ending location
        val numberIsEndingAtIndex: Int
        if (tokenAfter.isPartOfANumber()) {
            //findNumberEndingIndex
            var indexBuffer = expression.indexOf(tokenAfter!!)
            while (expression.getOrNull(indexBuffer+1).isPartOfANumber()) {
                indexBuffer++
            }
            numberIsEndingAtIndex = indexBuffer
        } else {
            numberIsEndingAtIndex = expression.indexOf(tokenBefore!!)
        }

        //make sure the values are accurate
        if (!expression.getOrNull(numberIsStartingAtIndex).isPartOfANumber()
            || !expression.getOrNull(numberIsEndingAtIndex).isPartOfANumber()
            || expression.getOrNull(numberIsStartingAtIndex-1).isPartOfANumber()
            || expression.getOrNull(numberIsEndingAtIndex+1).isPartOfANumber()) {
            throw InternalError()
        }

        //try to find a decimal point in the found number. if so, adding a decimal point will break this number
        for (indexBuffer in numberIsStartingAtIndex..numberIsEndingAtIndex) {
            if (expression[indexBuffer] is DecimalPointExprToken) {
                return true
            }
        }

        //a decimal point in this index won't break any number
        return false
    }

    private fun createTokenFromSymbol(symbol: Symbol): ExpressionToken { //todo is this a factory? or a converter. needs to be outside??
        return when (symbol) {
            is Digit -> DigitExprToken(symbol, false, true)
            is DecimalPoint -> DecimalPointExprToken(true)
            is Operator -> OperatorExprToken(symbol)
            is Bracket -> BracketExprToken(symbol)
            is TimeUnit -> TimeUnitExprToken(symbol)
            else -> throw InternalError()
        }
    }


    private fun updateGroupingForAllNumbers() {
        val allNumbersInExpression = getAllNumbersInExpression(expressionTokens)
        allNumbersInExpression.forEach { setFormattingForNumberIfLegalOrNot(it) }
    }

    @TestedPrivateFunctionWithNoSideEffects
    private fun getAllNumbersInExpression(expression: List<ExpressionToken>): List<List<NumberExpressionToken>> {
        val numbers = mutableListOf<List<NumberExpressionToken>>()
        val currentNumber = mutableListOf<NumberExpressionToken>()
        fun addNumberIfOneWasQueried() {
            if (currentNumber.isNotEmpty()) {
                numbers.add(currentNumber.toList())
            }
        }
        for (indexBuffer in 0..expression.lastIndex) {
            val currentExprToken = expression[indexBuffer]
            if (currentExprToken is NumberExpressionToken) {
                currentNumber.add(currentExprToken)
            } else {
                addNumberIfOneWasQueried()
                currentNumber.clear()
            }
        }
        addNumberIfOneWasQueried()

        return numbers
    }

    private fun setFormattingForNumberIfLegalOrNot(number: List<NumberExpressionToken>) {
        number.forEach{
            if (it is DigitExprToken) {
                it.hasGroupingPrefix = false
            }
            it.isLegalNumber = false
        }

        if (isNumberLegal(number)) {
            val asReversed = getDecimalPartOfANumber(number).reversed()
            var counter = 1
            for (index in 0..asReversed.lastIndex) {
                val hasGroupingPrefix = ((index+1) % Num.GROUP_EVERY_X_DIGITS == 0 && index != asReversed.lastIndex)
                asReversed[index].hasGroupingPrefix = hasGroupingPrefix
            }
            number.forEach{ it.isLegalNumber = true }
        }
    }

    private fun getDecimalPartOfANumber(number: List<NumberExpressionToken>): List<DigitExprToken> {
        val decimalPoint = number.firstOrNull { it is DecimalPointExprToken }
        val decimalPart = if (decimalPoint != null) {
            number.slice(0 until number.indexOf(decimalPoint))
        } else {
            number
        }
        return decimalPart.map { it as DigitExprToken }
    }

    data class NumberParts(val decimalPart: List<DigitExprToken>? = null, val decimalPoint: DecimalPointExprToken? = null, val fractionalPart: List<DigitExprToken>? = null)

    @TestedPrivateFunctionWithNoSideEffects
    private fun sliceLegalNumberIntoItsNumberParts(expression: List<ExpressionToken>, fromIndex: Int, toIndex: Int): NumberParts {
        val number = expressionBoundsToNumberTokenSequence(expression, fromIndex, toIndex)
        if (!isNumberLegal(number)) { throw NumberFormatException() }

        val decimalPoint = number.find { it is DecimalPointExprToken }
        if (decimalPoint == null) {
            return NumberParts(number.map { it as DigitExprToken }, null, null)
        } else {
            val decimalPointIndex = number.indexOf(decimalPoint)
            val decimalPart = number.slice(0 until decimalPointIndex).map { it as DigitExprToken }.ifEmpty { null }
            val fractionalPart = number.slice((decimalPointIndex..number.lastIndex).drop(1)).map { it as DigitExprToken }.ifEmpty { null }
            return NumberParts(decimalPart, decimalPoint as DecimalPointExprToken, fractionalPart)
        }
    }

    private fun isNumberLegal(number: List<NumberExpressionToken>): Boolean {
        return !(number.isEmpty()
                || (number.size == 1 && number[0] is DecimalPointExprToken)
                || (number.count{ it is DecimalPointExprToken } > 1))
    }

    private fun expressionBoundsToNumberTokenSequence(expression: List<ExpressionToken>, fromIndex: Int, toIndex: Int): List<NumberExpressionToken> {
        return expression.slice(fromIndex..toIndex).map { it as NumberExpressionToken }
    }


}