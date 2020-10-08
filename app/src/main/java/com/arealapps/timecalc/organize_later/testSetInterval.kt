package com.arealapps.timecalc.organize_later

import android.app.Activity
import java.util.*

fun testSetInterval(activity: Activity, everyXMillis: Long, func: () -> Unit) {
    Timer().scheduleAtFixedRate(object : TimerTask() {
        override fun run() {
            activity.runOnUiThread(func)
        }
    }, 0, everyXMillis)
}