package el.arn.timecalc

class LimitedAccessFunction(private val function: (params: Array<out Any?>) -> Unit, var accesses: Int = 0) {
    fun invokeIfHasAccess(vararg params: Any?) {
        if (accesses > 0) {
            function.invoke(params)
            accesses--
        }
    }

    fun grantOneAccess() { accesses = 1 }
}