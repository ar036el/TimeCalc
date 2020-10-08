package com.arealapps.timecalc.helpers.native_

class DynamicFieldsDispatcher<T>(val obj: T) {

    val map = mutableMapOf<String, Any?>()

    operator fun <V>get(key: String): V = map[key] as V
    operator fun <V>set(key: String, value: V) { map[key] = value }
    fun remove(key: String) = map.remove(key)
    val fields  get() = map.keys.toSet()
}