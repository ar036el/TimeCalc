package com.arealapps.timecalculator.utils.swipeGestureHandler

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.constraintlayout.widget.ConstraintLayout
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager

class TouchSurfaceLayout : ConstraintLayout {
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
