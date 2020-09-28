package el.arn.timecalc

import android.text.format.DateUtils
import el.arn.timecalc.calculation_engine.TimeExpressionConfig
import el.arn.timecalc.calculation_engine.TimeExpressionFactory
import el.arn.timecalc.calculation_engine.atoms.MutableTimeVariable
import el.arn.timecalc.calculation_engine.atoms.toNum
import java.text.SimpleDateFormat
import java.util.*


fun main() {

    val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


    val dateStr = "2016-01-24T16:00:00.000Z"
    val date = inputFormat.parse(dateStr)

    val niceDateStr = DateUtils.getRelativeTimeSpanString(
        date.time,
        Calendar.getInstance().timeInMillis,
        DateUtils.MINUTE_IN_MILLIS
    )

}




fun mainTimeExpression() { //todo do it afterwards

    val timeExpression = TimeExpressionFactory(TimeExpressionConfig(28.1f, 365f)).createTimeExpression(
        toNum(
            215002340999
        )
    )

    val collapsed = MutableTimeVariable { false }

    timeExpression.setCollapsed(collapsed)

    println(timeExpression.units)

}