
package com.arealapps.timecalculator.helpers.android

import android.app.Activity
import android.view.View

val Activity?.isAlive: Boolean
    get() = (this?.isDestroyed == false)



val Activity.isDirectionRTL
    get() =  resources.configuration.layoutDirection == View.LAYOUT_DIRECTION_RTL

