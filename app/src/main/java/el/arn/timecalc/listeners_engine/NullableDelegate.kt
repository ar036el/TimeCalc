package el.arn.timecalc.listeners_engine

interface HoldsListener<D> {
    fun setListener(listener: D?)
    fun removeListener()
}

class ListenerManager<D>(listener: D? = null) : HoldsListener<D> {
    private val handler: ListenersHandlerEngine<D> = if (listener != null) ListenersHandlerEngine(listener) else ListenersHandlerEngine()

    override fun setListener(listener: D?) {
        handler.clear()
        listener?.let { handler.add(it) }
    }
    override fun removeListener() {
        handler.clear()
    }
    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }

}