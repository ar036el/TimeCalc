package el.arn.timecalc

import android.os.Bundle
import android.text.format.DateUtils
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl
import el.arn.timecalc.calculation_engine.TimeExpressionConfig
import el.arn.timecalc.calculation_engine.TimeExpressionFactory
import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.mainActivity.ui.ResultLayoutManager
import el.arn.timecalc.organize_later.testSetInterval
import java.text.SimpleDateFormat
import java.util.*

class TestingActivity : AppCompatActivity() {

    private val timeConverter: TimeConverter = TimeConverterImpl()

    private lateinit var resultLayoutManager: ResultLayoutManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val timeResult = TimeResult(
            TimeExpressionFactory(TimeExpressionConfig(30f, 365f)).createTimeExpression(
                timeConverter.timeVariableToMillis(
                    TimeVariable(toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1)))))

        resultLayoutManager = ResultLayoutManager(
            findViewById(R.id.resultLayout),
            timeResult,
            rootUtils.configManager.getConfigForTimeResultLayoutManager(),
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
            resultLayoutManager.maxHeight+=1
        }

        niceDate()
    }


    val timeVars = listOf(
        TimeVariable(toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1)),
        TimeVariable(toNum(0), toNum(213), toNum(0), toNum(0), toNum(0), toNum(1), toNum(1), toNum(1)),
        TimeVariable(toNum(14), toNum(5), toNum(11), toNum(0), toNum(0), toNum(1), toNum(0), toNum(0)),
        TimeVariable(toNum(0), toNum(0), toNum(0), toNum(0), toNum(0), toNum(0), toNum(10), toNum(33)),
        TimeVariable(toNum(771), toNum(2), toNum(0), toNum(0), toNum(0), toNum(0), toNum(10), toNum(33)),
        TimeVariable(toNum(0), toNum(213), toNum(0), toNum(0), toNum(0), toNum(1), toNum(1), toNum(1)),
        TimeVariable(toNum(14), toNum(5), toNum(11), toNum(0), toNum(0), toNum(1), toNum(0), toNum(0)),
        TimeVariable(toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1), toNum(1)),
        TimeVariable(toNum(0), toNum(0), toNum(0), toNum(0), toNum(0), toNum(0), toNum(10), toNum(33)),
        TimeVariable(toNum(771), toNum(2), toNum(0), toNum(0), toNum(0), toNum(0), toNum(10), toNum(33)),
    )
    var currentVar = 0
    fun changeTimeResult(view: View) {
        val timeResult = TimeResult(
            TimeExpressionFactory(TimeExpressionConfig(30f, 365f)).createTimeExpression(
                timeConverter.timeVariableToMillis(timeVars[currentVar++])
            ))
        resultLayoutManager.updateResult(timeResult)

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


