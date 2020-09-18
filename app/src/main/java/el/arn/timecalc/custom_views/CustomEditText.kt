package el.arn.timecalc.custom_views

import android.content.Context
import android.util.AttributeSet
import el.arn.timecalc.listeners_engine.HoldsListeners
import el.arn.timecalc.listeners_engine.ListenersManager


class CustomEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)


    val listenersHolder: HoldsListeners<Listener> get() = listenersMgr!!
    private val listenersMgr: ListenersManager<Listener>? = ListenersManager() //this is nullable because crappy parent ctor calls to onSelectionChanged before its being initialized -.-

    override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
        super.onSelectionChanged(selectionStart, selectionEnd)
        listenersMgr?.notifyAll { it.onSelectionChanged(this, selectionStart, selectionEnd) }
    }

    interface Listener {
        fun onSelectionChanged(subject: CustomEditText, selectionStart: Int, selectionEnd: Int)
    }
}
