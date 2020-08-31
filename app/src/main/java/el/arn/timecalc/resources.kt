package el.arn.timecalc

import androidx.annotation.DimenRes
import androidx.annotation.StringRes
import el.arn.timecalc.appRoot

fun stringFromRes(@StringRes stringRes: Int): String {
    return appRoot.resources.getString(stringRes)
}

fun dimenFromRes(@DimenRes dimenRes: Int): Float {
    return appRoot.resources.getDimension(dimenRes)
}