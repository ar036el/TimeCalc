package com.arealapps.timecalculator.helpers.native_

fun errorIf(errorMessage: String? = null, predicate: () -> Boolean) {
    if (predicate.invoke()) {
        throw InternalError(errorMessage)
    }
}