package el.arn.timecalc

import el.arn.timecalc.calculator_core.calculation_engine.*

open class TimeVariable<T>(
    open val millis: T,
    open val seconds: T,
    open val minutes: T,
    open val hours: T,
    open val days: T,
    open val weeks: T,
    open val months: T,
    open val years: T,
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

