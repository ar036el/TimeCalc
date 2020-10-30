package com.arealapps.timecalculator.utils

import android.view.HapticFeedbackConstants
import android.view.View


interface VibrationManager {
    fun vibrateAsSimpleClick(subject: View)
}

class VibrationManagerImpl : VibrationManager {

    override fun vibrateAsSimpleClick(subject: View) {
        subject.performHapticFeedback(HapticFeedbackConstants.CLOCK_TICK)
    }

}