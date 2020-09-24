package el.arn.timecalc

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.calculation_engine.TimeConverter
import el.arn.timecalc.calculation_engine.TimeConverterImpl
import el.arn.timecalc.calculation_engine.atoms.MutableTimeVariable
import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.calculation_engine.atoms.toNum
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.mainActivity.ui.TimeResultUI
import el.arn.timecalc.mainActivity.ui.TimeResultUIConfig

class TestingActivity : AppCompatActivity() {

    val timeConverter: TimeConverter = TimeConverterImpl()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_testing)

        val timeResultUIConfig = TimeResultUIConfig(true, MutableTimeVariable{false}.apply { set(TimeUnit.Milli, false) })

        val timeResultUI = TimeResultUI(
            findViewById(R.id.time_result),
            TimeResult(
                timeConverter.timeVariableToMillis(TimeVariable(toNum(0), toNum(0), toNum(1), toNum(1), toNum(0), toNum(1), toNum(0), toNum(1)))), timeResultUIConfig)

    }

}


