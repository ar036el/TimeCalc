/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalc.utils.preferences_managers

import android.content.Context
import android.content.SharedPreferences
import com.arealapps.timecalc.R
import com.arealapps.timecalc.appRoot
import com.arealapps.timecalc.calculation_engine.timeExpression.TimeExpressionConfig
import com.arealapps.timecalc.helpers.android.stringFromRes
import com.arealapps.timecalc.utils.preferences_managers.parts.PreferencesManagerImpl

class CalculatorPreferencesManager : PreferencesManagerImpl(getSharedPreferencesInstance()) {
    companion object {
        private fun getSharedPreferencesInstance(): SharedPreferences{
            return appRoot.getSharedPreferences(stringFromRes(R.string.internal_prefs_main), Context.MODE_PRIVATE)
        }
    }

    val daysInAMonth = createEnumPref(stringFromRes(R.string.internal_prefs_main_daysInAMonth), TimeExpressionConfig.DaysInAMonthOptions.values() , TimeExpressionConfig.DaysInAMonthOptions._30)
    val daysInAYear = createEnumPref(stringFromRes(R.string.internal_prefs_main_daysInAYear), TimeExpressionConfig.DaysInAYearOptions.values() , TimeExpressionConfig.DaysInAYearOptions._365)

    val vibrateKeys = createBooleanPref(stringFromRes(R.string.internal_prefs_main_vibrateKeys), true)

    val autoCollapseMillis = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_millis), false)
    val autoCollapseSeconds = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_seconds), false)
    val autoCollapseMinutes = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_minutes), false)
    val autoCollapseHours = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_hours), false)
    val autoCollapseDays = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_days), false)
    val autoCollapseWeeks = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_weeks), false)
    val autoCollapseMonths = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_months), false)
    val autoCollapseYears = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_years), false)

    val calculatorTheme = createIntPref(stringFromRes(R.string.internal_prefs_main_calculatorTheme), 0..2, 0)


}