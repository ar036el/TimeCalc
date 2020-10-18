package com.arealapps.timecalc.organize_later

fun errorIf(errorMessage: String? = null, predicate: () -> Boolean) {
    if (predicate.invoke()) {
        throw InternalError(errorMessage)
    }
}