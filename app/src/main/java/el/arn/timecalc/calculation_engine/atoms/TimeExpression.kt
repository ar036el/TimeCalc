package el.arn.timecalc.calculation_engine.atoms

import el.arn.timecalc.calculation_engine.symbol.TimeUnit

open class TimeExpression<T>(
    open val millis: T,
    open val seconds: T,
    open val minutes: T,
    open val hours: T,
    open val days: T,
    open val weeks: T,
    open val months: T,
    open val years: T,
) {

    fun toList(): List<Pair<TimeUnit, T>> {
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
        if (list.size != TimeUnit.asList.size) { throw InternalError("lists are not 1:1 mapped") }
        return list
    }

    operator fun get(timeUnit: TimeUnit) = map.getValue(timeUnit)

    protected val map: MutableMap<TimeUnit, T> by lazy { toList().toMap().toMutableMap() }

    constructor(timeExpression: TimeExpression<T>) : this(
        timeExpression.millis,
        timeExpression.seconds,
        timeExpression.minutes,
        timeExpression.hours,
        timeExpression.days,
        timeExpression.weeks,
        timeExpression.months,
        timeExpression.years,
    )

    override fun toString(): String {
        return "millis[$millis],seconds[$seconds],minutes[$minutes],hours[$hours],days[$days],weeks[$weeks],months[$months],years[$years]"
    }

}

class MutableTimeExpression<T>(
    override var millis: T,
    override var seconds: T,
    override var minutes: T,
    override var hours: T,
    override var days: T,
    override var weeks: T,
    override var months: T,
    override var years: T,
) : TimeExpression<T>(millis, seconds, minutes, hours, days, weeks, months, years) {
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

    constructor(timeExpression: TimeExpression<T>) : this(
        timeExpression.millis,
        timeExpression.seconds,
        timeExpression.minutes,
        timeExpression.hours,
        timeExpression.days,
        timeExpression.weeks,
        timeExpression.months,
        timeExpression.years,
    )
}

