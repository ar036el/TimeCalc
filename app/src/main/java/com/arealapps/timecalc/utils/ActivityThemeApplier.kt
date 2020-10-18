package com.arealapps.timecalc.utils

import android.app.Activity
import com.arealapps.timecalc.R
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.preferences_managers.CalculatorPreferencesManager

interface ActivityThemeApplier {
    fun applyTheme(activity: Activity)
}

class ActivityThemeApplierImpl(
    prefsManager: CalculatorPreferencesManager
) : ActivityThemeApplier {

    override fun applyTheme(activity: Activity) {
        val theme = when (rootUtils.calculatorPreferencesManager.calculatorTheme.value) {
            0 -> R.style.Theme0_Default
            1 -> R.style.Theme1
            2 -> R.style.Theme2
            else -> throw NotImplementedError()
        }
        activity.setTheme(theme)
    }
}