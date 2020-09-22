package el.arn.timecalc

import android.app.Activity
import androidx.annotation.DimenRes


fun pxToSp(px: Float): Float =  px / appRoot.resources.displayMetrics.scaledDensity //todo ofc change it to appRoot
fun pxToDp(px: Float): Float =  px / appRoot.resources.displayMetrics.density //todo ofc change it to appRoot

fun dpToPx(dp: Int): Float =  dp * appRoot.resources.displayMetrics.density //todo ofc change it to appRoot
fun spToPx(sp: Int): Float =  sp * appRoot.resources.displayMetrics.scaledDensity //todo ofc change it to appRoot
