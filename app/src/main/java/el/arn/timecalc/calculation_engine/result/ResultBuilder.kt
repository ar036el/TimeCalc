package el.arn.timecalc.calculation_engine.result

import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeExpressionFactory
import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.atoms.createZero
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.calculation_engine.expression.*
import el.arn.timecalc.calculation_engine.symbol.*
import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.StringBuilder

interface ResultBuilder {
    fun solveAndGetResult(expression: Expression): Result
}

class ResultBuilderImpl(
    private val timeConverter: TimeConverter,
    private val timeExpressionFactory: TimeExpressionFactory
) : ResultBuilder {

    override fun solveAndGetResult(expression: Expression): Result {
        try {
            val formulaBuilder = FormulaBuilder()
            val expressionTokens = expression.expressionTokens

            expressionTokens.forEach {
                formulaBuilder.addExpressionToken(it)
            }

            var preResultNumeral = formulaBuilder.solve()
            return createResult(preResultNumeral)

        } catch (e: BadExpressionException) {
//            e.printStackTrace()
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

    inner class FormulaBuilder {
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
                    else if (getLastComponent() is SegmentComponent_Numeral && (getLastComponent() as SegmentComponent_Numeral).numeral is PreResultNumeral_TimeAsMillis) {
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
                            if (getLastComponent() is SegmentComponent_SegmentOrNumeral) {
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
            currentSegment.add(SegmentComponent_Numeral(PreResultNumeral_SimpleNumber(number)))
        }
        fun addMilliseconds(number: Num) {
            currentSegment.add(SegmentComponent_Numeral(PreResultNumeral_TimeAsMillis(number)))
        }
        fun addOperator(operator: Operator) {
            currentSegment.add(SegmentComponent_Operator(operator))
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


        private fun Segment.add(component: SegmentComponent) {
            if ((lastComponent == null && component is SegmentComponent_Operator && !component.isAdditive)
                || (component is SegmentComponent_SegmentOrNumeral && lastComponent is SegmentComponent_SegmentOrNumeral)
                || (component is SegmentComponent_Operator && lastComponent is SegmentComponent_Operator)) {
                throw BadExpressionException()
            }

            //add zero to start if first component is 'minus'
            if (lastComponent == null && component is SegmentComponent_Operator && component.isAdditive) {
                components.add(SegmentComponent_Numeral(PreResultNumeral_SimpleNumber(createZero())))
            }

            components.add(component)
        }

        private fun Segment.solve(): SegmentComponent_Numeral {
            //check if the expression was parsed right
            if (components.isEmpty()) {
                throw BadExpressionException()
            }
            components.forEachIndexed {index, component ->
                if (index % 2 == 0 && component !is SegmentComponent_SegmentOrNumeral
                    || index % 2 == 1 && component !is SegmentComponent_Operator
                    || index % 2 == 1 && index == components.lastIndex) {
                    throw BadExpressionException() //todo maybe internalError?
                }
            }
            //solve all multiplicative operations
            while (true) {
                val operatorIndex = components.indexOfFirst { it is SegmentComponent_Operator && it.isMultiplicative }
                if (operatorIndex == -1) {
                    break
                }
                solveSingleOperation(operatorIndex)
            }
            if (components.any { it is SegmentComponent_Operator && it.isMultiplicative }) { throw InternalError() }
            //solve all additive operations
            while (true) {
                val operatorIndex = components.indexOfFirst { it is SegmentComponent_Operator && it.isAdditive }
                if (operatorIndex == -1) {
                    break
                }
                solveSingleOperation(operatorIndex)
            }
            //you are left with only 1 Numeral or Segment. just format() it to eliminate weird formatting and that's your result
            if  (components.size > 1) { throw InternalError() }
            var lastSegment = components[0]
            lastSegment = if (lastSegment is Segment) lastSegment.solve() else lastSegment as SegmentComponent_Numeral

            val formattedNumeral =  when (val numeral = lastSegment.numeral) {
                is PreResultNumeral_SimpleNumber -> PreResultNumeral_SimpleNumber(numeral.number.format())
                is PreResultNumeral_TimeAsMillis -> PreResultNumeral_TimeAsMillis(numeral.milliseconds.format())
                is PreResultNumeral_Mixed -> PreResultNumeral_Mixed(numeral.number.format(), numeral.milliseconds.format())
                else -> throw NotImplementedError()
            }

            return SegmentComponent_Numeral(formattedNumeral)
        }


        private fun Segment.solveSingleOperation(operatorComponentIndex: Int) {
            val index = operatorComponentIndex
            fun SegmentComponent_SegmentOrNumeral.getNumeral() = if (this is Segment) solve().numeral else (this as SegmentComponent_Numeral).numeral
            val numeralA = components[index -1] as SegmentComponent_SegmentOrNumeral
            val operation = components[index] as SegmentComponent_Operator
            val numeralB = components[index+1] as SegmentComponent_SegmentOrNumeral

            val result =  numeralA.getNumeral().doOperation(operation.operator, numeralB.getNumeral())

            components.removeAt(index-1)
            components.removeAt(index-1)
            components.removeAt(index-1)
            components.add(index-1, SegmentComponent_Numeral(result))
        }

    }

    interface SegmentComponent
    interface SegmentComponent_SegmentOrNumeral : SegmentComponent

    class Segment(val parent: Segment?) : SegmentComponent_SegmentOrNumeral {
        val components = mutableListOf<SegmentComponent>()
        val lastComponent get() = components.getOrNull(components.lastIndex)
    }
    class SegmentComponent_Numeral(val numeral: PreResultNumeral) :
        SegmentComponent_SegmentOrNumeral
    class SegmentComponent_Operator(val operator: Operator) : SegmentComponent {    //todo change all 'operator' to 'operation'?
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


