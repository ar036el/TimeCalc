package el.arn.timecalc.tests

import el.arn.timecalc.calculator_core.calculation_engine.*
import el.arn.timecalc.helpers.assert.assertThrows
import el.arn.timecalc.helpers.invokePrivateFunction
import org.junit.After
import org.junit.Assert.*
import org.junit.Test

import org.junit.Before
import java.lang.NumberFormatException
import java.lang.StringBuilder


class ExpressionTest {

    private var tester: Expression? = null

    @Before
    fun setUp() {
        tester = ExpressionImpl()
    }

    @After
    fun tearDown() {
        tester = null
    }


    @Test
    fun sliceLegalNumberIntoItsNumberParts_test() {

        fun Expression.sliceLegalNumberIntoItsNumberParts(expression: List<ExpressionToken>, fromIndex: Int, toIndex: Int): ExpressionImpl.NumberParts {
            return (tester as ExpressionImpl).invokePrivateFunction("sliceLegalNumberIntoItsNumberParts", expression, fromIndex, toIndex)
        }

        fun testNumberToItsParts(number: String, decimalPart: String?, decimalPoint: Boolean, fractionalPart: String?) {
            val expression = toNumberExprTokenSequence(number)
            val numberParts: ExpressionImpl.NumberParts = tester!!.sliceLegalNumberIntoItsNumberParts(expression, 0, expression.lastIndex)

            assertEquals(numberParts.decimalPart?.asString(), decimalPart)
            assertEquals(numberParts.decimalPoint?.decimalPoint?.asChar,  if (decimalPoint) '.' else null)
            assertEquals(numberParts.fractionalPart?.asString(), fractionalPart)
        }
        fun testNumberToItsParts_fail(number: String) {
            val expression = toNumberExprTokenSequence(number)
            assertThrows<NumberFormatException> {
                tester!!.sliceLegalNumberIntoItsNumberParts(expression, 0, expression.lastIndex)
            }
        }

        testNumberToItsParts("1.00", "1", true, "00")
        testNumberToItsParts("25234342413123.03123121231231230", "25234342413123", true, "03123121231231230")
        testNumberToItsParts(".03123121231231230", null, true, "03123121231231230")
        testNumberToItsParts("03123121231231230.", "03123121231231230", true, null)
        testNumberToItsParts("00000000000000.03123121231231230", "00000000000000", true, "03123121231231230")
        testNumberToItsParts("0.0", "0", true, "0")
        testNumberToItsParts("00", "00", false, null)
        testNumberToItsParts("0000000", "0000000", false, null)
        testNumberToItsParts(".0000000", null, true, "0000000")
        testNumberToItsParts("000121.0000000", "000121", true, "0000000")
        testNumberToItsParts("000121.", "000121", true, null)
        testNumberToItsParts("000121.0", "000121", true, "0")
        testNumberToItsParts("000121.9", "000121", true, "9")
        testNumberToItsParts("12345678901234.0908070695942712378135418298", "12345678901234", true, "0908070695942712378135418298")
        testNumberToItsParts(".0908070695942712378135418298", null, true, "0908070695942712378135418298")
        testNumberToItsParts("0908070695942712378135418298.", "0908070695942712378135418298", true, null)
        testNumberToItsParts("123456789012340908070695942712378135418298", "123456789012340908070695942712378135418298", false, null)

        testNumberToItsParts_fail("")
        testNumberToItsParts_fail(".")
        testNumberToItsParts_fail("..")
        testNumberToItsParts_fail(".....")
        testNumberToItsParts_fail(".0.")
        testNumberToItsParts_fail("000121.0.")
        testNumberToItsParts_fail("000121.0.2312")
    }


    @Test
    fun testGetAllNumbersInExpression_privateFunc() {
        fun Expression.testGetAllNumbersInExpression(expression: List<ExpressionToken>): List<List<NumberExpressionToken>> {
            return (tester as ExpressionImpl).invokePrivateFunction("getAllNumbersInExpression", expression)
        }
        fun testNumbers(expression: String, vararg numbers: String) {
            val expression = toExprTokenSequence(expression)
            val numbersResult = tester!!.testGetAllNumbersInExpression(expression)
            if (numbers.size != numbersResult.size) {
                fail("expected and actual number list size are not equal(expected:${numbers.size} actual:${numbersResult.size}")
            }
            for (i in numbersResult.indices) {
                assertEquals(numbers[i], numbersResult[i].asString())
            }
        }


        testNumbers("42342+2343+23234.23123+112", "42342", "2343", "23234.23123", "112")
        testNumbers("777777777777777", "777777777777777")
        testNumbers("")
        testNumbers("1", "1")
        testNumbers("1+2-3*4m7h6s+111", "1", "2", "3", "4", "7", "6","111")
        testNumbers("311111m111111s", "311111", "111111")
        testNumbers("++**++", )
        testNumbers("++011**++1", "011", "1")
        testNumbers("1++011**++1", "1", "011", "1")
        testNumbers("-42342%+234..3+23.234.2+3+1+23+112", "42342", "234..3", "23.234.2", "3", "1", "23", "112")
    }







    private fun List<ExpressionToken>.asString(): String {
        val numberAsString = StringBuilder()
        this.forEach{ numberAsString.append(it.symbol.asChar) }
        return numberAsString.toString()
    }


    private fun toNumberExprTokenSequence(number: String): List<NumberExpressionToken> {
        val exprTokenSequence = toExprTokenSequence(number)

        return exprTokenSequence.map {
            if (it !is NumberExpressionToken) {
                throw InternalError()
            }
            it as NumberExpressionToken
        }
    }

    private fun toExprTokenSequence(expression: String): List<ExpressionToken> {
        if (!isStringAnExpression(expression)) { throw InternalError() }

        val exprTokenSequence = mutableListOf<ExpressionToken>()
        for (char in expression.toCharArray()) {
            val exprToken = when (char) {
                '.' ->
                    DecimalPointExprToken(true)
                in "0123456789" ->
                    DigitExprToken(Digit.charOf(char), false, true)
                in ".+-*/%" ->
                    OperatorExprToken(Operator.charOf(char))
                in "()" ->
                    BracketExprToken(Bracket.charOf(char))
                in "lsmhdwoy" ->
                    TimeUnitExprToken(TimeUnit.charOf(char))
                else -> throw InternalError()
            }
            exprTokenSequence.add(exprToken)
        }
        return exprTokenSequence
    }


    private fun isStringANumber(number: String): Boolean {
        val allLegalInputChars = "0123456789.".toCharArray()
        for (char in number.toCharArray()) {
            if (char !in allLegalInputChars) {
                return false
            }
        }
        return true
    }

    private fun isStringAnExpression(expression: String): Boolean {
        val allLegalInputChars = "0123456789.+-*/%()lsmhdwoy".toCharArray()
        for (char in expression.toCharArray()) {
            if (char !in allLegalInputChars) {
                return false
            }
        }
        return true
    }


}
