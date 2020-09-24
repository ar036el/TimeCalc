package el.arn.timecalc.calculation_engine.atoms

import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.helpers.native_.allNext
import el.arn.timecalc.helpers.native_.allPrev
import el.arn.timecalc.helpers.native_.next
import el.arn.timecalc.helpers.native_.prev

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

    fun prev(item: T): T? = toListPaired().map { it.second }.prev(item)
    fun next(item: T): T? = toListPaired().map { it.second }.next(item)
    fun allNext(item: T): List<T> = toListPaired().map { it.second }.allNext(item)
    fun allPrev(item: T): List<T> = toListPaired().map { it.second }.allPrev(item)

    operator fun get(timeUnit: TimeUnit) = map.getValue(timeUnit)

    protected val map: MutableMap<TimeUnit, T> by lazy { toListPaired().toMap().toMutableMap() }

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

    override fun toString(): String {
        return "millis[$millis],seconds[$seconds],minutes[$minutes],hours[$hours],days[$days],weeks[$weeks],months[$months],years[$years]"
    }

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

    constructor(init: () -> T) :this(init(), init(), init(), init(), init(), init(), init(), init())

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

