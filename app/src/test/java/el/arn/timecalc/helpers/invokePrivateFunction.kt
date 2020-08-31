package el.arn.timecalc.helpers

import java.lang.reflect.InvocationTargetException
import kotlin.reflect.full.declaredMemberFunctions
import kotlin.reflect.jvm.isAccessible

inline fun <reified Class,ReturnType>Class.invokePrivateFunction(functionName: String, vararg params: Any?): ReturnType {
    try {
        val privateFunc = Class::class.declaredMemberFunctions.find { it.name == functionName }
            ?: throw NoSuchMethodError()
        privateFunc?.isAccessible = true
        return privateFunc?.call(this, *params) as ReturnType
    } catch (e: ReflectiveOperationException) {
        throw e.cause ?: e
    }
}