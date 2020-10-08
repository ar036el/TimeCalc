package com.arealapps.timecalc.organize_later

fun errorIf(errorMessage: String?, predicate: () -> Boolean) {
    if (predicate.invoke()) {
        throw InternalError(errorMessage)
    }
}