package el.arn.timecalc.calculator_core.calculation_engine

import java.lang.Exception
import java.lang.NumberFormatException
import java.lang.StringBuilder

interface ResultBuilder {
    fun build(expression: List<ExpressionToken>): Result
}

class ResultBuilderImpl : ResultBuilder {

    override fun build(expression: List<ExpressionToken>): Result {
        try {
            val formulaBuilder = FormulaBuilder()
            expression.forEach {
                formulaBuilder.addExpressionToken(it)
            }
            val result =  formulaBuilder.solve()
            return when (result) {
                is ResultNumeralSimpleNumber -> NumberResult(result.number)
                is ResultNumeralMilliseconds -> TimeResult(millisToTimeVariable(result.milliseconds))
                is ResultNumeralMixed -> MixedResult(result.number, millisToTimeVariable(result.milliseconds))
                else -> throw NotImplementedError()
            }
        } catch (e: CantDivideByZeroException) {
            e.printStackTrace()
            return CantDivideBy0()
        } catch (e: CantMultiplyTimeQuantitiesException) {
            e.printStackTrace()
            return CantMultiplyTimeQuantities()
        } catch (e: BadExpressionException) {
            return BadFormula()
        }
    }

    class FormulaBuilder {
        private val segmentBuilder = SegmentsBuilder()
        private val numberBuilder = NumberBuilder()

        private var areTimeUnitBeingUsed = false
        private var wasLastTokenTimeUnit = false

        fun addExpressionToken(exprToken: ExpressionToken) {
            when (exprToken) {
                is NumberExpressionToken -> {
                    if (segmentBuilder.lastComponentOfCurrentSegment is Segment) {
                        segmentBuilder.addOperator(Operator.Multiplication)
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
                            if (segmentBuilder.lastComponentOfCurrentSegment is SegmentOrNumeral) {
                                segmentBuilder.addOperator(Operator.Multiplication)
                            }
                            segmentBuilder.openSegment()
                        }
                        Bracket.Closing -> {
                            if (segmentBuilder.lastComponentOfCurrentSegment == null) {
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
                    segmentBuilder.addMilliseconds(TimeUnitConverter.convert(toNum(1), exprToken.timeUnit, TimeUnit.Milli))
                    areTimeUnitBeingUsed = true
                }
            }
            wasLastTokenTimeUnit = exprToken is TimeUnitExprToken
        }

        fun solve(): ResultNumeral {
            closeAndAddNumberIfInQueue()
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
            currentSegment.add(NumberAsSegmentComponent(ResultNumeralSimpleNumber(number)))
        }
        fun addMilliseconds(number: Num) {
            currentSegment.add(NumberAsSegmentComponent(ResultNumeralMilliseconds(number)))
        }
        fun addOperator(operator: Operator) {
            currentSegment.add(OperatorAsSegmentComponent(operator))
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

    }

    interface SegmentComponent
    interface SegmentOrNumeral : SegmentComponent

    class Segment(val parent: Segment?) : SegmentOrNumeral {

        private val components = mutableListOf<SegmentComponent>()
        val lastComponent get() = components.getOrNull(components.lastIndex)

        fun add(component: SegmentComponent) {
            if ((lastComponent == null && component is OperatorAsSegmentComponent && !component.isAdditive)
                || (component is SegmentOrNumeral && lastComponent is SegmentOrNumeral)
                || (component is OperatorAsSegmentComponent && lastComponent is OperatorAsSegmentComponent)) {
                throw BadExpressionException()
            }

            //add zero to start if first component is 'minus'
            if (lastComponent == null && component is OperatorAsSegmentComponent && component.isAdditive) {
                components.add(NumberAsSegmentComponent(ResultNumeralSimpleNumber(createZero())))
            }

            components.add(component)
        }

        fun solve(): NumberAsSegmentComponent {
            var numeralBuffer: ResultNumeral = ResultNumeralSimpleNumber(createZero())
            fun gerResult() = NumberAsSegmentComponent(numeralBuffer)

            fun SegmentOrNumeral.getNumeral() = (if (this is Segment) solve() else (this as NumberAsSegmentComponent)).numeral



            val firstSegment = components.getOrNull(0)
            if (firstSegment == null) {
                return gerResult()
            } else if (firstSegment is OperatorAsSegmentComponent) {
                throw BadExpressionException()
            } else {
                numeralBuffer = numeralBuffer.plus((firstSegment as SegmentOrNumeral).getNumeral())
            }


            for (i in 1 .. components.lastIndex step 2) {
                val componentA = components[i]
                val componentB = components.getOrNull(i+1)

                if (componentA !is OperatorAsSegmentComponent
                    || componentB !is SegmentOrNumeral) {
                    throw BadExpressionException()
                }

                val operator = componentA.operator
                val nextNumeral = componentB.getNumeral()

                numeralBuffer = when (operator) {
                    Operator.Minus -> numeralBuffer.minus(nextNumeral)
                    Operator.Plus -> numeralBuffer.plus(nextNumeral)
                    Operator.Multiplication -> numeralBuffer.multiply(nextNumeral)
                    Operator.Division -> numeralBuffer.divide(nextNumeral)
                    Operator.Percent -> numeralBuffer.percent(nextNumeral)
                }

            }
            return gerResult()
        }

    }

    //todo change all 'operator' to 'operation'?
    class NumberAsSegmentComponent(val numeral: ResultNumeral) : SegmentOrNumeral

    class OperatorAsSegmentComponent(val operator: Operator) : SegmentComponent {
        val isAdditive = operator.type == Operator.Types.Additive
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
        fun buildNumber(): Num /** @throws [NumberFormatException]*/ {
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
//todo expressionIsEmpty?? like no input at all


