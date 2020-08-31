package el.arn.timecalc.tests

import el.arn.timecalc.calculator_core.calculation_engine.Number
import el.arn.timecalc.calculator_core.calculation_engine.NumberImpl
import org.junit.Test
import org.junit.After
import org.junit.Assert.*
import java.lang.NumberFormatException


class BigDecimalEngineTest {

    private var tester: Number? = null

//    @Before
//    fun setUp() {
//        tester = Number()
//    }

    @After
    fun tearDown() {
        tester = null
    }

    @Test
    fun createNumbersOfDifferentKinds() {
        fun testNumberAsPositiveAndNegative(positiveNumberAsString: String) {
            val negativeNumberAsString = "-$positiveNumberAsString"
            tester = NumberImpl(positiveNumberAsString)
            assertEquals(positiveNumberAsString, tester!!.toStringUnformatted())
            tester = NumberImpl(negativeNumberAsString)
            assertEquals(negativeNumberAsString, tester!!.toStringUnformatted())
        }
        fun testNumber(numberAsString: String, expected: String? = null) {
            tester = NumberImpl(numberAsString)
            assertEquals(expected ?: numberAsString, tester!!.toStringUnformatted())
        }

        for (numberAsInt in 1..35) {
            testNumberAsPositiveAndNegative(numberAsInt.toString())
        }
        testNumberAsPositiveAndNegative("101")
        testNumberAsPositiveAndNegative("12312")
        testNumberAsPositiveAndNegative("74112")
        testNumberAsPositiveAndNegative("54241")
        testNumberAsPositiveAndNegative("1251")
        testNumberAsPositiveAndNegative("12456")
        testNumberAsPositiveAndNegative("385623247234")
        testNumberAsPositiveAndNegative("8375753453412312366234234")
        testNumberAsPositiveAndNegative("546243413231231246755112635231232363423213123")
        testNumberAsPositiveAndNegative("23982930124714775092131312930182312093812093810239812903123182908109283213")
        testNumberAsPositiveAndNegative("2398293012471477509213131293018231209312097807128300000000000000000011111111111122111111111111111111113812093810239812903123182908109283213")
        testNumberAsPositiveAndNegative("2398293012471477509213131293018231209312023982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312093123982930124714775092131312930182312092398293012471477509213131293018231209312398293012471477509213131293018231209313111111111111111113812093810239812903123182908109283213")


        testNumber("0")
        testNumber("-0")
        testNumber("000000000000000")
        testNumber("-00000000")
        testNumber("-000000000000000000000000000000000")
        testNumberAsPositiveAndNegative("00000.0")
        testNumberAsPositiveAndNegative("0.0")
        testNumberAsPositiveAndNegative("00000.00000")
        testNumberAsPositiveAndNegative("0.00000")
        testNumberAsPositiveAndNegative("00000000000000000000000000000000000.0000000000000000000000000000000")
        testNumberAsPositiveAndNegative("0214000001111")
        testNumberAsPositiveAndNegative("11111.0")
        testNumberAsPositiveAndNegative("4563423222.000000000000000000000000000000000111111")
        testNumberAsPositiveAndNegative("754239031230121111100011.000000000000000000000000000000000111111")
        testNumberAsPositiveAndNegative("754239031230121111100011.754239031230121111100011")
        testNumberAsPositiveAndNegative("00000000000000000000000000000000000.00000000000000000000000000000000001")
        testNumberAsPositiveAndNegative("100000000000000000000000000000000000.00000000000000000000000000000000001")
        testNumberAsPositiveAndNegative("100000000000000000000000000000000000.00000000000000000000000000000000000")
        testNumberAsPositiveAndNegative(".0")
        testNumberAsPositiveAndNegative(".1")
        testNumberAsPositiveAndNegative(".155634344232")
        testNumberAsPositiveAndNegative(".0000000000000000000001")
        testNumberAsPositiveAndNegative(".0000000000000000005745231231230001")
        testNumberAsPositiveAndNegative("0.")
        testNumberAsPositiveAndNegative("1.")
        testNumberAsPositiveAndNegative("155634344232.")
        testNumberAsPositiveAndNegative("0000000000000000000001.")
        testNumberAsPositiveAndNegative("0000000000000000005745231231230001.")

        testNumberAsPositiveAndNegative("000000001")
        testNumberAsPositiveAndNegative("0000000000000000000001")
        testNumberAsPositiveAndNegative("00000000000000000000000000000001")
        testNumberAsPositiveAndNegative("100000000")
        testNumberAsPositiveAndNegative("10000000000000000000000")
        testNumberAsPositiveAndNegative("100000000000000000000000000000000")
    }

    @Test
    fun createIllegalNumbers() {
        fun testIllegalNumber(numberAsString: String) {
            try {
                tester = NumberImpl(numberAsString)
                fail("suppose to throw ${NumberFormatException::class}")
            } catch (e: NumberFormatException) {
                assertTrue(true)
            }
        }

        testIllegalNumber("")
        testIllegalNumber(" ")
        testIllegalNumber("rwerweqeqe")
        testIllegalNumber("rwerwe 346weo ad[f23-8 q-d aaqeqe")
        testIllegalNumber("42532325234. ")
        testIllegalNumber("42,532,325,234")
        testIllegalNumber("1,000")
        testIllegalNumber("-1,000")
        testIllegalNumber("-1[000")
        testIllegalNumber("-1 000")
        testIllegalNumber("+1000")
        testIllegalNumber("*1000")
        testIllegalNumber("%1000")
        testIllegalNumber("/1000")
        testIllegalNumber("1000+")
        testIllegalNumber("1000-")
        testIllegalNumber("1000*")
        testIllegalNumber("1000%")
        testIllegalNumber("1000/")
        testIllegalNumber("1+2")
        testIllegalNumber("1-2")
        testIllegalNumber("1*2")
        testIllegalNumber("1/2")
        testIllegalNumber("1%2")
        testIllegalNumber("1&2")
        testIllegalNumber("1\\2")
        testIllegalNumber("*")
        testIllegalNumber("+")
        testIllegalNumber("a")
        testIllegalNumber("00000423423!424222222222222222222222222222222222222222")


        testIllegalNumber("-")
        testIllegalNumber(".")


        testIllegalNumber("--1")
        testIllegalNumber("----1")
        testIllegalNumber("1-")
        testIllegalNumber("1--")
        testIllegalNumber("1-----")

        testIllegalNumber("423423424-1")
        testIllegalNumber("0-00004234234241")
        testIllegalNumber("00000423423424222222222222222222222222222222222222222-")
        testIllegalNumber("00000423423!424222222222222222222222222222222222222222-")


        testIllegalNumber("..1")
        testIllegalNumber("1..")
        testIllegalNumber("32431.312123.01")
        testIllegalNumber("32431.312123.")
        testIllegalNumber("0.32431.312123.")
        testIllegalNumber("032431.312123.")

        testIllegalNumber("155555555555555555555555555522222222222222222222222222222222222222221111111111..")
        testIllegalNumber(".155555555555555555555555555522222222222222222222222222222222222222221111111111.")
        testIllegalNumber(".155555555555555555555555555522222222222222222222222222222222222222221111111111.")
        testIllegalNumber(".155555555555555555...............555")
        testIllegalNumber("155555555555555555...............5555555")
        testIllegalNumber("1555555............55555555555...............555")

        testIllegalNumber("1555555.....---....55555555555...............555")

    }
}