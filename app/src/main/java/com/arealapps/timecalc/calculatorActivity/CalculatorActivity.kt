package com.arealapps.timecalc.calculatorActivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.arealapps.timecalc.R
import com.arealapps.timecalc.SettingsActivity
import com.arealapps.timecalc.calculation_engine.CalculatorCoordinator
import com.arealapps.timecalc.calculation_engine.CalculatorCoordinatorImpl
import com.arealapps.timecalc.calculation_engine.expression.Expression
import com.arealapps.timecalc.calculation_engine.result.Result
import com.arealapps.timecalc.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalc.calculatorActivity.ui.calculator.expressionInputText.parts.HookedEditText
import com.arealapps.timecalc.helpers.android.stringFromRes
import com.arealapps.timecalc.helpers.native_.LimitedAccessFunction
import com.arealapps.timecalc.helpers.native_.initOnce
import com.arealapps.timecalc.calculatorActivity.ui.historyManager.HistoryDrawerLayout
import com.arealapps.timecalc.calculatorActivity.ui.historyManager.HistoryDrawerLayoutImpl
import com.arealapps.timecalc.rootUtils


class CalculatorActivity : AppCompatActivity() {

    private lateinit var expressionEditTextView: HookedEditText
    private lateinit var resultLayout: ResultLayout


    private var calculatorCoordinator: CalculatorCoordinator by initOnce()
    private var historyDrawerLayout: HistoryDrawerLayout by initOnce()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_calculator)

        initHistoryDrawerLayout()
        initCalculatorCoordinator.grantOneAccess()

        findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
            openSettingsActivity()
        }
        findViewById<ImageButton>(R.id.showHistoryButton).setOnClickListener {
            historyDrawerLayout.openDrawer()
        }
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    private fun initHistoryDrawerLayout() {
        historyDrawerLayout = HistoryDrawerLayoutImpl(this)
        historyDrawerLayout.addListener(historyLayoutManagerListener)
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
        initCalculatorCoordinator.invokeIfHasAccess()
    }

    private val initCalculatorCoordinator = LimitedAccessFunction({
        calculatorCoordinator = CalculatorCoordinatorImpl(this)
        calculatorCoordinator.addListener(calculatorCoordinatorListener)
    })

    private val calculatorCoordinatorListener = object: CalculatorCoordinator.Listener {
        override fun calculationPerformed(expression: Expression, result: Result) {
            historyDrawerLayout.saveItem(expression, result)
        }
    }

    private val historyLayoutManagerListener = object: HistoryDrawerLayout.Listener {
        override fun displayItemInCalculator(expressionAsString: String, resultAsString: String) {
            historyDrawerLayout.closeDrawer()
            calculatorCoordinator.loadExpression(expressionAsString)
        }

        override fun copyExpression(expressionAsString: String) {
            copyTextToClipboard(expressionAsString)
            rootUtils.toastManager.showShort(stringFromRes(R.string.expressionCopyMessage))
        }

        override fun copyResult(resultAsString: String) {
            copyTextToClipboard(resultAsString)
            rootUtils.toastManager.showShort(stringFromRes(R.string.resultCopyMessage))
        }

    }

    private fun copyTextToClipboard(text: String) {
        val clipboard = getSystemService(CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", text)
        clipboard.setPrimaryClip(clip)
    }
}