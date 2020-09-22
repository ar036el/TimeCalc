package el.arn.timecalc.calculator_core.calculation_engine

interface ResultNumeral {
    fun plus(other: ResultNumeral): ResultNumeral
    fun minus(other: ResultNumeral): ResultNumeral
    fun multiply(other: ResultNumeral): ResultNumeral
    fun divide(other: ResultNumeral): /** @throws [CantDivideBy0Exception]*/ ResultNumeral
    fun percent(other: ResultNumeral): ResultNumeral
}

class ResultNumeralSimpleNumber(val number: Num) : ResultNumeral {
    
    override fun plus(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralSimpleNumber(number.plus(other.number))
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMixed(number, other.milliseconds)
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.plus(other.number), other.milliseconds)
            }
            else -> throw NotImplementedError()
        }
    }

    override fun minus(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralSimpleNumber(number.minus(other.number))
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMixed(number, other.milliseconds.reverseSign())
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.minus(other.number), other.milliseconds.reverseSign())
            }
            else -> throw NotImplementedError()
        }
    }

    override fun multiply(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralSimpleNumber(number.multiply(other.number))
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMilliseconds(number.multiply(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.multiply(other.number), number.multiply(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }

    override fun divide(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralSimpleNumber(number.divide(other.number))
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMilliseconds(number.divide(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.divide(other.number), number.divide(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }

    override fun percent(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralSimpleNumber(number.percent(other.number))
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMilliseconds(number.percent(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.percent(other.number), number.percent(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }
}

class ResultNumeralMilliseconds(val milliseconds: Num) : ResultNumeral {

    override fun plus(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(other.number, milliseconds)
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMilliseconds(milliseconds.plus(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(other.number, milliseconds.plus(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }

    override fun minus(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(other.number.reverseSign(), milliseconds)
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMilliseconds(milliseconds.minus(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(other.number.reverseSign(), milliseconds.minus(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }

    override fun multiply(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMilliseconds(milliseconds.multiply(other.number))
            }
            is ResultNumeralMilliseconds -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is ResultNumeralMixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun divide(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMilliseconds(milliseconds.divide(other.number))
            }
            is ResultNumeralMilliseconds -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is ResultNumeralMixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun percent(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMilliseconds(milliseconds.percent(other.number))
            }
            is ResultNumeralMilliseconds -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is ResultNumeralMixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }
}

class ResultNumeralMixed(val number: Num, val milliseconds: Num) : ResultNumeral {

    override fun plus(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(number.plus(other.number), milliseconds)
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMixed(number, milliseconds.plus(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.plus(other.number), milliseconds.plus(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }

    override fun minus(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(number.minus(other.number), milliseconds)
            }
            is ResultNumeralMilliseconds -> {
                ResultNumeralMixed(number, milliseconds.minus(other.milliseconds))
            }
            is ResultNumeralMixed -> {
                ResultNumeralMixed(number.minus(other.number), milliseconds.minus(other.milliseconds))
            }
            else -> throw NotImplementedError()
        }
    }

    override fun multiply(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(number.multiply(other.number), milliseconds.multiply(other.number))
            }
            is ResultNumeralMilliseconds -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is ResultNumeralMixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun divide(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(number.divide(other.number), milliseconds.divide(other.number))
            }
            is ResultNumeralMilliseconds -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is ResultNumeralMixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }

    override fun percent(other: ResultNumeral): ResultNumeral {
        return when (other) {
            is ResultNumeralSimpleNumber -> {
                ResultNumeralMixed(number.percent(other.number), milliseconds.percent(other.number))
            }
            is ResultNumeralMilliseconds -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            is ResultNumeralMixed -> {
                throw CantMultiplyTimeQuantitiesException()
            }
            else -> throw NotImplementedError()
        }
    }
}