package el.arn.timecalc

import el.arn.timecalc.calculator_core.calculation_engine.*

open class TimeVariable<T>(
    open var millis: T,
    open var seconds: T,
    open var minutes: T,
    open var hours: T,
    open var days: T,
    open var weeks: T,
    open var months: T,
    open var years: T,
) {

    val asList: List<Pair<TimeUnit, T>> get() {
        val list = listOf(
            TimeUnit.Milli to millis,
            TimeUnit.Second to seconds,
            TimeUnit.Minute to minutes,
            TimeUnit.Hour to hours,
            TimeUnit.Day to days,
            TimeUnit.Week to weeks,
            TimeUnit.Month to months,
            TimeUnit.Year to years
        )
        if (list.size != TimeUnit.asList.size) { throw InternalError("lists not 1:1 mapped") }
        return list
    }

    operator fun get(timeUnit: TimeUnit) = map.getValue(timeUnit)

    protected val map: MutableMap<TimeUnit, T> by lazy { asList.toMap().toMutableMap() }

    constructor(timeVariable: TimeVariable<T>) : this(
        timeVariable.millis,
        timeVariable.seconds,
        timeVariable.minutes,
        timeVariable.hours,
        timeVariable.days,
        timeVariable.weeks,
        timeVariable.months,
        timeVariable.years,
    )

}

private fun asFullTime(totalMillis: Long) {
    val years = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Year).toLong()
    val months = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Month).toLong() % MONTHS_IN_YEARS
    val weeks = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Week).toLong() % WEEKS_IN_MONTH
    val days = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Day).toLong() % DAYS_IN_WEEK
    val hours = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Hour).toLong() % HOURS_IN_DAY
    val minutes = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Minute).toLong() % MINS_IN_HOUR
    val seconds = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Second).toLong() % SECS_IN_MIN
    val milliseconds = totalMillis % MILLIS_IN_SEC
}

class MutableTimeVariable<T>(
    override var millis: T,
    override var seconds: T,
    override var minutes: T,
    override var hours: T,
    override var days: T,
    override var weeks: T,
    override var months: T,
    override var years: T,
) : TimeVariable<T>(millis, seconds, minutes, hours, days, weeks, months, years) {
    fun set(
        millis: T? = null,
        seconds: T? = null,
        minutes: T? = null,
        hours: T? = null,
        days: T? = null,
        weeks: T? = null,
        months: T? = null,
        years: T? = null,
    ) {
        millis?.let { this.millis = it }
        seconds?.let { this.seconds = it }
        minutes?.let { this.minutes = it }
        hours?.let { this.hours = it }
        days?.let { this.days = it }
        weeks?.let { this.weeks = it }
        months?.let { this.months = it }
        years?.let { this.years = it }
    }
    operator fun set(timeUnit: TimeUnit, value: T) {
        map[timeUnit] = value
    }

    constructor(timeVariable: TimeVariable<T>) : this(
        timeVariable.millis,
        timeVariable.seconds,
        timeVariable.minutes,
        timeVariable.hours,
        timeVariable.days,
        timeVariable.weeks,
        timeVariable.months,
        timeVariable.years,
    )
}

