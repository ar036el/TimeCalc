package el.arn.timecalc.calculator_core.calculation_engine

interface Number {
    fun plus(number: Number): Number
    fun minus(number: Number): Number
    fun multiply(number: Number): Number
    fun divide(number: Number): Number
    fun percent(number: Number): Number

    fun toStringUnformatted(): String //todo toStringNoFormatting

    companion object {
        const val ALL_INPUT_CHARS = "0123456789.-"
        const val SPECIAL_INPUT_CHARS = ".-"
        const val DIGITS = "0123456789"
        const val DECIMAL_POINT = '.'
        const val UNARY_MINUS = '-'
    }
}

object NumberFactory { //todo should I use or is overkill? it's mainly for not instantiating 'impl's
    fun createNumber(numberAsString: String): Number = NumberImpl(numberAsString)
}

class NumberImpl(numberAsString: String) : Number {

    private val numberAsString: String

    override fun plus(number: Number): Number {
        TODO("Not yet implemented")
    }

    override fun minus(number: Number): Number {
        TODO("Not yet implemented")
    }

    override fun multiply(number: Number): Number {
        TODO("Not yet implemented")
    }

    override fun divide(number: Number): Number {
        TODO("Not yet implemented")
    }

    override fun percent(number: Number): Number {
        TODO("Not yet implemented")
    }

    override fun toStringUnformatted(): String {
        return numberAsString
    }


    private fun checkIfNumberIsOk(numberAsString: String) {
        if (numberAsString.isEmpty()) {
            throw NumberFormatException("string is empty")
        }
        if (!doesStringContainOnlyLegalInputChars(numberAsString)) {
            throw NumberFormatException("not a number")
        }
        if (numberAsString.length == 1 && numberAsString[0] in Number.SPECIAL_INPUT_CHARS) {
            throw NumberFormatException("'.' or '_' is not a number")
        }
        val totalCharOccurrencesOfUnaryMinus = getTotalCharsOfType(numberAsString, Number.UNARY_MINUS)
        if (totalCharOccurrencesOfUnaryMinus > 1) {
            throw NumberFormatException("only one UnaryMinus is allowed")
        }
        if (totalCharOccurrencesOfUnaryMinus == 1 && numberAsString[0] != Number.UNARY_MINUS) {
            throw NumberFormatException("UnaryMinus is not in legal position")
        }
        if (getTotalCharsOfType(numberAsString, Number.DECIMAL_POINT) > 1) {
            throw NumberFormatException("only one DecimalPoint is allowed")
        }
    }

    private fun doesStringContainOnlyLegalInputChars(number: String): Boolean {
        val allLegalInputChars = Number.ALL_INPUT_CHARS.toCharArray()
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