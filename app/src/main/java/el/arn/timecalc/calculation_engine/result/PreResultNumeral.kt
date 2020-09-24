package el.arn.timecalc.calculation_engine.result

import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.symbol.Operator

interface PreResultNumeral {
    fun plus(other: PreResultNumeral): PreResultNumeral
    fun minus(other: PreResultNumeral): PreResultNumeral
    fun multiply(other: PreResultNumeral): PreResultNumeral
    fun divide(other: PreResultNumeral): /** @throws [CantDivideBy0Exception]*/ PreResultNumeral
    fun percent(other: PreResultNumeral): PreResultNumeral
    fun doOperation(operator: Operator, other: PreResultNumeral): PreResultNumeral
    fun isZero():Boolean


    //for private use
    fun updateIfEncountersZero(): PreResultNumeral
}

abstract class AbstractPreResultNumeral : PreResultNumeral {

    override fun doOperation(operator: Operator, other: PreResultNumeral): PreResultNumeral {
        return when (operator) {
            Operator.Plus -> plus(other)
            Operator.Minus -> minus(other)
            Operator.Multiplication -> multiply(other)
            Operator.Division -> divide(other)
            Operator.Percent -> percent(other)
        }
    }

    override fun isZero(): Boolean {
        return when(this) {
            is PreResultNumeral_SimpleNumber -> number.isZero()
            is PreResultNumeral_TimeAsMillis -> milliseconds.isZero()
            is PreResultNumeral_Mixed -> number.isZero() && milliseconds.isZero()
            else -> throw NotImplementedError()
        }
    }

    override fun updateIfEncountersZero(): PreResultNumeral {
        return when (this) {
            is PreResultNumeral_SimpleNumber -> this
            is PreResultNumeral_TimeAsMillis -> this
            is PreResultNumeral_Mixed -> {
                when{
                    number.isZero() -> PreResultNumeral_TimeAsMillis(milliseconds)
                    else -> this
                }
            }
            else -> throw NotImplementedError()
        }
    }
}

class PreResultNumeral_SimpleNumber(val number: Num) : AbstractPreResultNumeral() {
    
    override fun plus(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_SimpleNumber(number.plus(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_Mixed(number, other.milliseconds).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.plus(other.number), other.milliseconds).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun minus(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_SimpleNumber(number.minus(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_Mixed(number, other.milliseconds.reverseSign()).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.minus(other.number), other.milliseconds.reverseSign()).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun multiply(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_SimpleNumber(number.multiply(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_TimeAsMillis(number.multiply(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.multiply(other.number), number.multiply(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun divide(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_SimpleNumber(number.divide(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_TimeAsMillis(number.divide(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.divide(other.number), number.divide(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun percent(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_SimpleNumber(number.percent(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_TimeAsMillis(number.percent(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.percent(other.number), number.percent(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }
}

class PreResultNumeral_TimeAsMillis(val milliseconds: Num) : AbstractPreResultNumeral() {

    override fun plus(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(other.number, milliseconds).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_TimeAsMillis(milliseconds.plus(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(other.number, milliseconds.plus(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun minus(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(other.number.reverseSign(), milliseconds).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_TimeAsMillis(milliseconds.minus(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(other.number.reverseSign(), milliseconds.minus(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun multiply(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_TimeAsMillis(milliseconds.multiply(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is PreResultNumeral_Mixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun divide(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_TimeAsMillis(milliseconds.divide(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is PreResultNumeral_Mixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun percent(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_TimeAsMillis(milliseconds.percent(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is PreResultNumeral_Mixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }
}

class PreResultNumeral_Mixed(val number: Num, val milliseconds: Num) : AbstractPreResultNumeral() {

    override fun plus(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(number.plus(other.number), milliseconds).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_Mixed(number, milliseconds.plus(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.plus(other.number), milliseconds.plus(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun minus(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(number.minus(other.number), milliseconds).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                PreResultNumeral_Mixed(number, milliseconds.minus(other.milliseconds)).updateIfEncountersZero()
            }
            is PreResultNumeral_Mixed -> {
                PreResultNumeral_Mixed(number.minus(other.number), milliseconds.minus(other.milliseconds)).updateIfEncountersZero()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun multiply(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(number.multiply(other.number), milliseconds.multiply(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is PreResultNumeral_Mixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun divide(other: PreResultNumeral): PreResultNumeral {
        if (other.isZero()) {
            throw CantDivideByZeroException()
        }
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(number.divide(other.number), milliseconds.divide(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is PreResultNumeral_Mixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun percent(other: PreResultNumeral): PreResultNumeral {
        return when (other) {
            is PreResultNumeral_SimpleNumber -> {
                PreResultNumeral_Mixed(number.percent(other.number), milliseconds.percent(other.number)).updateIfEncountersZero()
            }
            is PreResultNumeral_TimeAsMillis -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is PreResultNumeral_Mixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

}