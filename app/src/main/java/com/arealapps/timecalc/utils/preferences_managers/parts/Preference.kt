package com.arealapps.timecalc.utils.preferences_managers.parts

import android.content.SharedPreferences
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager


interface Preference<V> : HoldsListeners<Preference.Listener<V>> {
    val key: String
    val possibleValues: Iterable<V>?
    val defaultValue: V
    var value: V
    fun restoreToDefault()
    //todo refresh value and then add this to settingsPrefsMgrBridge

    interface Listener<V> {
        /**called only when the pref's value is actually changed. assigning the pref with the same value invokes nothing*/
        fun prefHasChanged(preference: Preference<V>, value: V)
    }
}

abstract class PreferenceImpl<V> (
    override val key: String,
    override val possibleValues: Iterable<V>?,
    final override val defaultValue: V,
    protected val preflistenersMgr: ListenersManager<Preference.Listener<V>> = ListenersManager()
): Preference<V>, HoldsListeners<Preference.Listener<V>> by preflistenersMgr {

    override fun restoreToDefault() {
        value = defaultValue
    }
    fun notifyListenersPrefHasChanged() {
        preflistenersMgr.notifyAll { it.prefHasChanged(this, value) }
    }

    override var value: V
        get() =
            getValueFromSharedPreferences()
        set(v) {
            assertValueIsInRange(v)
            if (this.value != v) {
                writeValueToSharedPreferences(v)
            }
        }
    private fun assertValueIsInRange(value: V) {
        if (possibleValues != null && value !in possibleValues ?: emptyList()) {
            throw InternalError(value.toString() + " is not a possible value for pref " + key)
        }
    }
    protected fun initPreferenceValueIfKeyDoesNotExistInSharedPreferences(sharedPreferences: SharedPreferences) {
        if (!sharedPreferences.contains(key)) {
            writeValueToSharedPreferences(defaultValue)
        }
    }
    init {
        assertValueIsInRange(defaultValue)
    }
    protected abstract fun getValueFromSharedPreferences(): V
    protected abstract fun writeValueToSharedPreferences(value: V)

}
