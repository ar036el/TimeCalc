/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalc.organize_later

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import com.arealapps.timecalc.helpers.native_.PxPoint

val View.locationInWindow: PxPoint
    get() {
        val loc = IntArray(2)
        this.getLocationInWindow(loc)
        return PxPoint(
            loc[0].toFloat(),
            loc[1].toFloat()
        )
    }

val View.activity: Activity?
    get() {
        var context: Context = context
        while (context is ContextWrapper) {
            if (context is Activity) {
                return context
            }
            context = context.baseContext
        }
        return null
    }