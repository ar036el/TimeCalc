package el.arn.timecalc.helpers.assert

import org.junit.Assert.fail

inline fun <reified E : Throwable>assertThrows(function: () -> Unit) {
    try {
        function.invoke()
        fail("suppose to throw ${E::class.java}")
    } catch (e: Throwable) {
        if (e is E) {
            pass()
        } else {
            fail("exception thrown is not ${E::class.java}")
        }
    }
}