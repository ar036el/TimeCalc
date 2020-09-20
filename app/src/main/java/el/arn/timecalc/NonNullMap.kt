package el.arn.timecalc

class NonNullMap<K, V>(private val map: Map<K, V>) : Map<K, V> by map {
    override operator fun get(key: K): V {
        return map[key] ?: error("trying to access non existing key '$key'")
    }
}