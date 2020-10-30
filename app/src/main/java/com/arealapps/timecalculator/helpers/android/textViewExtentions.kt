package com.arealapps.timecalculator.helpers.android

import android.widget.TextView

fun TextView.measureTextWidth() = paint.measureText(text, 0, text.length)
fun TextView.measureTheoreticalTextWidth(text: String) = paint.measureText(text, 0, text.length)