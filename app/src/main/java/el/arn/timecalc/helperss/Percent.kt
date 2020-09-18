package el.arn.timecalc.helperss

fun checkIfPercentIsLegal(percent: Float) {
    if (percent < 0f || percent > 1f) {
        throw InternalError("percent[$percent] cannot exceed the bounds of -1f to 1f")
    }
}

fun percentToValue(percent: Float, minValue: Float, maxValue: Float): Float {
    checkIfPercentIsLegal(percent)
    return (maxValue - minValue) * percent + minValue
}

fun valueToPercent(value: Float, minValue: Float, maxValue: Float): Float {
    val percent = 1 - (maxValue - value) / (maxValue - minValue)
    checkIfPercentIsLegal(percent)
    return if (!percent.isNaN()) percent else 1f
}
