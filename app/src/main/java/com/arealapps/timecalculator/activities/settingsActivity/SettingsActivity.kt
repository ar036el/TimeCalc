package com.arealapps.timecalculator.activities.settingsActivity

import android.content.ActivityNotFoundException
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.activities.settingsActivity.ui.SelectAutoCollapseUnitsDialog
import com.arealapps.timecalculator.calculation_engine.timeExpression.TimeExpressionConfig
import com.arealapps.timecalculator.helpers.android.stringFromRes
import com.arealapps.timecalculator.helpers.native_.errorIf
import com.arealapps.timecalculator.rootUtils
import com.arealapps.timecalculator.utils.externalIntentInvoker.GooglePlayStoreAppPageInvoker
import com.arealapps.timecalculator.utils.externalIntentInvoker.UrlInvoker
import com.arealapps.timecalculator.utils.preferences_managers.CalculatorPreferencesManager
import com.arealapps.timecalculator.utils.purchase_manager.widget.PurchaseButton


class SettingsActivity : AppCompatActivity() {

    private lateinit var calcPrefsManager: CalculatorPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        rootUtils.activityInitUtils.initTheme(this)
        setContentView(R.layout.activity_settings)

        calcPrefsManager = rootUtils.calculatorPreferencesManager

        initActivityComponents()
    }


    private fun initActivityComponents() {
        attachComponentToPreference_DaysInAMonthSpinner()
        attachComponentToPreference_DaysInAYearSpinner()
        attachComponentToPreference_VibrateKeysSwitch()
        attachComponentToPreference_CalculatorTheme()

        findViewById<View>(R.id.setting_autoCollapseTimeUnitsButton).setOnClickListener {
            SelectAutoCollapseUnitsDialog(this, calcPrefsManager)
        }

        initActionButtons()
        initGetPremiumPurchaseButton()
    }


    //-----

    private fun initActionButtons() {
        findViewById<View>(R.id.actionItem_rateUs).setOnClickListener {
            GooglePlayStoreAppPageInvoker(this).open()
        }

        findViewById<View>(R.id.actionItem_contactUs).setOnClickListener {
            openIntentSendEmailToDeveloper()
        }

        findViewById<View>(R.id.actionItem_privacyPolicy).setOnClickListener {
            openUrlAppPrivacyPolicy()
        }

        findViewById<View>(R.id.actionItem_restartTutorialShowcase).setOnClickListener {
            restartTutorialShowcase()
        }

    }

    private fun initGetPremiumPurchaseButton() {
        PurchaseButton(
            findViewById(R.id.settingActivity_getPremium),
            rootUtils.purchasesManager.noAds,
            R.string.buyPremium_purchaseButtonAction,
            R.string.buyPremium_alreadyPurchased,
            R.string.buyPremium_purchasingNotAvailableMessage,
            this
        )
    }

    private fun attachComponentToPreference_DaysInAMonthSpinner() {
        val spinner = findViewById<Spinner>(R.id.settingsActivity_daysInAMonthSpinner)
        val pref = calcPrefsManager.daysInAMonth

        val spinnerItemsIndicesWithPrefValues = mapOf(
            0 to TimeExpressionConfig.DaysInAMonthOptions._28,
            1 to TimeExpressionConfig.DaysInAMonthOptions._29,
            2 to TimeExpressionConfig.DaysInAMonthOptions._30,
            3 to TimeExpressionConfig.DaysInAMonthOptions._31,
            4 to TimeExpressionConfig.DaysInAMonthOptions.Average
        )

        spinner.setSelection(spinnerItemsIndicesWithPrefValues.filterValues { it == pref.value }.keys.first())
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                pref.value = spinnerItemsIndicesWithPrefValues.getValue(position)
            }
        }
    }

    private fun attachComponentToPreference_DaysInAYearSpinner() {
        val spinner: Spinner = findViewById(R.id.settingsActivity_daysInAYearSpinner)
        val pref = calcPrefsManager.daysInAYear

        val spinnerItemsIndicesWithPrefValues = mapOf(
            0 to TimeExpressionConfig.DaysInAYearOptions._365,
            1 to TimeExpressionConfig.DaysInAYearOptions._366,
            2 to TimeExpressionConfig.DaysInAYearOptions.Average
        )

        spinner.setSelection(spinnerItemsIndicesWithPrefValues.filterValues { it == pref.value }.keys.first())
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) {}
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                pref.value = spinnerItemsIndicesWithPrefValues.getValue(position)
            }
        }
    }

    private fun attachComponentToPreference_VibrateKeysSwitch() {
        val switch: SwitchCompat = findViewById(R.id.settingsActivity_vibrateKeysSwitch)
        val pref = calcPrefsManager.vibrateKeys

        switch.isChecked = pref.value
        switch.setOnCheckedChangeListener { _, isChecked ->
            pref.value = isChecked
        }
    }

    private fun attachComponentToPreference_CalculatorTheme() {
        val prevButton: ImageButton = findViewById(R.id.setting_calculatorTheme_prev)
        val nextButton: ImageButton = findViewById(R.id.setting_calculatorTheme_next)
        val thumbnail: ImageButton = findViewById(R.id.setting_calculatorTheme_thumbnail)
        val thumbnailResOptions = listOf(
            R.color.theme0_colorPrimary,
            R.color.theme1_colorPrimaryDark,
            R.color.theme2_colorPrimary,
        )
        val pref = calcPrefsManager.calculatorTheme
        val prefPossibleIndices = pref.possibleValues!!


        fun updatePref(newIndex: Int) {
            errorIf { newIndex !in prefPossibleIndices }
            prevButton.isEnabled = (newIndex != 0)
            nextButton.isEnabled = (newIndex != prefPossibleIndices.last())
            thumbnail.setColorFilter(ContextCompat.getColor(this, thumbnailResOptions[newIndex]), android.graphics.PorterDuff.Mode.SRC_IN);
            pref.value = newIndex
        }

        prevButton.setOnClickListener {
            val newValue = pref.value-1
            if (newValue in prefPossibleIndices) {
                updatePref(newValue)
            }
        }
        nextButton.setOnClickListener {
            val newValue = pref.value+1
            if (newValue in prefPossibleIndices) {
                updatePref(newValue)
            }
        }
        thumbnail.setOnClickListener {
            val newValue = pref.value+1
            if (newValue in prefPossibleIndices) {
                updatePref(newValue)
            }
        }

        updatePref(pref.value)
    }

    //----

    private fun openUrlAppPrivacyPolicy() {
        UrlInvoker(stringFromRes(R.string.internal_privacyPolicyURL), this).open()
    }

    private fun openIntentSendEmailToDeveloper() {
        val i = Intent(Intent.ACTION_SEND)
        i.type = "message/rfc822"
        i.putExtra(Intent.EXTRA_EMAIL, arrayOf(stringFromRes(R.string.internal_developerEmail)))
//        i.putExtra(Intent.EXTRA_SUBJECT, "subject of email")
//        i.putExtra(Intent.EXTRA_TEXT, "body of email")
        try {
            startActivity(Intent.createChooser(i, "Send mail..."))
        } catch (ex: ActivityNotFoundException) {
            Toast.makeText(
                this@SettingsActivity,
                "There are no email clients installed.",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    private fun restartTutorialShowcase() {
        rootUtils.tutorialShowcaseManager.flagForceInvokeShowcase = true
        rootUtils.toastManager.showShort(stringFromRes(R.string.settings_restartTutorialShowcase_Message))
    }

}