package com.arealapps.timecalc.calculation_engine.result

import com.arealapps.timecalc.calculation_engine.TimeConverter
import com.arealapps.timecalc.calculation_engine.TimeExpressionFactory
import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.createZero
import com.arealapps.timecalc.calculation_engine.basics.toNum
import com.arealapps.timecalc.calculation_engine.expression.*
import com.arealapps.timecalc.calculation_engine.symbol.*
import com.arealapps.timecalc.helpers.native_.next
import com.arealapps.timecalc.helpers.native_.prev
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.StringBuilder

interface ResultBuilder {
    fun getResult(expression: Expression): Result
    fun getOfficialResult(expression: Expression): Result?
    fun getTempResult(expression: Expression): Result?
}

class ResultBuilderImpl(
    private val timeConverter: TimeConverter,
    private val timeExpressionFactory: TimeExpressionFactory
) : ResultBuilder {

    override fun getTempResult(expression: Expression): Result? {
        val tokens = expression.tokens.toMutableList()
        if (tokens.isEmpty()) {
            return null
        }
        if (tokens.last() is OperatorExprToken) {
            tokens.removeAt(tokens.lastIndex)
        }
        if (tokens.all { it is NumberExpressionToken }) {
            return null
        }
        val result = getResult(tokens)
        if (result is ErrorResult) {
            return null
        }
        return result
    }

    override fun getOfficialResult(expression: Expression): Result? {
        val tokens = expression.tokens
        if (tokens.isEmpty()) {
            return null
        }
        if (tokens.all { it is NumberExpressionToken }) {
            return null
        }
        return getResult(tokens)
    }

    override fun getResult(expression: Expression): Result {
        return getResult(expression.tokens)
    }

    private fun getResult(tokens: List<ExpressionToken>): Result {
        try {
            val formulaBuilder = FormulaBuilder()
            tokens

            tokens.forEach {
                formulaBuilder.addExpressionToken(it)
            }

            var preResultNumeral = formulaBuilder.solve()
            return createResult(preResultNumeral)

        } catch (e: BadExpressionException) {
            e.printStackTrace()
            return when (e) {
                is CantMultiplyTimeQuantitiesException -> CantMultiplyTimeQuantitiesErrorResult()
                is CantDivideByZeroException -> CantDivideByZeroErrorResult()
                is ExpressionIsEmptyException -> ExpressionIsEmptyErrorResult()
                else -> BadFormulaErrorResult()
            }
        }
    }

    private fun createResult(preResultNumeral: PreResultNumeral): Result {
        return when (preResultNumeral) {
            is PreResultNumeral_SimpleNumber -> NumberResult(preResultNumeral.number)
            is PreResultNumeral_TimeAsMillis -> TimeResult(timeExpressionFactory.createTimeExpression(preResultNumeral.milliseconds))
            is PreResultNumeral_Mixed -> MixedResult(preResultNumeral.number, timeExpressionFactory.createTimeExpression(preResultNumeral.milliseconds)) //todo change all this to timeExpression when ready
            else -> throw NotImplementedError()
        }
    }

    // todo לעבור על כל המשתנים והקלאסים והמתודות לבדוק שאין סתם פבליק בכל מקום
    private inner class FormulaBuilder {
        private val segmentBuilder = SegmentsBuilder()
        private val numberBuilder = NumberBuilder()

        private var areTimeUnitBeingUsed = false
        private var wasLastTokenTimeUnit = false

        fun addExpressionToken(exprToken: ExpressionToken) {
            fun getLastComponent() = segmentBuilder.lastComponentOfCurrentSegment

            when (exprToken) {
                is NumberExpressionToken -> {
                    if (getLastComponent() is Segment) {
                        segmentBuilder.addOperator(Operator.Multiplication)
                    }
                    else if (getLastComponent() is Numeral_SegmentComponent && (getLastComponent() as Numeral_SegmentComponent).numeral is PreResultNumeral_TimeAsMillis) {
                        segmentBuilder.addOperator(Operator.Plus)
                    }
                    when (exprToken) {
                        is DigitExprToken -> numberBuilder.addDigit(exprToken.digit)
                        is DecimalPointExprToken -> numberBuilder.addDecimalPoint()
                    }
                }
                is OperatorExprToken -> {
                    closeAndAddNumberIfInQueue()
                    segmentBuilder.addOperator(exprToken.operator)
                }
                is BracketExprToken -> {
                    closeAndAddNumberIfInQueue()
                    when (exprToken.bracket) {
                        Bracket.Opening -> {
                            if (getLastComponent() is SegmentOrNumeral_SegmentComponent) {
                                segmentBuilder.addOperator(Operator.Multiplication)
                            }
                            segmentBuilder.openSegment()
                        }
                        Bracket.Closing -> {
                            if (getLastComponent() == null) {
                                throw BadExpressionException()
                            }
                            segmentBuilder.closeSegment()
                        }
                    }
                }
                is TimeUnitExprToken -> {
                    if (wasLastTokenTimeUnit) {
                        throw BadExpressionException()
                    }
                    closeAndAddNumberIfInQueue()
                    segmentBuilder.addOperator(Operator.Multiplication)
                    segmentBuilder.addMilliseconds(
                        timeConverter.convertTimeUnit(
                            toNum(1),
                            exprToken.timeUnit,
                            TimeUnit.Milli
                        )
                    )
                    areTimeUnitBeingUsed = true
                }
            }
            wasLastTokenTimeUnit = exprToken is TimeUnitExprToken
        }

        fun solve(): PreResultNumeral {
            closeAndAddNumberIfInQueue()
            if (segmentBuilder.isEmpty()) {
                throw ExpressionIsEmptyException()
            }
            return segmentBuilder.solve()
        }

        private fun closeAndAddNumberIfInQueue(): Boolean {
            if (!numberBuilder.isEmpty()) {
                segmentBuilder.addNumber(numberBuilder.buildNumber())
                return true
            }
            return false
        }
    }


    class SegmentsBuilder {
        private val rootSegment = Segment(null)
        private var currentSegment = rootSegment
        val lastComponentOfCurrentSegment get() = currentSegment.lastComponent
        fun addNumber(number: Num) {
            currentSegment.add(Numeral_SegmentComponent(PreResultNumeral_SimpleNumber(number)))
        }
        fun addMilliseconds(number: Num) {
            currentSegment.add(Numeral_SegmentComponent(PreResultNumeral_TimeAsMillis(number)))
        }
        fun addOperator(operator: Operator) {
            currentSegment.add(Operator_SegmentComponent(operator))
        }
        fun openSegment() {
            val newSegment = Segment(currentSegment)
            currentSegment.add(newSegment)
            currentSegment = newSegment
        }
        fun closeSegment() {
            currentSegment = currentSegment.parent ?: throw BadExpressionException()
        }
        fun solve() = rootSegment.solve().numeral
        fun isEmpty() = rootSegment.lastComponent == null


        private fun Segment.add(newComponent: SegmentComponent) {
            if (
                (components.isEmpty() && newComponent is Operator_SegmentComponent && newComponent.isMultiplicative)
                || (newComponent is SegmentOrNumeral_SegmentComponent && lastComponent is SegmentOrNumeral_SegmentComponent)
                || (newComponent is Operator_SegmentComponent && lastComponent is Operator_SegmentComponent && !((lastComponent as Operator_SegmentComponent).isMultiplicative && newComponent.isAdditive))
            ){
                throw BadExpressionException()
            }

            //add zero to start if first component is 'minus'
            if (lastComponent == null && newComponent is Operator_SegmentComponent && newComponent.isAdditive) {
                components.add(createZeroSegmentComponent())
            }

            //if there is something like "3*-2", it makes it to "3*(0-2)
            val prevComponentBeforeLastIfAny = lastComponent?.let { components.prev(it) }
            if (newComponent is SegmentOrNumeral_SegmentComponent
                && lastComponent is Operator_SegmentComponent && (lastComponent as Operator_SegmentComponent).isAdditive
                && prevComponentBeforeLastIfAny is Operator_SegmentComponent && prevComponentBeforeLastIfAny.isMultiplicative) {

                val segment = Segment(currentSegment)
                segment.add(createZeroSegmentComponent())
                segment.add(lastComponent as Operator_SegmentComponent)
                segment.add(newComponent)
                replaceLastComponent(segment)
                return
            }

            components.add(newComponent)
        }

        private fun Segment.solve(): Numeral_SegmentComponent {
            //check if the expression was parsed right
            if (components.isEmpty()) {
                throw BadExpressionException()
            }
            components.forEachIndexed {index, component ->
                if (index % 2 == 0 && component !is SegmentOrNumeral_SegmentComponent
                    || index % 2 == 1 && component !is Operator_SegmentComponent
                    || index % 2 == 1 && index == components.lastIndex) {
                    throw BadExpressionException() //todo maybe internalError?
                }
            }

            while (true) {
                val firstItemIndex = components.indexOfFirst {
                    val firstItem = it
                    val secondItem = components.next(it)
                    val thirdItem = secondItem?.let { components.next(it) }

                    //return:
                    firstItem is Operator_SegmentComponent && firstItem.isMultiplicative &&
                    secondItem is Operator_SegmentComponent && firstItem.isAdditive &&
                    thirdItem is SegmentOrNumeral_SegmentComponent
                }
                if (firstItemIndex == -1) {
                    break
                }
                components.add(firstItemIndex+1, createZeroSegmentComponent())
                solveSingleOperation(firstItemIndex+2)
            }

            //solve all multiplicative operations
            while (true) {
                val operatorIndex = components.indexOfFirst { it is Operator_SegmentComponent && it.isMultiplicative }
                if (operatorIndex == -1) {
                    break
                }
                solveSingleOperation(operatorIndex)
            }


            if (components.any { it is Operator_SegmentComponent && it.isMultiplicative }) { throw InternalError() }
            //solve all additive operations
            while (true) {
                val operatorIndex = components.indexOfFirst { it is Operator_SegmentComponent && it.isAdditive }
                if (operatorIndex == -1) {
                    break
                }
                solveSingleOperation(operatorIndex)
            }
            //you are left with only 1 Numeral or Segment. just format() it to eliminate weird formatting and that's your result
            if  (components.size > 1) { throw InternalError() }
            var lastSegment = components[0]
            lastSegment = if (lastSegment is Segment) lastSegment.solve() else lastSegment as Numeral_SegmentComponent

            val formattedNumeral =  when (val numeral = lastSegment.numeral) {
                is PreResultNumeral_SimpleNumber -> PreResultNumeral_SimpleNumber(numeral.number.format())
                is PreResultNumeral_TimeAsMillis -> PreResultNumeral_TimeAsMillis(numeral.milliseconds.format())
                is PreResultNumeral_Mixed -> PreResultNumeral_Mixed(numeral.number.format(), numeral.milliseconds.format())
                else -> throw NotImplementedError()
            }

            return Numeral_SegmentComponent(formattedNumeral)
        }

        private fun createZeroSegmentComponent(): Numeral_SegmentComponent {
            return Numeral_SegmentComponent(PreResultNumeral_SimpleNumber(createZero()))
        }


        private fun Segment.solveSingleOperation(indexOfOperatorInBetween: Int) {
            val index = indexOfOperatorInBetween
            fun SegmentOrNumeral_SegmentComponent.getNumeral() = if (this is Segment) solve().numeral else (this as Numeral_SegmentComponent).numeral
            val numeralA = components[index -1] as SegmentOrNumeral_SegmentComponent
            val operation = components[index] as Operator_SegmentComponent
            val numeralB = components[index+1] as SegmentOrNumeral_SegmentComponent

            val result =  numeralA.getNumeral().doOperation(operation.operator, numeralB.getNumeral())

            components.removeAt(index-1)
            components.removeAt(index-1)
            components.removeAt(index-1)
            components.add(index-1, Numeral_SegmentComponent(result))
        }

    }

    interface SegmentComponent
    interface SegmentOrNumeral_SegmentComponent : SegmentComponent

    class Segment(val parent: Segment?) : SegmentOrNumeral_SegmentComponent {
        val components = mutableListOf<SegmentComponent>()
        val lastComponent get() = components.getOrNull(components.lastIndex)
        fun replaceLastComponent(newComponent: SegmentComponent) { components[components.lastIndex] = newComponent }
    }
    class Numeral_SegmentComponent(val numeral: PreResultNumeral) :
        SegmentOrNumeral_SegmentComponent
    class Operator_SegmentComponent(val operator: Operator) : SegmentComponent {    //todo change all 'operator' to 'operation'?
        val isAdditive = operator.type == Operator.Types.Additive
        val isMultiplicative = !isAdditive
    }


    class NumberBuilder {
        private var numberBuffer = StringBuilder()
        fun isEmpty() = numberBuffer.isEmpty()
        fun addDigit(digit: Digit) {
            numberBuffer.append(digit.asChar)
        }
        fun addDecimalPoint() {
            numberBuffer.append(DecimalPoint.asChar)
        }
        fun buildNumber(): Num
                /** @throws [NumberFormatException]*/ {
            if (numberBuffer.isEmpty()) {
                throw InternalError()
            }
            val number = toNum(numberBuffer.toString())
            numberBuffer.clear()
            return number
        }
    }

}

open class BadExpressionException : Exception()
class CantDivideByZeroException : BadExpressionException()
class CantMultiplyTimeQuantitiesException : BadExpressionException()
class ExpressionIsEmptyException : BadExpressionException()


