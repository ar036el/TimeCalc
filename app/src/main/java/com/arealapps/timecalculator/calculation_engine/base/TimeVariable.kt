package com.arealapps.timecalculator.calculation_engine.base

import com.arealapps.timecalculator.calculation_engine.symbol.TimeUnit

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

    constructor(timeVariable: TimeVariable<T>) : this(timeVariable.millis, timeVariable.seconds, timeVariable.minutes, timeVariable.hours, timeVariable.days, timeVariable.weeks, timeVariable.months, timeVariable.years, )
    constructor(init: (TimeUnit) -> T) : this(init(TimeUnit.Milli), init(TimeUnit.Second), init(TimeUnit.Minute), init(TimeUnit.Hour), init(TimeUnit.Day), init(TimeUnit.Week), init(TimeUnit.Month), init(TimeUnit.Year))

    protected val map: MutableMap<TimeUnit, T> by lazy { toListPaired().toMap().toMutableMap() }


    operator fun get(timeUnit: TimeUnit) = map.getValue(timeUnit)

    fun toList() = toListPaired().map { it.second }
    fun toListPaired(): List<Pair<TimeUnit, T>> {
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

    override fun toString(): String {
        return "millis[$millis],seconds[$seconds],minutes[$minutes],hours[$hours],days[$days],weeks[$weeks],months[$months],years[$years]"
    }
}

fun <T>TimeVariable<T>.toTimeVariable() = TimeVariable(this)
fun <T>TimeVariable<T>.toMutableTimeVariable() = MutableTimeVariable(this)

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

    constructor(timeVariable: TimeVariable<T>) : this(timeVariable.millis, timeVariable.seconds, timeVariable.minutes, timeVariable.hours, timeVariable.days, timeVariable.weeks, timeVariable.months, timeVariable.years, )
    constructor(init: (TimeUnit) -> T) : this(init(TimeUnit.Milli), init(TimeUnit.Second), init(TimeUnit.Minute), init(TimeUnit.Hour), init(TimeUnit.Day), init(TimeUnit.Week), init(TimeUnit.Month), init(TimeUnit.Year))


    operator fun set(timeUnit: TimeUnit, value: T) {
        map[timeUnit] = value
        when (timeUnit) {
            TimeUnit.Milli -> millis = value
            TimeUnit.Second -> seconds = value
            TimeUnit.Minute -> minutes = value
            TimeUnit.Hour -> hours = value
            TimeUnit.Day -> days = value
            TimeUnit.Week -> weeks = value
            TimeUnit.Month -> months = value
            TimeUnit.Year -> years = value
        }
    }

    override fun toString(): String {
        return "millis[$millis],seconds[$seconds],minutes[$minutes],hours[$hours],days[$days],weeks[$weeks],months[$months],years[$years]"
    }


//    fun set(
//        millis: T? = null,
//        seconds: T? = null,
//        minutes: T? = null,
//        hours: T? = null,
//        days: T? = null,
//        weeks: T? = null,
//        months: T? = null,
//        years: T? = null,
//    ) {
//        millis?.let { this.millis = it }
//        seconds?.let { this.seconds = it }
//        minutes?.let { this.minutes = it }
//        hours?.let { this.hours = it }
//        days?.let { this.days = it }
//        weeks?.let { this.weeks = it }
//        months?.let { this.months = it }
//        years?.let { this.years = it }
//    }
}

