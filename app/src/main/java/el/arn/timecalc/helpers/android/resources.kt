package el.arn.timecalc.helpers.android

import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import el.arn.timecalc.appRoot

fun stringFromRes(@StringRes stringRes: Int): String {
    return appRoot.resources.getString(stringRes)
}

fun dimenFromResAsPx(@DimenRes dimenRes: Int): Float {
    return appRoot.resources.getDimension(dimenRes)
}