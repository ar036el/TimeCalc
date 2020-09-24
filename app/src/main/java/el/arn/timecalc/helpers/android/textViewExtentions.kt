package el.arn.timecalc.helpers.android

import android.widget.TextView

fun TextView.measureTextWidth() = paint.measureText(text, 0, text.length)