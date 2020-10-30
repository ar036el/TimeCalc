package com.arealapps.timecalculator.helpers.listeners_engine

interface LimitedListener {

    val destroyIf: (() -> Boolean)?
    val destroyAfterIf: (() -> Boolean)?

    val destroyAfterTotalCallsOf: Int?
    val destroyAfterCall: Boolean
    fun destroy()

}
object LimitedListenerFactory {
    fun createImpl() = LimitedListenerImpl()
}

class LimitedListenerImpl(
    override var destroyIf: (() -> Boolean)? = null,
    override var destroyAfterIf: (() -> Boolean)? = null,
    override var destroyAfterTotalCallsOf: Int? = null,
    override var destroyAfterCall: Boolean = false
) : LimitedListener {
    override fun destroy() {
        destroyed = true
    }
    var destroyed: Boolean = false
        private set
}