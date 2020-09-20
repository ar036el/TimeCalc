package el.arn.timecalc.calculator_core.calculation_engine

const val MILLIS_IN_SEC = 1000
const val SECS_IN_MIN = 60
const val MINS_IN_HOUR = 60
const val HOURS_IN_DAY = 24
const val DAYS_IN_WEEK = 7
const val WEEKS_IN_MONTH = 4
const val MONTHS_IN_YEARS = 12


object TimeUnitConverter {
    //todo change "seconds and minutes in blocks to sec and min??"
    private const val millisInSec = MILLIS_IN_SEC.toLong()
    private const val millisInMin = millisInSec * SECS_IN_MIN
    private const val millisInHour = millisInMin * MINS_IN_HOUR
    private const val millisInDay = millisInHour * HOURS_IN_DAY
    private const val millisInWeek = millisInDay * DAYS_IN_WEEK
    private const val millisInMonth = millisInWeek * WEEKS_IN_MONTH
    private const val millisInYear = millisInMonth * MONTHS_IN_YEARS

    fun convert(num: Long, from: TimeUnit, to: TimeUnit): Double {
        return toMillis(num, from).toDouble() / toMillis(1, to)
    }

    private fun toMillis(num: Long, timeUnit: TimeUnit): Long {
        return when (timeUnit) {
            is TimeUnit.Milli -> 1
            is TimeUnit.Second -> millisInSec
            is TimeUnit.Minute -> millisInMin
            is TimeUnit.Hour -> millisInHour
            is TimeUnit.Day -> millisInDay
            is TimeUnit.Week -> millisInWeek
            is TimeUnit.Month -> millisInMonth
            is TimeUnit.Year -> millisInYear
        } * num
    }
}

