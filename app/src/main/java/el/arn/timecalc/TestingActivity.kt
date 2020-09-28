package el.arn.timecalc

import android.app.Activity
import android.os.Bundle
import android.text.format.DateUtils
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl
import el.arn.timecalc.calculation_engine.TimeExpressionConfig
import el.arn.timecalc.calculation_engine.TimeExpressionFactory
import el.arn.timecalc.calculation_engine.atoms.MutableTimeVariable
import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.helpers.android.heightByLayoutParams
import el.arn.timecalc.mainActivity.ui.TimeResultLayout
import el.arn.timecalc.mainActivity.ui.TimeResultUIConfig
import el.arn.timecalc.organize_later.testSetInterval
import java.text.SimpleDateFormat
import java.util.*

class TestingActivity : AppCompatActivity() {

    private val timeConverter: TimeConverter = TimeConverterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val timeResultUIConfig = TimeResultUIConfig(true, MutableTimeVariable { false }.apply {
            set(
                TimeUnit.Milli,
                false
            )
        })

        val timeResult = TimeResult(
            TimeExpressionFactory(TimeExpressionConfig(30f, 365f)).createTimeExpression(
                timeConverter.timeVariableToMillis(
                    TimeVariable(toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1)))))

        val timeResultUI = TimeResultLayout(
            findViewById(R.id.timeResultLayout),
            timeResult,
            timeResultUIConfig,
            1000f,
            60f,
            80f
        )

//        var counter = 100f
//        testSetInterval(this, 30) {
//            counter += 1f
//            timeResultUI.customHeight = counter
//        }

//        timeResultUI.customHeight = 1000f

        testSetInterval(this, 50) {
            timeResultUI.maxHeight+=1
        }

        niceDate()
    }


    fun niceDate() {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");


        val dateStr = "2020-01-24T16:00:00.000Z"
        val date = inputFormat.parse(dateStr)

        val niceDateStr = DateUtils.getRelativeTimeSpanString(
            System.currentTimeMillis() - 1200000000000000L,
            Calendar.getInstance().timeInMillis,
            DateUtils.MINUTE_IN_MILLIS
        )

        println("haho $niceDateStr")

        println("haho ${date.time}")

    }

}


