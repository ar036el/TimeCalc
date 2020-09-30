package el.arn.timecalc.helpers.native_

import kotlin.math.*

fun random(min: Float, max: Float): Float = (min + Math.random() * (max - min)).toFloat()
fun random(min: Int, max: Int): Int = (min + Math.random() * (max - min)).toInt()

fun boundByMinAndMax(number: Int, min: Int, max: Int): Int {
    if (min > max) { throw InternalError("min[$min] > max[$max]") }
    var bounded = max(number, min)
    bounded = min(bounded, max)
    return bounded
}
fun boundByMinAndMax(number: Float, min: Float, max: Float): Float {
    if (min > max) { throw InternalError("min[$min] > max[$max]") }
    var bounded = max(number, min)
    bounded = min(bounded, max)
    return bounded
}


fun getDistance(from: Point<Float>, to: Point<Float>): Float {
    return sqrt((to.y - from.y) * (to.y - from.y) + (to.x - from.x) * (to.x - from.x))
}

//todo get point to here?


