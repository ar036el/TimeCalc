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
import com.arealapps.timecalc.helpers.android.stringFromRes
import com.arealapps.timecalc.helpers.native_.EnumWithId
import com.arealapps.timecalc.utils.preferences_managers.parts.PreferencesManagerGenericImpl

class CalculatorPreferencesManager : PreferencesManagerGenericImpl(getSharedPreferencesInstance()) {
    companion object {
        private fun getSharedPreferencesInstance(): SharedPreferences{
            return appRoot.getSharedPreferences(stringFromRes(R.string.internal_prefs_main), Context.MODE_PRIVATE)
        }
    }

    enum class DaysInAMonthOptions(override val id: String): EnumWithId { _28("28"), _29("29"), _30("30"), _31("31"), Average("average") }
    enum class DaysInAYearOptions(override val id: String): EnumWithId { _365("365"), _366("366"), Average("average") }


    val daysInAMonth = createEnumPref(stringFromRes(R.string.internal_prefs_main_daysInAMonth), DaysInAMonthOptions.values() , DaysInAMonthOptions._30)
    val daysInAYear = createEnumPref(stringFromRes(R.string.internal_prefs_main_daysInAYear), DaysInAYearOptions.values() , DaysInAYearOptions._365)

    val vibrateKeys = createBooleanPref(stringFromRes(R.string.internal_prefs_main_vibrateKeys), false)

    val autoCollapseMillis = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_millis), false)
    val autoCollapseSeconds = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_seconds), false)
    val autoCollapseMinutes = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_minutes), false)
    val autoCollapseHours = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_hours), false)
    val autoCollapseDays = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_days), false)
    val autoCollapseWeeks = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_weeks), false)
    val autoCollapseMonths = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_months), false)
    val autoCollapseYears = createBooleanPref(stringFromRes(R.string.internal_prefs_main_autoCollapse_years), false)


}