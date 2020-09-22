package el.arn.timecalc.calculator_core.calculation_engine

import java.math.BigDecimal

/**
 * @throws [NumberFormatException]]
 */
interface Num {
    operator fun plus(number: Num): Num
    operator fun minus(number: Num): Num
    fun multiply(number: Num): Num
    fun divide(number: Num): /** @throws [CantDivideBy0Exception]*/ Num
    fun percent(number: Num): Num
    fun reverseSign(): Num

    operator fun div(number: Num): Num = divide(number)
    operator fun times(number: Num): Num = multiply(number)
    operator fun rem(number: Num): Num


    fun equals(number: Num): Boolean

    fun toDouble(): Double
    fun toStringUnformatted(): String //todo toStringNoFormatting
    fun toStringWithGroupingFormatting(): String

    companion object { //TODO it has also string res for all of this. what to do?
        const val ALL_INPUT_CHARS = "0123456789.-"
        const val SPECIAL_INPUT_CHARS = ".-"
        const val DIGITS = "0123456789"
        const val DECIMAL_POINT = '.'
        const val UNARY_MINUS = '-'
        const val GROUPING_SYMBOL = ','
        const val GROUP_EVERY_X_DIGITS = 3

    }
}


fun toNum(number: String): Num = NumImpl(number)
fun toNum(number: Number): Num {
    return when (number) {
        is Int -> NumImpl(BigDecimal(number).toString())
        is Long -> NumImpl(BigDecimal(number).toString())
        is Double -> NumImpl(BigDecimal(number).toString())
        is Long -> NumImpl(BigDecimal(number).toString())
        else -> throw NotImplementedError()
    }
}
fun createZero(): Num = NumImpl("0")


class NumImpl(numberAsString: String) : Num {

    private val numberAsString: String

    override fun plus(number: Num): Num {
        return NumImpl(BigDecimal(numberAsString).plus(BigDecimal(number.toStringUnformatted())).toString())
    }

    override fun minus(number: Num): Num {
        return NumImpl(BigDecimal(numberAsString).minus(BigDecimal(number.toStringUnformatted())).toString())
    }

    override fun multiply(number: Num): Num {
        return NumImpl(BigDecimal(numberAsString).multiply(BigDecimal(number.toStringUnformatted())).toString())
    }

    override fun divide(number: Num): Num {
        if (number.equals(createZero())) {
            throw CantDivideByZeroException()
        }
        return NumImpl(BigDecimal(numberAsString).divide(BigDecimal(number.toStringUnformatted())).toString())
    }

    override fun percent(number: Num): Num {
        val percent = BigDecimal(number.toStringUnformatted()).divide(BigDecimal("100"))
        return NumImpl(BigDecimal(numberAsString).multiply(percent).toString())
    }

    override fun reverseSign(): Num {
        return NumImpl(BigDecimal("0").minus(BigDecimal(numberAsString)).toString())
    }

    override fun rem(number: Num): Num {
        return NumImpl(BigDecimal(numberAsString).rem(BigDecimal(number.toStringUnformatted())).toString())
    }

    override fun equals(number: Num): Boolean {
        return BigDecimal(numberAsString) == BigDecimal(number.toStringUnformatted())
    }

    override fun toString(): String = toStringWithGroupingFormatting()

    override fun toStringUnformatted(): String {
        return numberAsString
    }

    override fun toStringWithGroupingFormatting(): String {
        val reversedNumber = numberAsString.reversed()

        val startingLocation = reversedNumber.indexOf(Num.DECIMAL_POINT) + 1

        val number = StringBuilder(numberAsString)

        var count = 0
        for (i in startingLocation until reversedNumber.lastIndex) {
            if (!isDigit(reversedNumber[i+1])) {
                break
            }
            count++
            if (count % 3 == 0) {
                number.insert(reversedNumber.length - i - 1, Num.GROUPING_SYMBOL)
            }
        }

        return number.toString()
    }

    override fun toDouble(): Double {
        return BigDecimal(numberAsString).toDouble()
    }

    private fun isDigit(char: Char) = Num.DIGITS.contains(char)

    private fun checkIfNumberIsOk(numberAsString: String) {
        if (numberAsString.isEmpty()) {
            throw NumberFormatException("string is empty")
        }
        if (!doesStringContainOnlyLegalInputChars(numberAsString)) {
            throw NumberFormatException("not a number")
        }
        if (numberAsString.length == 1 && numberAsString[0] in Num.SPECIAL_INPUT_CHARS) {
            throw NumberFormatException("'.' or '_' is not a number")
        }
        val totalCharOccurrencesOfUnaryMinus = getTotalCharsOfType(numberAsString, Num.UNARY_MINUS)
        if (totalCharOccurrencesOfUnaryMinus > 1) {
            throw NumberFormatException("only one UnaryMinus is allowed")
        }
        if (totalCharOccurrencesOfUnaryMinus == 1 && numberAsString[0] != Num.UNARY_MINUS) {
            throw NumberFormatException("UnaryMinus is not in legal position")
        }
        if (getTotalCharsOfType(numberAsString, Num.DECIMAL_POINT) > 1) {
            throw NumberFormatException("only one DecimalPoint is allowed")
        }
    }

    private fun doesStringContainOnlyLegalInputChars(number: String): Boolean {
        val allLegalInputChars = Num.ALL_INPUT_CHARS.toCharArray()
        for (char in number.toCharArray()) {
            if (char !in allLegalInputChars) {
                return false
            }
        }
        return true
    }

    private fun getTotalCharsOfType(number: String, char: Char): Int {
        var occurences = 0
        for (c in number.toCharArray()) {
            if (c == char) {
                occurences++
            }
        }
        return occurences
    }

    init {
        checkIfNumberIsOk(numberAsString)
        this.numberAsString = numberAsString
    }

}