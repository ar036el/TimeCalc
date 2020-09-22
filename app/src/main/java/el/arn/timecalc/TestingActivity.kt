package el.arn.timecalc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.calculator_core.calculation_engine.TimeResult
import el.arn.timecalc.calculator_core.calculation_engine.TimeUnit
import el.arn.timecalc.calculator_core.calculation_engine.millisToTimeVariable
import el.arn.timecalc.calculator_core.calculation_engine.timeVariableToMillis
import java.util.*

class TestingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val timeResultLayout = TimeResultLayout(
            findViewById(R.id.time_result),
            TimeResult(
                millisToTimeVariable(timeVariableToMillis(TimeVariable(0, 0, 0, 14, 5, 0, 0, 0)))))
//
//        var percent = 0
//        Timer().scheduleAtFixedRate(object : TimerTask() {
//            override fun run() {
//                percent++
//                timeResultLayout.setPositionFor(TimeUnit.Month, (percent%100)/100f)
//            }
//        }, 0, 50)

    }

}


