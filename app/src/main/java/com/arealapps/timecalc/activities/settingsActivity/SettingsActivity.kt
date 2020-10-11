package com.arealapps.timecalc.activities.settingsActivity

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.Spinner
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.arealapps.timecalc.R
import com.arealapps.timecalc.activities.settingsActivity.ui.SelectAutoCollapseUnitsDialog
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.externalIntentInvoker.GooglePlayStoreAppPageInvoker
import com.arealapps.timecalc.utils.preferences_managers.CalculatorPreferencesManager


class SettingsActivity : AppCompatActivity() {

    private lateinit var calcPrefsManager: CalculatorPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        calcPrefsManager = rootUtils.calculatorPreferencesManager

        initActivityComponents()
    }

    private fun initActivityComponents() {
        attachComponentToPreference_DaysInAMonthSpinner()
        attachComponentToPreference_DaysInAYearSpinner()
        attachComponentToPreference_VibrateKeysSwitch()


        findViewById<View>(R.id.actionItem_rateUs).setOnClickListener {
            GooglePlayStoreAppPageInvoker(this).open()
        }
        findViewById<View>(R.id.actionItem_moreBlabla).setOnClickListener {
            //todo
        }

        findViewById<View>(R.id.setting_autoCollapseTimeUnitsButton).setOnClickListener {
            SelectAutoCollapseUnitsDialog(this, calcPrefsManager)
        }
    }


    //-----


    private fun attachComponentToPreference_DaysInAMonthSpinner() {
        val spinner = findViewById<Spinner>(R.id.settingsActivity_daysInAMonthSpinner)
        val pref = calcPrefsManager.daysInAMonth

        val spinnerItemsIndicesWithPrefValues = mapOf(
            0 to CalculatorPreferencesManager.DaysInAMonthOptions._28,
            1 to CalculatorPreferencesManager.DaysInAMonthOptions._29,
            2 to CalculatorPreferencesManager.DaysInAMonthOptions._30,
            3 to CalculatorPreferencesManager.DaysInAMonthOptions._31,
            4 to CalculatorPreferencesManager.DaysInAMonthOptions.Average
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
            0 to CalculatorPreferencesManager.DaysInAYearOptions._365,
            1 to CalculatorPreferencesManager.DaysInAYearOptions._366,
            2 to CalculatorPreferencesManager.DaysInAYearOptions.Average
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

}