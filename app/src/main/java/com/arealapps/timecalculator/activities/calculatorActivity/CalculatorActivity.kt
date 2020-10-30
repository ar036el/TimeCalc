package com.arealapps.timecalculator.activities.calculatorActivity

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageButton
import androidx.appcompat.app.AppCompatActivity
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText.parts.HookedEditText
import com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.HistoryDrawerLayout
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.HistoryDrawerLayoutImpl
import com.arealapps.timecalculator.activities.settingsActivity.SettingsActivity
import com.arealapps.timecalculator.calculation_engine.calculatorCoordinator.CalculatorCoordinator
import com.arealapps.timecalculator.calculation_engine.calculatorCoordinator.CalculatorCoordinatorImpl
import com.arealapps.timecalculator.calculation_engine.expression.Expression
import com.arealapps.timecalculator.calculation_engine.result.MixedResult
import com.arealapps.timecalculator.calculation_engine.result.NumberResult
import com.arealapps.timecalculator.calculation_engine.result.Result
import com.arealapps.timecalculator.calculation_engine.result.TimeResult
import com.arealapps.timecalculator.helpers.android.stringFromRes
import com.arealapps.timecalculator.helpers.native_.LimitedAccessFunction
import com.arealapps.timecalculator.helpers.native_.initOnce
import com.arealapps.timecalculator.utils.tutoriaShowcase.data.Script
import com.arealapps.timecalculator.rootUtils
import com.arealapps.timecalculator.utils.preferences_managers.parts.Preference
import com.arealapps.timecalculator.utils.preferences_managers.parts.PreferencesManager
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.MobileAds


class CalculatorActivity : AppCompatActivity() {

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
        removeListeners()
        super.onDestroy()
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
        if ((!showcaseMgr.wasCompletedAtLeastOnce || showcaseMgr.flagForceInvokeShowcase) && !showcaseMgr.isRunning) {
            showcaseMgr.startShowcase(this)
            showcaseMgr.flagForceInvokeShowcase = false
        }
    }

    private fun tryToInvokeAllLimitedAccessFunctionsOnFocusChanged() {
        initCalculatorCoordinator.invokeIfHasAccess()
        resetCalculator.invokeIfHasAccess()
    }

    private fun tryToLoadABannerAd() {
        val bannerAdView: AdView = findViewById(R.id.calculatorActivity_bannerAdViewBottom)
        if (rootUtils.purchasesManager.purchasedNoAds) {
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


    private var flagWasCalculatorThemeChanged = false
    private val resetCalculator = LimitedAccessFunction({
        if (flagWasCalculatorThemeChanged) {
            flagWasCalculatorThemeChanged = false
            restartActivity()
        } else {
            rootUtils.timeExpressionUtils.config =
                rootUtils.configManager.getTimeExpressionConfig() //TODO VERY RISKY!!! it's very main
            calculatorCoordinator.reset()
        }
    })

    //----

    private fun restartActivity() {
        rootUtils.timeExpressionUtils.config = rootUtils.configManager.getTimeExpressionConfig() //TODO VERY RISKY!!! it's very main
        val intent = intent
        finish()
        startActivity(intent)
    }

    //-----

    private val calculatorPreferencesManagerListener = object : PreferencesManager.Listener {
        override fun prefsHaveChanged(changedPreference: Preference<*>) {
            if (changedPreference == rootUtils.calculatorPreferencesManager.calculatorTheme) {
                flagWasCalculatorThemeChanged = true
            }
            resetCalculator.grantOneAccess()
        }
    }

    private val calculatorCoordinatorListener = object: CalculatorCoordinator.Listener {
        override fun officialCalculationPerformed(expression: Expression, result: Result) {
            historyDrawerLayout.saveItem(expression, result)
            notifyTutorialShowcaseManagerAboutResultChanges(result)
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

    private fun notifyTutorialShowcaseManagerAboutResultChanges(result: Result) {
        val events = mutableSetOf<Script.Events>()
        when (result) {
            is NumberResult -> {
                events.add(Script.Events.CalculatedNumberResult)
            }
            is TimeResult -> {
                events.add(Script.Events.CalculatedTimeResult)
                events.add(Script.Events.TimeBlockAppeared)
            }
            is MixedResult -> {
                events.add(Script.Events.CalculatedMixedResult)
                events.add(Script.Events.TimeBlockAppeared)
            }
            else -> throw NotImplementedError()
        }
        rootUtils.tutorialShowcaseManager.doIfRunning()?.notifyEvents(*events.toTypedArray())
    }

}