package el.arn.timecalc.listeners_engine

object ListenersMgrDispatcher {

    private val subjectsWithListenersManagers = mutableMapOf<HoldsListeners<*>, ListenersManager<*>>()

    fun <T>give(subject : HoldsListeners<T>): ListenersManager<T> { //todo give??
        if (subjectsWithListenersManagers[subject] != null) {
            throw InternalError("subject [$subject] already registered in dispatcher. wtf??")
        }
        val newListenersMgr = ListenersManager<T>()
        subjectsWithListenersManagers[subject] = newListenersMgr
        return newListenersMgr
    }

    fun <T>get(subject : HoldsListeners<T>): ListenersManager<*>? {
        if (subjectsWithListenersManagers[subject] == null) {
            throw InternalError("subject [$subject] is not registered in dispatcher or already got listenersManager")
        }
        val listenersMgr = subjectsWithListenersManagers[subject]
        subjectsWithListenersManagers.remove(listenersMgr)
        return listenersMgr
    }


}