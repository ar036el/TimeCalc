package el.arn.timecalc.helpers.android

import android.content.res.Resources
import androidx.annotation.*
import androidx.core.content.ContextCompat
import el.arn.timecalc.appRoot

fun stringFromRes(@StringRes stringRes: Int): String {
    return appRoot.resources.getString(stringRes)
}

fun dimenFromResAsPx(@DimenRes dimenRes: Int): Float {
    return appRoot.resources.getDimension(dimenRes)
}

@ColorInt fun colorFromRes(@ColorRes colorRes: Int): Int {
    return ContextCompat.getColor(appRoot.applicationContext, colorRes)
}

fun floatFromRes(@DimenRes floatRes: Int): Float {
    val outValue = android.util.TypedValue()
    appRoot.resources.getValue(floatRes, outValue, true)
    return outValue.float
}