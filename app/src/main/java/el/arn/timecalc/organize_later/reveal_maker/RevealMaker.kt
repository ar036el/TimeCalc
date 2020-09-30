package el.arn.timecalc.organize_later.reveal_maker

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import el.arn.timecalc.R
import el.arn.timecalc.helpers.android.AnimatorListener
import el.arn.timecalc.helpers.listeners_engine.HoldsListeners
import el.arn.timecalc.helpers.listeners_engine.ListenersManager
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.helpers.native_.getDistance
import el.arn.timecalc.organize_later.reveal_maker.RevealMaker.States.*
import java.lang.IllegalStateException
import kotlin.math.abs

interface RevealMaker : HoldsListeners<RevealMaker.Listener>{
    val drawingSurface: FrameLayout

    /** PxPoints have to be relative to drawing surface.
     * @throws [IllegalStateException] if [currentState] is not [States.Inactive]*/
    fun startBubbleReveal(from: PxPoint, to: PxPoint)
    /** px coords have to be relative to drawing surface.
     * @throws [IllegalStateException] if [currentState] is not [States.Inactive]*/
    fun startVerticalRectReveal(startX: Float, endX: Float, fromY: Float, toY: Float)

    val currentState: States
    enum class States { Inactive, IsExpanding, IsFading }

    interface Listener {
        fun stateHasChanged(subject: RevealMaker, newState: States)
    }
}


class RevealMakerImpl(
    override val drawingSurface: FrameLayout,
    private val expandDuration: Long,
    private val fadeDuration: Long,
    private val listenersMgr: ListenersManager<RevealMaker.Listener> = ListenersManager()
): RevealMaker, HoldsListeners<RevealMaker.Listener> by listenersMgr {

    override var currentState = Inactive
        private set(newState) {
            if (field == newState) { return }
            field = newState
            listenersMgr.notifyAll { it.stateHasChanged(this, newState) }
        }

    private var currentReveal: Reveal? = null

    override fun startBubbleReveal(fromRelative: PxPoint, toRelative: PxPoint) {
        val maxRadius = getDistance(fromRelative, toRelative)
        val reveal = Bubble(fromRelative)
        startReveal(reveal, maxRadius)
    }

    override fun startVerticalRectReveal(startX: Float, endX: Float, fromY: Float, toY: Float) {
        if (startX > endX) { throw InternalError("startX[$startX] > endX[$endX]") }
        val reveal = VerticalRect(startX, endX, fromY, (toY > fromY))
        startReveal(reveal, abs(fromY - toY))
    }

    private fun startReveal(reveal: Reveal, maxLength: Float) {
        if (currentState != Inactive) { throw IllegalStateException() }
        if (currentReveal != null) { throw InternalError() }

        currentReveal = reveal
        currentState = IsExpanding
        startPercentValueAnimation(expandDuration, {
            setRevealLength(maxLength, it)
        }) {
            currentState = IsFading
            startPercentValueAnimation(fadeDuration, {
                setRevealFadePercentage(it)
            }) {
                removeReveal()
                currentState = Inactive
            }
        }
    }

    private fun setRevealLength(length: Float, expansionPercent: Float) {
        currentReveal!!.lengthInPx = length * expansionPercent
    }

    private fun setRevealFadePercentage(fadePercent: Float) {
        currentReveal!!.alpha = 1f - fadePercent
    }

    private fun removeReveal() {
        currentReveal!!.delete()
        currentReveal = null
    }

    //TODO can be made to class no? just add interpolator param
    private fun startPercentValueAnimation(
        duration: Long,
        doOnUpdate: (percent: Float) -> Unit,
        doOnFinish: (() -> Unit)? = null
    ) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.apply {
            addUpdateListener {animation ->
                doOnUpdate(animation.animatedValue as Float)
            }
            addListener(object: AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    doOnFinish?.invoke()
                }
            })
            this.duration = duration
            interpolator = null
            start()
        }
    }

    private interface Reveal {
        var lengthInPx: Float
        var alpha: Float
        fun delete()
    }

    private inner class Bubble(
        relativeStartingLocation: PxPoint
    ) : Reveal {
        private val originalLengthInPx = 1
        private val imageView: ImageView = ImageView(drawingSurface.context)

        override var lengthInPx: Float = 0f //lateinit
            set(newLength) {
                field = newLength
                imageView.scaleX = (newLength*2+1) / originalLengthInPx
                imageView.scaleY = (newLength*2+1) / originalLengthInPx
            }

        override var alpha
            get() = imageView.alpha
            set(value) { imageView.alpha = value}

        override fun delete() {
            drawingSurface.removeView(imageView)
        }

        init{
            imageView.layoutParams = ViewGroup.LayoutParams(originalLengthInPx,originalLengthInPx)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.setImageResource(R.drawable.reveal_circle)
            drawingSurface.addView(imageView)
            imageView.x = relativeStartingLocation.x
            imageView.y = relativeStartingLocation.y
            imageView.pivotX = originalLengthInPx / 2f
            imageView.pivotY = originalLengthInPx / 2f
            lengthInPx = 0f
        }
    }


    private inner class VerticalRect(
        startX: Float,
        endX: Float,
        fromY: Float,
        revealFromTopToBottom: Boolean
    ) : Reveal {
        private val originalHeightInPx = 1
        private val imageView: ImageView = ImageView(drawingSurface.context)

        override var lengthInPx = 0f //lateinit
            set(newLength) {
                field = newLength
                imageView.scaleY = (newLength+1) / originalHeightInPx
            }

        override var alpha
            get() = imageView.alpha
            set(value) { imageView.alpha = value}

        override fun delete() {
            drawingSurface.removeView(imageView)
        }

        init{
            imageView.layoutParams = ViewGroup.LayoutParams((endX - startX).toInt(),originalHeightInPx)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.setImageResource(R.drawable.reveal_square)
            drawingSurface.addView(imageView)
            imageView.x = startX
            imageView.y = fromY

            imageView.pivotX = 0f
            imageView.pivotY = if (revealFromTopToBottom) 0f else originalHeightInPx.toFloat()
            lengthInPx = 0f
        }
    }

}