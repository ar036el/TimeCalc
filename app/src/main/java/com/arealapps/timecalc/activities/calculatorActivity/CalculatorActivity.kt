package com.arealapps.timecalc.activities.calculatorActivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.arealapps.timecalc.R
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.expressionInputText.parts.HookedEditText
import com.arealapps.timecalc.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalc.activities.calculatorActivity.ui.historyManager.HistoryDrawerLayout
import com.arealapps.timecalc.activities.calculatorActivity.ui.historyManager.HistoryDrawerLayoutImpl
import com.arealapps.timecalc.activities.settingsActivity.SettingsActivity
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.CalculatorCoordinator
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.CalculatorCoordinatorImpl
import com.arealapps.timecalc.calculation_engine.expression.Expression
import com.arealapps.timecalc.calculation_engine.result.Result
import com.arealapps.timecalc.helpers.android.stringFromRes
import com.arealapps.timecalc.helpers.native_.LimitedAccessFunction
import com.arealapps.timecalc.helpers.native_.initOnce
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.preferences_managers.parts.Preference
import com.arealapps.timecalc.utils.preferences_managers.parts.PreferencesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class CalculatorActivity : AppCompatActivity() {

    private lateinit var expressionEditTextView: HookedEditText
    private lateinit var resultLayout: ResultLayout


    private var calculatorCoordinator: CalculatorCoordinator by initOnce()
    private var historyDrawerLayout: HistoryDrawerLayout by initOnce()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootUtils.activityInitUtils.initTheme(this)
        setContentView(R.layout.activity_calculator)

        initMobileAds()
        initHistoryDrawerLayout()
        initCalculatorCoordinator.grantOneAccess()
        addListeners()

        findViewById<ImageButton>(R.id.calculator_settingsButton).setOnClickListener {
            openSettingsActivity()
        }
        findViewById<ImageButton>(R.id.calculator_showHistoryButton).setOnClickListener {
            historyDrawerLayout.openDrawer()
        }
    }

    override fun onResume() {
        super.onResume()
        tryToLoadABannerAd()
        tryToStartTutorialShowcase()
    }

    override fun onDestroy() {
        super.onDestroy()
        removeListeners()
    }

    override fun onBackPressed() {
        moveTaskToBack(true)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        tryToInvokeAllLimitedAccessFunctionsOnFocusChanged()
    }

    //--------

    private fun tryToStartTutorialShowcase() {
        val showcaseMgr = rootUtils.tutorialShowcaseManager
        if (!showcaseMgr.wasCompletedAtLeastOnce && !showcaseMgr.isRunning) {
            showcaseMgr.start(this)
        }
    }

    private fun tryToInvokeAllLimitedAccessFunctionsOnFocusChanged() {
        initCalculatorCoordinator.invokeIfHasAccess()
        resetCalculator.invokeIfHasAccess()
        restartActivity.invokeIfHasAccess()
    }

    private fun tryToLoadABannerAd() {
        val bannerAdView: AdView = findViewById(R.id.calculatorActivity_bannerAdViewBottom)
        if (true) {
            bannerAdView.visibility = View.GONE
            return
        }
        bannerAdView.visibility = View.VISIBLE
        val adRequest = AdRequest.Builder().build()
        bannerAdView.loadAd(adRequest)
    }

    private fun initMobileAds() {
        MobileAds.initialize(this)
//        updateBannerAdViewsVisibility()
    }

    private fun addListeners() {
        rootUtils.calculatorPreferencesManager.addListener(calculatorPreferencesManagerListener)
    }

    private fun removeListeners() {
        rootUtils.calculatorPreferencesManager.removeListener(calculatorPreferencesManagerListener)
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

    private val initCalculatorCoordinator = LimitedAccessFunction({
        calculatorCoordinator = CalculatorCoordinatorImpl(this)
        calculatorCoordinator.addListener(calculatorCoordinatorListener)
    })

    private val resetCalculator = LimitedAccessFunction({
        rootUtils.timeExpressionUtils.config = rootUtils.configManager.getTimeExpressionConfig() //TODO VERY RISKY!!! it's very main
        calculatorCoordinator.reset()
    })

    private val restartActivity = LimitedAccessFunction({
        rootUtils.timeExpressionUtils.config = rootUtils.configManager.getTimeExpressionConfig() //TODO VERY RISKY!!! it's very main
        val intent = intent
        finish()
        startActivity(intent)
    })

    private val calculatorPreferencesManagerListener = object : PreferencesManager.Listener {
        override fun prefsHaveChanged(changedPreference: Preference<*>) {
            if (changedPreference == rootUtils.calculatorPreferencesManager.calculatorTheme) {
                restartActivity.grantOneAccess()
                resetCalculator.removeAccesses()
            } else {
                restartActivity.removeAccesses()
                resetCalculator.grantOneAccess()
            }
        }
    }

    private val calculatorCoordinatorListener = object: CalculatorCoordinator.Listener {
        override fun officialCalculationPerformed(expression: Expression, result: Result) {
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