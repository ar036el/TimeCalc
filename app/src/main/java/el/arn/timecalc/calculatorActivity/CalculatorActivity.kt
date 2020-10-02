package el.arn.timecalc.calculatorActivity

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.R
import el.arn.timecalc.appRoot
import el.arn.timecalc.calculation_engine.CalculatorCoordinatorImpl
import el.arn.timecalc.helpers.native_.LimitedAccessFunction
import el.arn.timecalc.calculatorActivity.ui.expressionInputText.parts.HookedEditText
import el.arn.timecalc.calculatorActivity.ui.ResultLayoutManager.ResultLayoutManager
import el.arn.timecalc.SettingsActivity

class CalculatorActivity : AppCompatActivity() {

    private lateinit var expressionEditTextView: HookedEditText
    private lateinit var resultLayoutManager: ResultLayoutManager

    private val calculatorCoordinator by lazy { appRoot.calculatorCoordinator }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        initCalculator.grantOneAccess()

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            openSettingsActivity()
        }

    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }


    private fun openSettingsActivity() {
        val settingsActivity = Intent(this, SettingsActivity::class.java)
        startActivity(settingsActivity)
    }

    var doOnCalculatorSymbolButtonClick: ((Button) -> Unit)? = null
    fun onCalculatorSymbolButtonClick(view: View) {
        doOnCalculatorSymbolButtonClick?.invoke(view as Button)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        initCalculator.invokeIfHasAccess()
    }

    private val initCalculator = LimitedAccessFunction({
        appRoot.calculatorCoordinator = CalculatorCoordinatorImpl(this)
    })

}