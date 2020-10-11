package com.arealapps.timecalc.activities.settingsActivity.ui

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import androidx.annotation.IdRes
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.widget.SwitchCompat
import com.arealapps.timecalc.R
import com.arealapps.timecalc.utils.preferences_managers.CalculatorPreferencesManager
import com.arealapps.timecalc.utils.preferences_managers.parts.Preference

class SelectAutoCollapseUnitsDialog(
    private val activity: Activity,
    private val prefsManager: CalculatorPreferencesManager
) {

    init {
        val dialogLayout =
            activity.layoutInflater.inflate(R.layout.dialog_settings_autocollapse_units_select, null) as ViewGroup

        attachAllSwitchesToMatchingPreferences(dialogLayout)

        AlertDialog.Builder(activity)
            .setView(dialogLayout)
            .setPositiveButton(activity.resources.getString(android.R.string.ok), null)
            .show()
    }


    private fun attachAllSwitchesToMatchingPreferences(dialogLayout: ViewGroup) {
        fun findSwitch(@IdRes id: Int): SwitchCompat = dialogLayout.findViewById(id)
        fun findButton(@IdRes id: Int): ViewGroup = dialogLayout.findViewById(id)

        val switches = listOf(
            findSwitch(R.id.autoCollapseDialog_switch_millis),
            findSwitch(R.id.autoCollapseDialog_switch_seconds),
            findSwitch(R.id.autoCollapseDialog_switch_minutes),
            findSwitch(R.id.autoCollapseDialog_switch_hours),
            findSwitch(R.id.autoCollapseDialog_switch_days),
            findSwitch(R.id.autoCollapseDialog_switch_weeks),
            findSwitch(R.id.autoCollapseDialog_switch_months),
            findSwitch(R.id.autoCollapseDialog_switch_years)
        )

        val prefs = listOf(
            prefsManager.autoCollapseMillis,
            prefsManager.autoCollapseSeconds,
            prefsManager.autoCollapseMinutes,
            prefsManager.autoCollapseHours,
            prefsManager.autoCollapseDays,
            prefsManager.autoCollapseWeeks,
            prefsManager.autoCollapseMonths,
            prefsManager.autoCollapseYears
        )

        val buttons = listOf(
            findButton(R.id.autoCollapseDialog_button_millis),
            findButton(R.id.autoCollapseDialog_button_seconds),
            findButton(R.id.autoCollapseDialog_button_minutes),
            findButton(R.id.autoCollapseDialog_button_hours),
            findButton(R.id.autoCollapseDialog_button_days),
            findButton(R.id.autoCollapseDialog_button_weeks),
            findButton(R.id.autoCollapseDialog_button_months),
            findButton(R.id.autoCollapseDialog_button_years),
        )

        switches.forEachIndexed { index, item ->
            item.isChecked = prefs[index].value
            item.setOnCheckedChangeListener { _, isChecked ->
                prefs[index].value = isChecked
            }
        }

        switches.forEachIndexed { index, item ->
            prefs[index].addListener(object: Preference.Listener<Boolean> {
                override fun prefHasChanged(preference: Preference<Boolean>, value: Boolean) {
                    item.isChecked = value
                }
            })
        }

        buttons.forEachIndexed { index, item ->
            item.setOnClickListener {
                switches[index].apply { isChecked = !isChecked }
            }
        }


    }
}