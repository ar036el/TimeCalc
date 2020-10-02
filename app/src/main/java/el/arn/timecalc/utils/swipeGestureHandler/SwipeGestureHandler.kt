package el.arn.timecalc.utils.swipeGestureHandler

import android.app.Activity
import android.graphics.Rect
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.dynamicanimation.animation.DynamicAnimation
import androidx.dynamicanimation.animation.FlingAnimation
import el.arn.timecalc.helpers.android.doWhenDynamicVariablesAreReady
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.helpers.native_.percentToValue
import el.arn.timecalc.helpers.native_.valueToPercent
import el.arn.timecalc.helpers.listeners_engine.HoldsListeners
import el.arn.timecalc.helpers.listeners_engine.ListenersManager

interface SwipeGestureHandler : HoldsListeners<SwipeGestureHandler.Listener> {

    val touchSubject: View
    val xBound: Bound
    val yBound: Bound
    var additionalGestureListenerForTouchSubject: GestureDetector.SimpleOnGestureListener?
    var isEnabled: Boolean

    val minXPos: Float
    val maxXPos: Float
    val minYPos: Float
    val maxYPos: Float

    val currentPoint: PxPoint

    val currentXPercent: Float
    val currentYPercent: Float

    var isFlingMomentumEnabled: Boolean



    //Percent is determined by the relative location of the current point to the current bound. bound of Static gives only 1f or percent error. Free is relative to the touchSurface bounds, and range is relative to.. range
    fun toXPercent(pointXPositionInPx: Float): Float //todo better naming? //todo maybe return the percent relative to touch surface?
    fun toYPercent(pointYPositionInPx: Float): Float
    fun isWithinBounds(pos: PxPoint): Boolean
    fun updatePoint(newPointX: Float, newPointY: Float)
    fun updatePointFromPercent(XPercent: Float?, YPercent: Float?, )


    //fun destroy(): Boolean //todo not in the current moment

    enum class SwipeState {
        Static, Scrolling, Flinging
    }


    sealed class Bound {
        data class Range(val startInPx: Float, val endInPx: Float) : Bound()
        object FreeWithinTouchSurfaceBounds : Bound()
        object Static : Bound()
    }

    interface Listener {

        fun hasInitialized(subject: SwipeGestureHandler) {}
        fun pointHasChanged(subject: SwipeGestureHandler, lastPoint: PxPoint, newPoint: PxPoint)
        fun swipeStateHasChanged(subject: SwipeGestureHandler, state: SwipeState, lastState: SwipeState)
    }
}

// * Class A does something using [activity].
/**
 * MUST BE INSTANTIATED AFTER ACTIVITY WAS DRAWN
 * @param touchSurface is the total space area the gesture can be inputted. It can never move and it's best for it to be at the background for it to not interrupt other touch listeners.
 * @param touchSubject is the view that when will be touched, will trigger the gesture. it can be moved freely.
 */
class SwipeGestureHandlerImpl(
    private val activity: Activity,
    private val touchSurface: TouchSurfaceLayout,
    override val touchSubject: View,
    override val xBound: SwipeGestureHandler.Bound,
    override val yBound: SwipeGestureHandler.Bound,
    private val startingPoint: PxPoint,
    override var isFlingMomentumEnabled: Boolean,
    flingFriction: Float,
    additionalGestureListenerForTouchSubject: GestureDetector.SimpleOnGestureListener? = null,
    private val listenersMgr: ListenersManager<SwipeGestureHandler.Listener> = ListenersManager()
): SwipeGestureHandler, HoldsListeners<SwipeGestureHandler.Listener> by listenersMgr {

    override var currentPoint = startingPoint
    override var isEnabled: Boolean = true
        set(value) {
            if (!value) { cancelFlingAnimationsIfRunning() }
            field = value
        }
    override val minXPos: Float by lazy {
        checkIfTouchSurfaceHasInitialized()
        return@lazy when (xBound) {
            is SwipeGestureHandler.Bound.Range -> xBound.startInPx
            SwipeGestureHandler.Bound.FreeWithinTouchSurfaceBounds -> touchSurface.x
            SwipeGestureHandler.Bound.Static -> startingPoint.x
        }
    }
    override val maxXPos: Float by lazy {
        checkIfTouchSurfaceHasInitialized()
        return@lazy when (xBound) {
            is SwipeGestureHandler.Bound.Range -> xBound.endInPx
            SwipeGestureHandler.Bound.FreeWithinTouchSurfaceBounds -> touchSurface.width.toFloat()
            SwipeGestureHandler.Bound.Static -> startingPoint.x
        }
    }
    override val minYPos: Float by lazy {
        checkIfTouchSurfaceHasInitialized()
        return@lazy when (yBound) {
            is SwipeGestureHandler.Bound.Range -> yBound.startInPx
            SwipeGestureHandler.Bound.FreeWithinTouchSurfaceBounds -> touchSurface.y
            SwipeGestureHandler.Bound.Static -> startingPoint.y
        }
    }
    override val maxYPos: Float by lazy {
        checkIfTouchSurfaceHasInitialized()
        return@lazy when (yBound) {
            is SwipeGestureHandler.Bound.Range -> yBound.endInPx
            SwipeGestureHandler.Bound.FreeWithinTouchSurfaceBounds -> touchSurface.height.toFloat()
            SwipeGestureHandler.Bound.Static -> startingPoint.y
        }
    }

    override var additionalGestureListenerForTouchSubject: GestureDetector.SimpleOnGestureListener? = null
        set(value) {
            additionalGestureDetector =  value?.let { GestureDetector(activity, value) }
            field = value
        }

    override val currentXPercent: Float get() = toXPercent(currentPoint.x)
    override val currentYPercent: Float get() = toYPercent(currentPoint.y)

    private var lastPoint = startingPoint
    private val pointView: View = View(activity) //todo mimic??
    private var onTouchSubjectPress = false
    private var currentSwipeState = SwipeGestureHandler.SwipeState.Static

    private val flingAnimationX: FlingAnimation by lazy(LazyThreadSafetyMode.NONE) {
        FlingAnimation(pointView, DynamicAnimation.X).setFriction(flingFriction)
            .setMinValue(minXPos)
            .setMaxValue(maxXPos)
            .addUpdateListener { _, _, _ ->
                updatePointsAndNotifyChangesIfAny(pointView.x, pointView.y)
                //todo right now it's being notified twice, for x change and y change. for optimization, make it notify only when both flingAnimations are called
            }
            .addEndListener { _, _, _, _ ->
                if (!flingAnimationX.isRunning && !flingAnimationY.isRunning) {
                    changeSwipeStateAndNotifyChangeIfAny(SwipeGestureHandler.SwipeState.Static)
                }
            }
    }

    private val flingAnimationY: FlingAnimation by lazy(LazyThreadSafetyMode.NONE) {
        FlingAnimation(pointView, DynamicAnimation.Y).setFriction(flingFriction)
            .setMinValue(minYPos)
            .setMaxValue(maxYPos)
            .addUpdateListener { _, _, _ ->
                updatePointsAndNotifyChangesIfAny(pointView.x, pointView.y)
            }
            .addEndListener { _, _, _, _ ->
                if (!flingAnimationX.isRunning && !flingAnimationY.isRunning) {
                    changeSwipeStateAndNotifyChangeIfAny(SwipeGestureHandler.SwipeState.Static)
                }
            }
    }

    private val gestureListener = object : GestureDetector.SimpleOnGestureListener() {

        override fun onDown(e: MotionEvent?): Boolean { //todo necessary??
            return true
        }

        override fun onScroll(
            e1: MotionEvent,
            e2: MotionEvent,
            distanceX: Float,
            distanceY: Float
        ): Boolean {
            cancelFlingAnimationsIfRunning()

            println("distanceX $distanceX distance $distanceY") //todo remove
            advancePointView(distanceX, distanceY)
            normalizePointViewPositionIfOutOfBounds()

            updatePointsAndNotifyChangesIfAny(pointView.x, pointView.y)
            changeSwipeStateAndNotifyChangeIfAny(SwipeGestureHandler.SwipeState.Scrolling)
            return true
        }

        override fun onFling(
            e1: MotionEvent,
            e2: MotionEvent,
            velocityX: Float,
            velocityY: Float
        ): Boolean {
            normalizePointViewPositionIfOutOfBounds()

            if (isFlingMomentumEnabled) {

                if (xBound !is SwipeGestureHandler.Bound.Static) {
                    flingAnimationX.setStartVelocity(velocityX)
                    flingAnimationX.start()
                }
                if (yBound !is SwipeGestureHandler.Bound.Static) {
                    flingAnimationY.setStartVelocity(velocityY)
                    flingAnimationY.start()
                }

                changeSwipeStateAndNotifyChangeIfAny(SwipeGestureHandler.SwipeState.Flinging)
            } else {
                changeSwipeStateAndNotifyChangeIfAny(SwipeGestureHandler.SwipeState.Static)
            }
            return true
        }

        private fun advancePointView(distanceX: Float, distanceY: Float) {
            if (xBound !is SwipeGestureHandler.Bound.Static) { //todo/לעבור על כל האיקס וואי לראות שאין כפילויות צמוד
                pointView.x -= distanceX
            }
            if (yBound !is SwipeGestureHandler.Bound.Static) {
                pointView.y -= distanceY
            }
        }
    }

    private val gestureDetector = GestureDetector(activity, gestureListener)
    private var additionalGestureDetector: GestureDetector? = null

    private fun onTouch(motionEvent: MotionEvent): Boolean {
        if(!isEnabled) {
            return false
        }
        val rect = Rect()
        touchSubject.getGlobalVisibleRect(rect)
        if (rect.contains(motionEvent.rawX.toInt(), motionEvent.rawY.toInt())) {
            onTouchSubjectPress = true
        }
        if (onTouchSubjectPress) {
            doOnTouch(motionEvent)
        }
        return onTouchSubjectPress
    }

    private val touchSurfaceOnTouchListener = object : View.OnTouchListener {
        override fun onTouch(v: View?, motionEvent: MotionEvent): Boolean {
            return onTouch(motionEvent)
        }
    }

    private fun doOnTouch(motionEvent: MotionEvent) {
        gestureDetector.onTouchEvent(motionEvent)
        additionalGestureDetector?.onTouchEvent(motionEvent)
        if (motionEvent.action == MotionEvent.ACTION_UP) {
            onTouchSubjectPress = false
            if (!flingAnimationX.isRunning && !flingAnimationY.isRunning) {
                changeSwipeStateAndNotifyChangeIfAny(SwipeGestureHandler.SwipeState.Static)
            }
        }
    }


    override fun toXPercent(pointXPositionInPx: Float): Float {
        return valueToPercent(pointXPositionInPx, minXPos, maxXPos)
    }
    override fun toYPercent(pointYPositionInPx: Float): Float {
        return valueToPercent(pointYPositionInPx, minYPos, maxYPos)
    }



    override fun isWithinBounds(pos: PxPoint): Boolean {
        return isOnXBound(pos.x) && isOnYBound(pos.y)
    }

    override fun updatePoint(newPointX: Float, newPointY: Float) {
        cancelFlingAnimationsIfRunning()
        if (newPointX < minXPos || newPointX > maxXPos || newPointY < minYPos || newPointY > maxYPos) {
            throw InternalError("point is not within bounds or called before all components were initialized")
        }
        pointView.x = newPointX
        pointView.y = newPointY
        updatePointsAndNotifyChangesIfAny(newPointX, newPointY)
    }

    override fun updatePointFromPercent(XPercent: Float?, YPercent: Float?) {
        val newX = XPercent?.let { percentToValue(it, minXPos, maxXPos) } ?: currentPoint.x
        val newY = YPercent?.let { percentToValue(it, minYPos, maxYPos) } ?: currentPoint.y
        updatePoint(newX, newY)
    }


    private fun changeSwipeStateAndNotifyChangeIfAny(newSwipeState: SwipeGestureHandler.SwipeState) {
        val lastSwipeState = currentSwipeState
        currentSwipeState = newSwipeState
        if (currentSwipeState != lastSwipeState) {
            listenersMgr.notifyAll { it.swipeStateHasChanged(this, currentSwipeState, lastSwipeState) }
        }
    }

    private fun isOnXBound(xPosInPx: Float): Boolean {
        return when (xBound) {
            is SwipeGestureHandler.Bound.Range -> (xPosInPx >= xBound.startInPx && xPosInPx <= xBound.endInPx)
            SwipeGestureHandler.Bound.FreeWithinTouchSurfaceBounds -> {
                if (touchSurface.width == 0 || touchSurface.height == 0) {
                    throw InternalError("touch surface not yet initialized")
                } else {
                    (xPosInPx >= touchSurface.x && xPosInPx <= touchSurface.width)
                }
            }
            SwipeGestureHandler.Bound.Static -> xPosInPx == currentPoint.x
        }
    }

    private fun isOnYBound(yPosInPx: Float): Boolean {
        return when (yBound) {
            is SwipeGestureHandler.Bound.Range -> (yPosInPx >= yBound.startInPx && yPosInPx <= yBound.endInPx)
            SwipeGestureHandler.Bound.FreeWithinTouchSurfaceBounds -> {
                if (touchSurface.width == 0 || touchSurface.height == 0) {
                    throw InternalError("touch surface not yet initialized")
                } else {
                    (yPosInPx >= touchSurface.y && yPosInPx <= touchSurface.height)
                }
            }
            SwipeGestureHandler.Bound.Static -> yPosInPx == currentPoint.y
        }
    }

    private fun checkIfPointStartingLocationIsWithinBoundsAsync() {
        val wasNormalized = normalizePointViewPositionIfOutOfBounds()
        if (wasNormalized) { //todo problem now not initialized
            throw InternalError("startingPoint($startingPoint) is not in bounds(x$xBound y$yBound)")
        }
    }

    private fun checkIfBoundsAreValid(xBound: SwipeGestureHandler.Bound, yBound: SwipeGestureHandler.Bound) {
        if (xBound is SwipeGestureHandler.Bound.Range && xBound.startInPx >= xBound.endInPx) {
            throw InternalError("xBound($xBound) is not a legal BetweenPoints range($xBound)")
        }
        if (yBound is SwipeGestureHandler.Bound.Range && yBound.startInPx >= yBound.endInPx) {
            throw InternalError("yBound($yBound) is not a legal BetweenPoints range ($yBound)")
        }
    }

    private fun checkIfTouchSurfaceHasInitialized() {
        if (touchSurface.width == 0 || touchSurface.height == 0) {
            throw InternalError("touch surface not yet initialized")
        }
    }

    private fun updatePointsAndNotifyChangesIfAny(newPointX: Float, newPointY: Float) {
        lastPoint = currentPoint
        currentPoint = PxPoint(newPointX, newPointY)
        if (lastPoint.x != currentPoint.x || lastPoint.y != currentPoint.y) {
            notifyPointHasChanged()
        }
    }

    private fun notifyPointHasChanged() {
        listenersMgr.notifyAll { it.pointHasChanged(this@SwipeGestureHandlerImpl, lastPoint , currentPoint) }
    }

    private fun cancelFlingAnimationsIfRunning() {
        flingAnimationX.cancel()
        flingAnimationY.cancel()
    }

    private fun normalizePointViewPositionIfOutOfBounds(): Boolean {
        var wasNormalized = false
        if (pointView.x < minXPos) {
            pointView.x = minXPos
            wasNormalized = true
        } else if (pointView.x > maxXPos) {
            pointView.x = maxXPos
            wasNormalized = true
        }
        if (pointView.y < minYPos) {
            pointView.y = minYPos
            wasNormalized = true
        } else if (pointView.y > maxYPos) {
            pointView.y = maxYPos
            wasNormalized = true
        }
        return wasNormalized
    }


    init {
        checkIfBoundsAreValid(xBound, yBound)

        activity.addContentView(pointView, ViewGroup.LayoutParams(1, 1))

        //pointMimic.setBackgroundResource(R.color.colorPrimary)
        pointView.x = startingPoint.x
        pointView.y = startingPoint.y

        this.additionalGestureListenerForTouchSubject = additionalGestureListenerForTouchSubject

        touchSurface.setOnTouchListener(touchSurfaceOnTouchListener)

        touchSurface.listenersHolder.addListener(object: TouchSurfaceLayout.Listener {
            override fun onTouchEventIntercepted(motionEvent: MotionEvent) {
                onTouch(motionEvent)
            }
        })

        touchSurface.doWhenDynamicVariablesAreReady {
            checkIfPointStartingLocationIsWithinBoundsAsync()
            listenersMgr.notifyAll { it.hasInitialized(this) }
        }
    }

}