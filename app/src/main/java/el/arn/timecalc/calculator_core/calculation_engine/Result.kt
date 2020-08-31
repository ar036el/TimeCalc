package el.arn.timecalc.calculator_core.calculation_engine

interface Result

interface ErrorResult : Result

class CantDivideBy0 : ErrorResult
class BadFormula : ErrorResult

//data class NumberResult(val result: NumberFormulaFragment) : Result
//
//data class TimeResult(
//    val millis: TimeFormulaFragment<TimeUnit.Milli>? = null,
//    val seconds: TimeFormulaFragment<TimeUnit.Second>? = null,
//    val minutes: TimeFormulaFragment<TimeUnit.Minute>? = null,
//    val hours: TimeFormulaFragment<TimeUnit.Hour>? = null,
//    val days: TimeFormulaFragment<TimeUnit.Day>? = null,
//    val weeks: TimeFormulaFragment<TimeUnit.Week>? = null,
//    val months: TimeFormulaFragment<TimeUnit.Month>? = null,
//    val years: TimeFormulaFragment<TimeUnit.Year>? = null,
//) : Result {
//    val asList = createList()
//
//    private fun createList(): List<TimeFormulaFragment<out TimeUnit>> {
//        val list = mutableListOf<TimeFormulaFragment<out TimeUnit>>()
//        millis?.let { list.add(it) }
//        seconds?.let { list.add(it) }
//        minutes?.let { list.add(it) }
//        hours?.let { list.add(it) }
//        days?.let { list.add(it) }
//        weeks?.let { list.add(it) }
//        months?.let { list.add(it) }
//        years?.let { list.add(it) }
//        return list.toList()
//    }
//}