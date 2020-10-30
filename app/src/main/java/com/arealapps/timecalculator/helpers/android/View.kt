package com.arealapps.timecalculator.helpers.android

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.view.View
import android.view.ViewTreeObserver
import com.arealapps.timecalculator.helpers.native_.PxPoint

var View.heightByLayoutParams: Int
    get() = this.height
    set(value) {
        val layoutParams = this.layoutParams
        layoutParams.height = value
        this.layoutParams = layoutParams
    }
var View.widthByLayoutParams: Int
    get() = this.width
    set(value) {
        val layoutParams = this.layoutParams
        layoutParams.width = value
        this.layoutParams = layoutParams
    }

val View.paddingX: Int get() = paddingStart + paddingEnd
val View.paddingY: Int get() = paddingTop + paddingBottom

fun <T : View>T.doWhenDynamicVariablesAreReady(function: (it: T) -> Unit) { //todo does it work multiple times on same view?
    this.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
        override fun onGlobalLayout() {
            this@doWhenDynamicVariablesAreReady.viewTreeObserver.removeOnGlobalLayoutListener(this)
            function.invoke(this@doWhenDynamicVariablesAreReady)
        }
    })
    requestLayout()
}

fun View.observeWhenDynamicVariablesAreReady() {
    whenDynamicVariablesAreReadyMap[this] = false
    this.doWhenDynamicVariablesAreReady {
        whenDynamicVariablesAreReadyMap[this] = true
    }
}

val View.areDynamicVariablesReady: Boolean get() {
    return whenDynamicVariablesAreReadyMap[this] ?: throw InternalError("'View.observeWhenDynamicVariablesAreReady' must be called on view init for this property to work")
}

private val whenDynamicVariablesAreReadyMap = mutableMapOf<View, Boolean>()

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