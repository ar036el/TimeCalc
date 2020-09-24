package el.arn.timecalc.tests

import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl
import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.atoms.TimeExpression
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.calculation_engine.expression.*
import el.arn.timecalc.calculation_engine.result.*
import el.arn.timecalc.calculation_engine.symbol.*
import el.arn.timecalc.helpers.native_.random
import junit.framework.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test


class ResultBuilderTest {

    private var tester: ResultBuilder? = null
    private val timeConverter: TimeConverter = TimeConverterImpl()

    @Before
    fun setUp() {
        tester = ResultBuilderImpl(timeConverter)
    }

    @After
    fun tearDown() {
        tester = null
    }

    fun Number.l(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Milli, TimeUnit.Milli)
    fun Number.s(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Second, TimeUnit.Milli)
    fun Number.m(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Minute, TimeUnit.Milli)
    fun Number.h(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Hour, TimeUnit.Milli)
    fun Number.d(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Day, TimeUnit.Milli)
    fun Number.w(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Week, TimeUnit.Milli)
    fun Number.o(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Month, TimeUnit.Milli)
    fun Number.y(): Num = timeConverter.convertTimeUnit(toNum(this), TimeUnit.Year, TimeUnit.Milli)


    private fun n(number: Number) = toNum(number)


    private operator fun Number.plus(other: Num) = n(this) + other
    private operator fun Number.minus(other: Num) = n(this) - other
    private operator fun Number.times(other: Num) = n(this) * other
    private operator fun Number.div(other: Num) = n(this) / other
    private infix fun Number.p(other: Num) = n(this) p other

    private operator fun Num.plus(other: Number) = this + n(other)
    private operator fun Num.minus(other: Number) = this - n(other)
    private operator fun Num.times(other: Number) = this * n(other)
    private operator fun Num.div(other: Number) = this / n(other)
    private infix fun Num.p(other: Number) = this p n(other)


    fun testForNumberResult(expressionAsString: String, numberAsString: String) = testForNumberResult(expressionAsString, toNum(numberAsString))
    fun testForNumberResult(expressionAsString: String, number: Number) = testForNumberResult(expressionAsString, toNum(number))

    fun testForNumberResult(expressionAsString: String, number: Num) {
        val result = tester!!.solveAndGetResult(Expression(ExpressionTestUtils.stringToExpressionTokens(expressionAsString)))
        if (result !is NumberResult) {
            fail("expected NumberResult but was ${result.javaClass.simpleName}")
        }
        result as NumberResult
        assertEquals(number.toStringUnformatted(), result.number.toStringUnformatted())
    }

    //    fun testForTimeResult(expressionAsString: String, millis: Number = 0, seconds: Number = 0, minutes: Number = 0, hours: Number = 0, days: Number = 0, weeks: Number = 0, months: Number = 0, years: Number = 0) {

    fun testForTimeResult(expressionAsString: String, totalMillis: Num) {
        val result = tester!!.solveAndGetResult(Expression(ExpressionTestUtils.stringToExpressionTokens(expressionAsString)))
        if (result !is TimeResult) {
            fail("expected TimeResult but was ${result.javaClass.simpleName}")
        }
        assertTime(totalMillis, (result as TimeResult).totalMillis)

    }

    fun testForTimeResult_SeparateUnits(expressionAsString: String, millis: Number = 0, seconds: Number = 0, minutes: Number = 0, hours: Number = 0, days: Number = 0, weeks: Number = 0, months: Number = 0, years: Number = 0) {
        testForTimeResult(
            expressionAsString,
            timeConverter.timeExpressionToMillis(TimeExpression(
                toNum(millis.toString()),
                toNum(seconds.toString()),
                toNum(minutes.toString()),
                toNum(hours.toString()),
                toNum(days.toString()),
                toNum(weeks.toString()),
                toNum(months.toString()),
                toNum(years.toString())
            ))
        )
    }

    fun assertTime(totalMillisExpected: Num, totalMillisActual: Num) {
        val excepted = timeConverter.millisToTimeExpression(totalMillisExpected)
        val actual = timeConverter.millisToTimeExpression(totalMillisActual)
        assertEquals(totalMillisExpected.toStringUnformatted(), totalMillisActual.toStringUnformatted())

        assertEquals(excepted.millis.toString(), actual.millis.toString())
        assertEquals(excepted.seconds.toString(), actual.seconds.toString())
        assertEquals(excepted.minutes.toString(), actual.minutes.toString())
        assertEquals(excepted.hours.toString(), actual.hours.toString())
        assertEquals(excepted.days.toString(), actual.days.toString())
        assertEquals(excepted.weeks.toString(), actual.weeks.toString())
        assertEquals(excepted.months.toString(), actual.months.toString())
        assertEquals(excepted.years.toString(), actual.years.toString())

    }

    fun testForMixedResult_SeparateUnits(expressionAsString: String, number: Num, millis: Number = 0, seconds: Number = 0, minutes: Number = 0, hours: Number = 0, days: Number = 0, weeks: Number = 0, months: Number = 0, years: Number = 0) {
        testForMixedResult(
            expressionAsString,
            number,
            timeConverter.timeExpressionToMillis(TimeExpression(
                toNum(millis.toString()),
                toNum(seconds.toString()),
                toNum(minutes.toString()),
                toNum(hours.toString()),
                toNum(days.toString()),
                toNum(weeks.toString()),
                toNum(months.toString()),
                toNum(years.toString())
            ))
        )
    }

    fun testForMixedResult(expressionAsString: String, number: Num, totalMillis: Num) {
        val result = tester!!.solveAndGetResult(Expression(ExpressionTestUtils.stringToExpressionTokens(expressionAsString)))
        if (result !is MixedResult) {
            fail("expected MixedResult but was ${result.javaClass.simpleName}")
        }
        result as MixedResult
        assertEquals(number.toStringUnformatted(), result.number.toStringUnformatted())
        assertEquals(totalMillis.toStringUnformatted(), result.totalMillis.toStringUnformatted())
    }

    fun testForBadFormulaErrorResult(expressionAsString: String) {
        val result = tester!!.solveAndGetResult(Expression(ExpressionTestUtils.stringToExpressionTokens(expressionAsString)))
        if (result !is BadFormulaErrorResult || result is CantMultiplyTimeQuantitiesErrorResult || result is CantDivideByZeroErrorResult) {
            fail("expected MixedResult but was ${result.javaClass.simpleName}")
        }
    }
    fun testForCantMultiplyTimeUnitsResult(expressionAsString: String) {
        val result = tester!!.solveAndGetResult(Expression(ExpressionTestUtils.stringToExpressionTokens(expressionAsString)))
        if (result !is CantMultiplyTimeQuantitiesErrorResult) {
            fail("expected MixedResult but was ${result.javaClass.simpleName}")
        }
    }
    fun testForCantDivideByZeroResult(expressionAsString: String) {
        val result = tester!!.solveAndGetResult(Expression(ExpressionTestUtils.stringToExpressionTokens(expressionAsString)))
        if (result !is CantDivideByZeroErrorResult) {
            fail("expected MixedResult but was ${result.javaClass.simpleName}")
        }
    }



    @Test
    fun successfulBuild() {

        testForTimeResult("2l", 2*1.l())


        testForNumberResult("000000", "0")
        testForNumberResult("0", "0")
        testForNumberResult("00", "0")
        testForNumberResult("100.00", "100")
        testForNumberResult("100", "100")


        testForNumberResult("100*1", "100")
        testForNumberResult("100*1*2", "200")

        testForNumberResult("2(130*1)", "260")
        testForNumberResult("2(130*1)3", (2*(130*1)*3).toString())






        testForTimeResult_SeparateUnits("1m+0", minutes = 1)
        testForTimeResult_SeparateUnits("4m+0", minutes = 4)
        testForTimeResult_SeparateUnits("4m2m", minutes = 6)
        testForTimeResult_SeparateUnits("4y2h0m", years = 4, hours = 2)

        testForNumberResult("1+1", "2")
        testForNumberResult("1+10", "11")
        testForNumberResult("1+100000", "100001")

        testForNumberResult("1", "1")
        testForNumberResult("0", "0")
        testForNumberResult("0.", "0")

        testForBadFormulaErrorResult("1+1+")


        testForMixedResult_SeparateUnits("1m+03", number = n(3), minutes = 1)
        testForMixedResult_SeparateUnits("1m+03*2", number = n(6), minutes = 1)


        testForTimeResult_SeparateUnits("4m2m", minutes = 6)

        testForTimeResult("4w5d3m", 4.w()+5.d()+ 3.m())









    }

    @Test
    fun testResultsFromRandomExpressionsAndSeeThatNoExceptionIsBeingThrown() {
        fun test(minSymbols: Int, maxSymbols: Int) {
            val expressionBuilder = ExpressionBuilderImpl()
            ExpressionTestUtils.buildRandomExpression(expressionBuilder ,minSymbols, maxSymbols)
            val result = ResultBuilderImpl(TimeConverterImpl()).solveAndGetResult(expressionBuilder.getExpression())
        }
        repeat(1000) { test(0, 10) }
        repeat(1000) { test(0, 100) }
        repeat(1000) { test(0, 500) }
    }

}


object ExpressionTestUtils {
    fun stringToExpressionTokens(string: String): List<ExpressionToken> {
        val expressionTokens = mutableListOf<ExpressionToken>()
        string.forEach { char ->
            val expressionToken = when {
                Digit.values().map { it.asChar }.contains(char) -> DigitExprToken(
                    Digit.charOf(char),
                    false,
                    true
                )
                DecimalPoint.asChar == char -> DecimalPointExprToken(true)
                Operator.values().map { it.asChar }.contains(char) -> OperatorExprToken(
                    Operator.charOf(
                        char
                    )
                )
                Bracket.values().map { it.asChar }.contains(char) -> BracketExprToken(
                    Bracket.charOf(
                        char
                    )
                )
                TimeUnit.asList.map { it.asChar }.contains(char) -> TimeUnitExprToken(
                    TimeUnit.charOf(
                        char
                    )
                )
                else -> throw NotImplementedError("bad char:'$char'")
            }
            expressionTokens.add(expressionToken)
        }
        return expressionTokens.toList()
    }


    fun Expression.asString(showGrouping: Boolean): String {
        val expressionTokens = expressionTokens
        val stringBuilder = java.lang.StringBuilder()

        expressionTokens.forEach {
            if (it is DigitExprToken && it.hasGroupingPrefix && showGrouping) {
                stringBuilder.append(',')
            }
            stringBuilder.append(it.symbol.asChar)
        }
        return stringBuilder.toString()
    }


    fun buildRandomExpression(expressionBuilder: ExpressionBuilder, minSymbols: Int, maxSymbols: Int) { //todo use this in expressionBuilderTests
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

    }

}


