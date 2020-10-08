package com.arealapps.timecalc.helpers.listeners_engine

interface NonNullListenerHolder<D> {
    fun setListener(listener: D)
}

class NonNullListenerManager<D>(listener: D) : NonNullListenerHolder<D> {
    private val handler = ListenersHandlerEngine(listener)

    override fun setListener(listener: D) {
        handler.clear()
        handler.add(listener)
    }

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}