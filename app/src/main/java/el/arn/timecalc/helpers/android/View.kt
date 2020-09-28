package el.arn.timecalc.helpers.android

import android.view.View
import android.view.ViewTreeObserver

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