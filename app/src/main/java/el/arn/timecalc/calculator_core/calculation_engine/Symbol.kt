package el.arn.timecalc.calculator_core.calculation_engine

import el.arn.timecalc.allNext
import el.arn.timecalc.allPrev
import el.arn.timecalc.next
import el.arn.timecalc.prev

interface Symbol { //todo change to class I think
    val asChar: Char
    enum class Types { Digit, DecimalPoint, Operator, Bracket, TimeUnit }
    companion object {
        fun charOf(char: Char): Symbol { //todo too much Exception object instantiations???
            try {
                return Digit.charOf(char)
            } catch (e: NoSuchElementException) { }
            try {
                return DecimalPoint.charOf(char)
            } catch (e: NoSuchElementException) { }
            try {
                return Operator.charOf(char)
            } catch (e: NoSuchElementException) { }
            try {
                return Bracket.charOf(char)
            } catch (e: NoSuchElementException) { }
            try {
                return TimeUnit.charOf(char)
            } catch (e: NoSuchElementException) { }

            throw NoSuchElementException()
        }
    }
}

enum class Digit(override val asChar: Char): Symbol {
    Zero('0'),
    One('1'),
    Two('2'),
    Three('3'),
    Four('4'),
    Five('5'),
    Six('6'),
    Seven('7'),
    Eight('8'),
    Nine('9');
    companion object {
        fun charOf(char: Char): Digit {
            return values().firstOrNull { it.asChar == char } ?: throw NoSuchElementException()
        }
    }
}

object DecimalPoint : Symbol {
    override val asChar = '.'
    fun charOf(char: Char): DecimalPoint {
        return if (char == asChar) this else throw NoSuchElementException()
    }
}

enum class Operator(override val asChar: Char, val type: Types): Symbol {
    Plus('+', Types.Additive),
    Minus('-', Types.Additive),
    Multiplication('*', Types.Multiplicative),
    Division('/', Types.Multiplicative),
    Percent('%', Types.Multiplicative);
    companion object {
        fun charOf(char: Char): Operator {
            return values().firstOrNull { it.asChar == char } ?: throw NoSuchElementException()
        }
    }
    enum class Types { Additive, Multiplicative }
}

enum class Bracket(override val asChar: Char) : Symbol {
    Opening('('),
    Closing(')');
    companion object {
        fun charOf(char: Char): Bracket {
            return values().firstOrNull { it.asChar == char } ?: throw NoSuchElementException()
        }
    }
}

sealed class TimeUnit(override val asChar: Char): Symbol {
    object Milli: TimeUnit('l')
    object Second: TimeUnit('s')
    object Minute: TimeUnit('m')
    object Hour: TimeUnit('h')
    object Day: TimeUnit('d')
    object Week: TimeUnit('w')
    object Month: TimeUnit('o')
    object Year: TimeUnit('y')
    companion object {
        fun charOf(char: Char): TimeUnit {
            return when (char) {
                Milli.asChar -> Milli
                Second.asChar -> Second
                Minute.asChar -> Minute
                Hour.asChar -> Hour
                Day.asChar -> Day
                Week.asChar -> Week
                Month.asChar -> Month
                Year.asChar -> Year
                else -> throw NoSuchElementException("char[$char]")
            }
        }
        val asList: List<TimeUnit> by lazy { listOf(Milli, Second, Minute, Hour, Day, Week, Month, Year) }
    }
    fun next() = asList.next(this)
    fun allNext() = asList.allNext(this)
    fun prev() = asList.prev(this)
    fun allPrev() = asList.allPrev(this)
}


//sealed class Operator: FormulaComponent {
//    object Plus: Operator()
//    object Minus: Operator()
//    object Multiplication: Operator()
//    object Division: Operator()
//    object Percent: Operator()
//    //---
//    object SilentPlus: Operator()
//    object SilentMultiplication: Operator()
//}