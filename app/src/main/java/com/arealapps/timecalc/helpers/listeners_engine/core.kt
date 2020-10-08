package com.arealapps.timecalc.helpers.listeners_engine


class ListenersHandlerEngine<D>(vararg listeners: D) {
    private val list = mutableListOf(*listeners)

    fun add(vararg listeners: D) = list.addAll(listeners)
    fun remove(vararg listeners: D) = list.removeAll(listeners)
    fun clear () = list.clear()
    fun contains(listener: D) = list.contains(listener)

    fun notifyAll(action: (D) -> Unit) {
        val listenersToRemove = mutableSetOf<D>()

        fun remove(listener: LimitedListenerImpl) {
            listenersToRemove.add(listener as D)
            listener.destroy()
        }

        val list = list.toList() //to prevent async related problems
        for (listener in list) {

            if (listener is LimitedListenerImpl) {
                if (listener.destroyIf?.invoke() == true
                    || listener.destroyed) {
                    remove(listener)
                    continue
                }
            }

            if (listener in this.list) { //maybe it was removed during this call
                action.invoke(listener)
            }

            if (listener is LimitedListenerImpl) {
                if (listener.destroyAfterTotalCallsOf != null) {
                    listener.destroyAfterTotalCallsOf = listener.destroyAfterTotalCallsOf?.let { it - 1 }
                }
                if (listener.destroyAfterIf?.invoke() == true
                    || listener.destroyAfterTotalCallsOf ?: 100 <= 0
                    || listener.destroyAfterCall) {
                    remove(listener)
                }
            }

        }

        this.list -= listenersToRemove
    }

}

object DelegationManagerFactory {
    fun <D>listenerManager() = ListenerManager<D>()
    fun <D>listenersManager() = ListenersManager<D>()
}