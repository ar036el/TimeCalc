package el.arn.timecalc.helpers.native_

fun random(min: Float, max: Float): Float = (min + Math.random() * (max - min)).toFloat()
fun random(min: Int, max: Int): Int = (min + Math.random() * (max - min)).toInt()