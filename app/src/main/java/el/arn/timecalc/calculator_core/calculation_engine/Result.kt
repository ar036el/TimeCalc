package el.arn.timecalc.calculator_core.calculation_engine

import el.arn.timecalc.TimeVariable

interface Result

interface ErrorResult : Result

class CantDivideBy0 : ErrorResult
class CantMultiplyTimeQuantities : ErrorResult
class BadFormula : ErrorResult

data class TimeResult(val timeVariable: TimeVariable<Num>): Result

data class NumberResult(val number: Num) : Result

data class MixedResult(val number: Num, val time: TimeVariable<Num>): Result





fun millisToTimeVariable(totalMillis: Num): TimeVariable<Num> {
    return TimeVariable(
        years = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Year),
        months = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Month) % toNum(MONTHS_IN_YEARS),
        weeks = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Week) % toNum(WEEKS_IN_MONTH),
        days = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Day) % toNum(DAYS_IN_WEEK),
        hours = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Hour) % toNum(HOURS_IN_DAY),
        minutes = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Minute) % toNum(MINS_IN_HOUR),
        seconds = TimeUnitConverter.convert(totalMillis, TimeUnit.Milli, TimeUnit.Second) % toNum(SECS_IN_MIN),
        millis = totalMillis % toNum(MILLIS_IN_SEC)
    )
}
@JvmName("timeVariableToMillis1")
fun timeVariableToMillis(timeVariable: TimeVariable<Num>): Num {
    return TimeUnitConverter.convert(timeVariable.years, TimeUnit.Year, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.months, TimeUnit.Month, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.weeks, TimeUnit.Month, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.days, TimeUnit.Week, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.hours, TimeUnit.Day, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.minutes, TimeUnit.Hour, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.seconds, TimeUnit.Minute, TimeUnit.Milli) +
            TimeUnitConverter.convert(timeVariable.millis, TimeUnit.Second, TimeUnit.Milli)
}
fun timeVariableToMillis(timeVariable: TimeVariable<Number>): Num {
    return TimeUnitConverter.convert(toNum(timeVariable.years), TimeUnit.Year, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.months), TimeUnit.Month, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.weeks), TimeUnit.Month, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.days), TimeUnit.Week, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.hours), TimeUnit.Day, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.minutes), TimeUnit.Hour, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.seconds), TimeUnit.Minute, TimeUnit.Milli) +
            TimeUnitConverter.convert(toNum(timeVariable.millis), TimeUnit.Second, TimeUnit.Milli)
}