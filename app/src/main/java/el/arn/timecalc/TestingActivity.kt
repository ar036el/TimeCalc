package el.arn.timecalc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.calculator_core.calculation_engine.TimeResult
import el.arn.timecalc.calculator_core.calculation_engine.TimeUnit
import java.util.*

class TestingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val timeResultLayout = TimeResultLayout(
            findViewById(R.id.time_result),
            TimeResult(1, 2, 3, 4, 5, 6, 7, 177)
        )
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


