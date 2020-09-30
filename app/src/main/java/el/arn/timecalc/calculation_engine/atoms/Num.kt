package el.arn.timecalc.calculation_engine.atoms

import el.arn.timecalc.calculation_engine.atoms.Num.Companion.UNARY_MINUS
import el.arn.timecalc.calculation_engine.result.CantDivideByZeroException
import el.arn.timecalc.calculation_engine.symbol.DecimalPoint
import el.arn.timecalc.calculation_engine.symbol.Digit
import el.arn.timecalc.helpers.native_.Wrapper
import java.math.BigDecimal
import java.math.RoundingMode

/**
 * @throws [NumberFormatException]]
 */
interface Num {
    operator fun plus(other: Num): Num
    operator fun minus(other: Num): Num
    fun multiply(other: Num): Num
    fun divide(other: Num): /** @throws [CantDivideBy0Exception]*/ Num
    fun percent(other: Num): Num
    fun reverseSign(): Num
    fun floor(): Num
    fun round(decimalPlaces: Int, rounding: RoundingOptions): Num
    enum class RoundingOptions{ Down, Up, Even }
    fun equals(other: Num): Boolean
    fun isZero(): Boolean
    operator fun rem(other: Num): Num
    fun format() : Num

    operator fun div(other: Num): Num = divide(other)
    operator fun times(other: Num): Num = multiply(other)
    infix fun p(other: Num): Num = percent(other)

    fun toStringUnformatted(): String
    fun toStringWithGroupingFormatting(): String
    fun toStringFormatted(formatAfterInput: Boolean, showGrouping: Boolean, forceSign: Boolean, ): String


    companion object { //TODO it has also string res for all of this. what to do?
        const val ALL_INPUT_CHARS = "0123456789.-"
        const val SPECIAL_INPUT_CHARS = ".-"
        const val DIGITS = "0123456789"
        const val DECIMAL_POINT = '.'
        const val UNARY_MINUS = '-'
        const val UNARY_PLUS = '+'
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
        is Float -> NumImpl(BigDecimal(number.toDouble()).toString())
        else -> throw NotImplementedError()
    }
}
fun createZero(): Num = NumImpl("0")


class NumImpl(numberAsString: String) : Num {

    private val numberAsString: String

    override fun plus(other: Num): Num {
        return NumImpl(BigDecimal(numberAsString).plus(BigDecimal(other.toStringUnformatted())).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun minus(other: Num): Num {
        return NumImpl(BigDecimal(numberAsString).minus(BigDecimal(other.toStringUnformatted())).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun multiply(other: Num): Num {
        return NumImpl(BigDecimal(numberAsString).multiply(BigDecimal(other.toStringUnformatted())).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun divide(other: Num): Num {
        if (other.isZero()) {
            throw CantDivideByZeroException()
        }
        return NumImpl(BigDecimal(numberAsString).divide(BigDecimal(other.toStringUnformatted()), 8, RoundingMode.HALF_UP).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun percent(other: Num): Num {
        val percent = BigDecimal(other.toStringUnformatted()).divide(BigDecimal("100"))
        return NumImpl(BigDecimal(numberAsString).multiply(percent).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun reverseSign(): Num {
        return NumImpl(BigDecimal("0").minus(BigDecimal(numberAsString)).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun rem(other: Num): Num {
        return NumImpl(BigDecimal(numberAsString).rem(BigDecimal(other.toStringUnformatted())).toPlainString().stripTrailingZerosAfterDecimalPoint())
    }

    override fun format(): Num {
        return plus(createZero())
    }

    override fun equals(other: Num): Boolean {
        return BigDecimal(numberAsString).compareTo(BigDecimal(other.toStringUnformatted())) == 0
    }

    override fun isZero(): Boolean {
        return equals(createZero())
    }

    override fun round(decimalPlaces: Int, rounding: Num.RoundingOptions): Num {
        val roundingMode = when (rounding) {
            Num.RoundingOptions.Down -> BigDecimal.ROUND_DOWN
            Num.RoundingOptions.Up -> BigDecimal.ROUND_UP
            Num.RoundingOptions.Even -> BigDecimal.ROUND_HALF_EVEN
        }
        return NumImpl(BigDecimal(numberAsString).setScale(decimalPlaces, roundingMode).toPlainString().stripTrailingZerosAfterDecimalPoint()).format()
    }

    override fun floor(): Num {
        val decimalPointLocation = numberAsString.indexOfFirst { it == DecimalPoint.asChar }
        var newNumberAsString = numberAsString
        if (decimalPointLocation != -1) {
            newNumberAsString = numberAsString.slice(0 until decimalPointLocation)
        }
        if (newNumberAsString.isEmpty()) {
            newNumberAsString = "0"
        }

        return toNum(newNumberAsString).format()
    }

    override fun toString(): String = toStringWithGroupingFormatting()

    override fun toStringUnformatted(): String {
        return numberAsString
    }

    override fun toStringWithGroupingFormatting() = toStringWithGroupingFormatting(numberAsString)


    private fun toStringWithGroupingFormatting(numberAsString: String): String {
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

    override fun toStringFormatted(formatAfterInput: Boolean, showGrouping: Boolean, forceSign: Boolean): String {
        var numberAsString = numberAsString
        if (formatAfterInput) {
            numberAsString = toNum(numberAsString).format().toStringUnformatted()
        }
        if (showGrouping) {
            numberAsString = toStringWithGroupingFormatting(numberAsString)
        }
        if (forceSign) {
            numberAsString = tryToAddPositiveSign(numberAsString)
        }
        return numberAsString
    }

    private fun tryToAddPositiveSign(numberAsString: String): String {
        return if (numberAsString[0] != UNARY_MINUS) "+$numberAsString" else numberAsString

    }

    private fun isDigit(char: Char) = Num.DIGITS.contains(char)

    private fun checkIfNumberIsOk(numberAsString: String) {
        //TODo VERY DANGEROUS if number is "e+2323" of something...!!!!
        if (numberAsString.isEmpty()) {
            throw NumberFormatException("string is empty")
        }
        if (!doesStringContainOnlyLegalInputChars(numberAsString)) {
            throw NumberFormatException("not a number")
        }
        if (numberAsString.length == 1 && numberAsString[0] == Num.UNARY_MINUS) {
            throw NumberFormatException("'-' is not a number")
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

    private fun String.stripTrailingZerosAfterDecimalPoint(): String {
        fun List<Wrapper<Char>>.asString() = map { it.obj}.joinToString("","","")

        val number = toString().map { Wrapper(it) }

        val indexOfDecimalPoint = number.indexOfFirst { it.obj == DecimalPoint.asChar}
        if (indexOfDecimalPoint == -1) return number.asString()

        val lastNonZeroDigit = number.indexOfLast {
            number.indexOf(it) > indexOfDecimalPoint && it.obj != Digit.Zero.asChar
        }

        return number.slice(0..(if (lastNonZeroDigit == -1) indexOfDecimalPoint-1 else lastNonZeroDigit)).asString()
    }

    init {
        checkIfNumberIsOk(numberAsString)

        //special cases
        var numberAsString = numberAsString
        if (numberAsString == ".") {
            numberAsString = "0"
        }

        this.numberAsString = numberAsString
    }

}