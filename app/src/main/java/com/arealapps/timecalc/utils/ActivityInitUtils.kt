package com.arealapps.timecalc.utils

import android.app.Activity
import android.widget.Toolbar
import com.arealapps.timecalc.R
import com.arealapps.timecalc.rootUtils
import com.arealapps.timecalc.utils.preferences_managers.CalculatorPreferencesManager

interface ActivityInitUtils {
    fun initTheme(activity: Activity)
}

class ActivityInitUtilsImpl(
    private val prefsManager: CalculatorPreferencesManager
) : ActivityInitUtils {

    override fun initTheme(activity: Activity) {
        val theme = when (prefsManager.calculatorTheme.value) {
            0 -> R.style.Theme0_Default
            1 -> R.style.Theme1
            2 -> R.style.Theme2
            else -> throw NotImplementedError()
        }
        activity.setTheme(theme)
    }
}