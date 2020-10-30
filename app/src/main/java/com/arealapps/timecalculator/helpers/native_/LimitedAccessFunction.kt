package com.arealapps.timecalculator.helpers.native_

class LimitedAccessFunction(private val function: (params: Array<out Any?>) -> Unit, var accesses: Int = 0) {
    fun invokeIfHasAccess(vararg params: Any?) {
        if (accesses > 0) {
            function.invoke(params)
            accesses--
        }
    }

    fun grantOneAccess() { accesses = 1 }
    fun removeAccesses() { accesses = 0 }

}