package el.arn.timecalc.helpers.listeners_engine

interface ImmutableListenerHolder<D>

class ImmutableListenerManager<D>(listener: D) : ImmutableListenerHolder<D> {
    private val handler = ListenersHandlerEngine(listener)

    fun notify(action: (D) -> Unit) {
        handler.notifyAll(action)
    }
}