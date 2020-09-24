package el.arn.timecalc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.calculation_engine.atoms.TimeExpression
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.mainActivity.ui.TimeResultLayoutMaker

class TestingActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

//        val timeResultLayout = TimeResultLayoutMaker(
//            findViewById(R.id.time_result),
//            TimeResult(
//                millisToTimeExpression(timeExpressionToMillis(TimeExpression(toNum(0), toNum(0), toNum(0), toNum(14), toNum(5), toNum(0), toNum(0), toNum(0)))))
//        )
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


