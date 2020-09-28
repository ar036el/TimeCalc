package el.arn.timecalc.calculation_engine

import el.arn.timecalc.calculation_engine.atoms.*
import el.arn.timecalc.calculation_engine.symbol.TimeUnit


class TimeExpressionFactory(private val timeExpressionConfig: TimeExpressionConfig) {
    fun createTimeExpression(totalMillis: Num): TimeExpression = TimeExpressionImpl(timeExpressionConfig, totalMillis)
}

class TimeExpressionConfig(
    daysInAMonth: Float,
    daysInAYear: Float
) {
    val MILLIS_IN_SEC = toNum("1000")
    val MILLIS_IN_MIN = toNum("60000")
    val MILLIS_IN_HOUR = toNum("3600000")
    val MILLIS_IN_DAY = toNum("86400000")
    val MILLIS_IN_WEEK = toNum("604800000")
    val DAYS_IN_WEEK = toNum("7")
    val WEEKS_IN_MONTH = (toNum("4"))

    val daysInAMonth = toNum(daysInAMonth)
    val daysInAYear = toNum(daysInAYear)

    val DECIMAL_PLACES_TO_ROUND_FOR_MILLIS = 2
}

interface TimeExpression {
    val totalMillis: Num
    val units: TimeVariable<Num>
    val uncollapsedTime: TimeVariable<Num>
    fun setCollapsed(isCollapsed: TimeVariable<Boolean>)
    fun isCollapsed(timeUnit: TimeUnit): Boolean
}

class TimeExpressionImpl(
    private val config: TimeExpressionConfig,
    override val totalMillis: Num
) : TimeExpression {

    private val totalDaysFloored = (totalMillis / config.MILLIS_IN_DAY).floor()

    private val _uncollapsedTime = calculateTime(TimeVariable{false})
    override val uncollapsedTime: TimeVariable<Num> = _uncollapsedTime

    override var units = TimeVariable(uncollapsedTime)

    private val _isCollapsed = MutableTimeVariable { false }

    override fun setCollapsed(isCollapsed: TimeVariable<Boolean>) {
        if (isCollapsed[TimeUnit.Milli]) { throw InternalError("cannot collapse Milli") }
        units = calculateTime(isCollapsed)
    }

    override fun isCollapsed(timeUnit: TimeUnit) = _isCollapsed[timeUnit]

    private fun calculateTime(isCollapsed: TimeVariable<Boolean>): MutableTimeVariable<Num> {

        var years: Num = createZero()
        var months: Num = createZero()
        var weeks: Num = createZero()
        var days: Num = createZero()
        var hours: Num = createZero()
        var minutes: Num = createZero()
        var seconds: Num = createZero()
        var millis: Num = createZero()


        years = (totalDaysFloored / config.daysInAYear).floor()
        months = ((totalDaysFloored - years * config.daysInAYear) / config.daysInAMonth).floor()

        var millisBuffer = totalMillis
        if (!isCollapsed[TimeUnit.Week]) {
            weeks = (millisBuffer / config.MILLIS_IN_WEEK).floor()
            millisBuffer -= weeks * config.MILLIS_IN_WEEK
            weeks %= config.WEEKS_IN_MONTH

            if (isCollapsed[TimeUnit.Year] && isCollapsed[TimeUnit.Month]) {
                weeks += (months * config.daysInAMonth / config.DAYS_IN_WEEK).floor() + (years * config.daysInAYear / config.DAYS_IN_WEEK).floor()
            } else if (isCollapsed[TimeUnit.Month]) {
                weeks += (months * config.daysInAMonth / config.DAYS_IN_WEEK).floor()
            }

        } else {
            if (isCollapsed[TimeUnit.Year] && isCollapsed[TimeUnit.Month]) {
                //do nothing
            } else if (isCollapsed[TimeUnit.Month]) {
                millisBuffer -= months * config.daysInAMonth * config.MILLIS_IN_DAY
                weeks += (months * config.daysInAMonth / config.DAYS_IN_WEEK).floor()
            } else { //nor year or month are collapsed
                millisBuffer -= months * config.daysInAMonth * config.MILLIS_IN_DAY + years * config.daysInAYear * config.MILLIS_IN_DAY
            }
        }

        if (isCollapsed[TimeUnit.Year]) {
            years = createZero()
        }
        if (isCollapsed[TimeUnit.Month]) {
            months = createZero()
        }

        if (!isCollapsed[TimeUnit.Day]) {
            days = (millisBuffer / config.MILLIS_IN_DAY).floor()
            millisBuffer -= days * config.MILLIS_IN_DAY
        }
        if (!isCollapsed[TimeUnit.Hour]) {
            hours = (millisBuffer / config.MILLIS_IN_HOUR).floor()
            millisBuffer -= hours * config.MILLIS_IN_HOUR
        }
        if (!isCollapsed[TimeUnit.Minute]) {
            minutes = (millisBuffer / config.MILLIS_IN_MIN).floor()
            millisBuffer -= minutes * config.MILLIS_IN_MIN
        }
        if (!isCollapsed[TimeUnit.Second]) {
            seconds = (millisBuffer / config.MILLIS_IN_SEC).floor()
            millisBuffer -= seconds * config.MILLIS_IN_SEC
        }
        if (!isCollapsed[TimeUnit.Milli]) {
            millis = millisBuffer
            millisBuffer -= millis
        }

        if (!millisBuffer.isZero()) {
            throw InternalError()
        }


        return MutableTimeVariable(millis, seconds, minutes, hours, days, weeks, months, years)
    }


}