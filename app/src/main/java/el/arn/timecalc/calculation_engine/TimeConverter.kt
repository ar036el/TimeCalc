package el.arn.timecalc.calculation_engine

import el.arn.timecalc.calculation_engine.atoms.*
import el.arn.timecalc.calculation_engine.symbol.TimeUnit

interface TimeConverter {
    fun millisToTimeExpression(totalMillis: Num): TimeExpression<Num>
    fun timeExpressionToMillis(timeExpression: TimeExpression<Num>): Num
    fun convertTimeUnit(quantity: Num, from: TimeUnit, to: TimeUnit): Num

    companion object {
        const val DECIMAL_PLACES_TO_ROUND_FOR_MILLIS = 2
    }
}

class TimeConverterImpl : TimeConverter {
    override fun millisToTimeExpression(totalMillis: Num): TimeExpression<Num> {
        return TimeExpression(
            years = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Year).floor(),
            months = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Month).floor() % toNum(
                MONTHS_IN_YEARS
            ),
            weeks = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Week).floor() % toNum(
                WEEKS_IN_MONTH
            ),
            days = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Day).floor() % toNum(
                DAYS_IN_WEEK
            ),
            hours = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Hour).floor() % toNum(
                HOURS_IN_DAY
            ),
            minutes = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Minute).floor() % toNum(
                MINS_IN_HOUR
            ),
            seconds = convertTimeUnit(totalMillis, TimeUnit.Milli, TimeUnit.Second).floor() % toNum(
                SECS_IN_MIN
            ),
            millis = totalMillis.round(TimeConverter.DECIMAL_PLACES_TO_ROUND_FOR_MILLIS, Num.RoundingOptions.Even) % toNum(MILLIS_IN_SEC)
        )
    }

    override fun timeExpressionToMillis(timeExpression: TimeExpression<Num>): Num {
        return convertTimeUnit(timeExpression.years, TimeUnit.Year, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.months, TimeUnit.Month, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.weeks, TimeUnit.Week, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.days, TimeUnit.Day, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.hours, TimeUnit.Hour, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.minutes, TimeUnit.Minute, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.seconds, TimeUnit.Second, TimeUnit.Milli) +
                convertTimeUnit(timeExpression.millis, TimeUnit.Milli, TimeUnit.Milli)
    }

    //todo change "seconds and minutes in blocks to sec and min??"
    private val millisInSec = MILLIS_IN_SEC.toLong()
    private val millisInMin = millisInSec * SECS_IN_MIN
    private val millisInHour = millisInMin * MINS_IN_HOUR
    private val millisInDay = millisInHour * HOURS_IN_DAY
    private val millisInWeek = millisInDay * DAYS_IN_WEEK
    private val millisInMonth = millisInWeek * WEEKS_IN_MONTH
    private val millisInYear = millisInMonth * MONTHS_IN_YEARS

    override fun convertTimeUnit(quantity: Num, from: TimeUnit, to: TimeUnit): Num {
        return toMillis(quantity, from) / toMillis(toNum(1), to)
    }

    private fun toMillis(quantity: Num, timeUnit: TimeUnit): Num {
        val factor = when (timeUnit) {
            is TimeUnit.Milli -> 1
            is TimeUnit.Second -> millisInSec
            is TimeUnit.Minute -> millisInMin
            is TimeUnit.Hour -> millisInHour
            is TimeUnit.Day -> millisInDay
            is TimeUnit.Week -> millisInWeek
            is TimeUnit.Month -> millisInMonth
            is TimeUnit.Year -> millisInYear
        }
        return quantity * toNum(factor)
    }
}