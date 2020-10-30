package com.arealapps.timecalculator.helpers.android

import android.app.Activity
import java.util.*

fun setIntervalUiCompat(activity: Activity, initialDelayInMillis: Long, everyXMillis: Long, func: () -> Unit) {
    Timer().scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            activity.runOnUiThread(func)
        }
    }, initialDelayInMillis, everyXMillis)
}

fun setTimeoutUiCompat(activity: Activity, delayInMillis: Long, func: () -> Unit) {
    android.os.Handler().postDelayed ({
        activity.runOnUiThread(func)
    }, delayInMillis)
}