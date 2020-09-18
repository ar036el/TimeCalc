package el.arn.timecalc.custom_views

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import android.widget.LinearLayout
import androidx.constraintlayout.widget.ConstraintLayout
import el.arn.timecalc.listeners_engine.HoldsListeners
import el.arn.timecalc.listeners_engine.ListenersManager

class TouchSurfaceConstraintLayout : ConstraintLayout {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    val listenersHolder: HoldsListeners<Listener> get() = listenersMgr!!
    private val listenersMgr: ListenersManager<Listener> = ListenersManager()

    override fun onInterceptTouchEvent(motionEvent: MotionEvent): Boolean {
        listenersMgr.notifyAll { it.onTouchEventIntercepted(motionEvent) }
        return when (motionEvent.actionMasked) {
                    // Always handle the case of the touch gesture being complete.
            MotionEvent.ACTION_CANCEL, MotionEvent.ACTION_UP -> {
                false // Do not intercept touch event, let the child handle it
            }
            MotionEvent.ACTION_MOVE -> {
                false
            }
            else -> {
                // In general, we don't want to intercept touch events. They should be
                // handled by the child view.
                false
            }
        }
    }

    interface Listener {
        fun onTouchEventIntercepted(motionEvent: MotionEvent)
    }
}
