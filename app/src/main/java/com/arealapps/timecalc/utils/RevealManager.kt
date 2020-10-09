package com.arealapps.timecalc.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.annotation.ColorInt
import androidx.core.content.ContextCompat
import androidx.core.view.contains
import com.arealapps.timecalc.R
import com.arealapps.timecalc.helpers.android.AnimatorListener
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.helpers.native_.PxPoint
import com.arealapps.timecalc.helpers.native_.getDistance
import com.arealapps.timecalc.utils.RevealManager.States.*
import java.lang.IllegalStateException
import kotlin.math.abs

interface RevealManager : HoldsListeners<RevealManager.Listener> {
    val drawingSurface: FrameLayout

    /** PxPoints have to be relative to drawing surface.
     * @throws [IllegalStateException] if [currentState] is not [States.Inactive]*/
    fun startBubbleReveal(
        fromRelative: PxPoint,
        toRelative: PxPoint,
        expandDuration: Long,
        delayBeforeFadeDuration: Long,
        fadeDuration: Long,
        style: RevealStyles
    )

    /** px coords have to be relative to drawing surface.
     * @throws [IllegalStateException] if [currentState] is not [States.Inactive]*/
    fun startVerticalRectReveal(
        startX: Float,
        endX: Float,
        fromY: Float,
        toY: Float,
        expandDuration: Long,
        delayBeforeFadeDuration: Long,
        fadeDuration: Long,
        style: RevealStyles
    )

    fun clearRevealIfRunning()

    val currentState: States

    enum class States { Inactive, IsExpanding, IsFading }

    interface Listener {
        fun stateHasChanged(subject: RevealManager, oldState: States, newState: States)
    }

    enum class RevealStyles { Normal, Error }
}


class RevealManagerImpl(
    override val drawingSurface: FrameLayout,
    private val listenersMgr: ListenersManager<RevealManager.Listener> = ListenersManager(),
) : RevealManager, HoldsListeners<RevealManager.Listener> by listenersMgr {

    override var currentState = Inactive
        private set(newState) {
            if (field == newState) {
                return
            }
            val oldState = field
            field = newState
            listenersMgr.notifyAll { it.stateHasChanged(this, oldState, newState) }
        }

    private var currentReveal: Reveal? = null

    private var expandAnimation: PercentAnimation? = null
    private var delayAnimation: PercentAnimation? = null
    private var fadeAnimation: PercentAnimation? = null

    override fun startBubbleReveal(
        fromRelative: PxPoint,
        toRelative: PxPoint,
        expandDuration: Long,
        delayBeforeFadeDuration: Long,
        fadeDuration: Long,
        style: RevealManager.RevealStyles
    ) {
        val maxRadius = getDistance(fromRelative, toRelative)
        val reveal = Bubble(fromRelative, getRevealColor(style))
        startReveal(reveal, maxRadius, expandDuration, delayBeforeFadeDuration, fadeDuration)
    }

    override fun startVerticalRectReveal(
        startX: Float,
        endX: Float,
        fromY: Float,
        toY: Float,
        expandDuration: Long,
        delayBeforeFadeDuration: Long,
        fadeDuration: Long,
        style: RevealManager.RevealStyles
    ) {
        if (startX > endX) {
            throw InternalError("startX[$startX] > endX[$endX]")
        }
        val reveal = VerticalRect(startX, endX, fromY, (toY > fromY), getRevealColor(style))
        startReveal(reveal, abs(fromY - toY), expandDuration, delayBeforeFadeDuration, fadeDuration)
    }

    override fun clearRevealIfRunning() {
        expandAnimation?.cancel()
        delayAnimation?.cancel()
        fadeAnimation?.cancel()
        currentState = Inactive
        removeReveal()
    }


    @ColorInt private fun getRevealColor(style: RevealManager.RevealStyles): Int {
        return when (style) {
            RevealManager.RevealStyles.Normal -> ContextCompat.getColor(drawingSurface.context, R.color.revealColor_normal)
            RevealManager.RevealStyles.Error -> ContextCompat.getColor(drawingSurface.context, R.color.revealColor_error)
        }
    }

    private fun startReveal(
        reveal: Reveal,
        maxLength: Float,
        expandDuration: Long,
        delayBeforeFadeDuration: Long,
        fadeDuration: Long,
    ) {
        if (currentState != Inactive) {
            throw IllegalStateException()
        }
        if (currentReveal != null) {
            throw InternalError()
        }

        fadeAnimation = PercentAnimation(
            fadeDuration, null,
            { setRevealFadePercentage(it) },
            { removeReveal(); currentState = Inactive }
        )

        delayAnimation = PercentAnimation(
            delayBeforeFadeDuration, null,
            {},
            { fadeAnimation!!.start() }
        )

        expandAnimation =  PercentAnimation(
            expandDuration, null,
            { setRevealLength(maxLength, it) },
            { currentState = IsFading; delayAnimation!!.start() }
        )

        currentReveal = reveal
        currentState = IsExpanding
        expandAnimation!!.start()
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
        doOnFinish: (() -> Unit)? = null,
    ) {
        val valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        valueAnimator.apply {
            addUpdateListener { animation ->
                doOnUpdate(animation.animatedValue as Float)
            }
            addListener(object : AnimatorListener {
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
        val color: Int
        var lengthInPx: Float
        var alpha: Float
        fun delete()
    }

    private inner class Bubble(
        relativeStartingLocation: PxPoint,
        @ColorInt override val color: Int
    ) : Reveal {
        private val originalLengthInPx = 1
        private val imageView: ImageView = ImageView(drawingSurface.context)

        override var lengthInPx: Float = 0f //lateinit
            set(newLength) {
                field = newLength
                imageView.scaleX = (newLength * 2 + 1) / originalLengthInPx
                imageView.scaleY = (newLength * 2 + 1) / originalLengthInPx
            }

        override var alpha
            get() = imageView.alpha
            set(value) {
                imageView.alpha = value
            }

        override fun delete() {
            if (drawingSurface.contains(imageView)) {
                drawingSurface.removeView(imageView)
            }
        }

        init {
            imageView.layoutParams = ViewGroup.LayoutParams(originalLengthInPx, originalLengthInPx)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.setImageResource(R.drawable.reveal_circle)
            imageView.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
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
        revealFromTopToBottom: Boolean,
        @ColorInt override val color: Int
    ) : Reveal {
        private val originalHeightInPx = 1
        private val imageView: ImageView = ImageView(drawingSurface.context)

        override var lengthInPx = 0f //lateinit
            set(newLength) {
                field = newLength
                imageView.scaleY = (newLength + 1) / originalHeightInPx
            }

        override var alpha
            get() = imageView.alpha
            set(value) {
                imageView.alpha = value
            }

        override fun delete() {
            drawingSurface.removeView(imageView)
        }

        init {
            imageView.layoutParams =
                ViewGroup.LayoutParams((endX - startX).toInt(), originalHeightInPx)
            imageView.scaleType = ImageView.ScaleType.FIT_XY
            imageView.setImageResource(R.drawable.reveal_square)
            imageView.setColorFilter(color, android.graphics.PorterDuff.Mode.SRC_IN);
            drawingSurface.addView(imageView)
            imageView.x = startX
            imageView.y = fromY

            imageView.pivotX = 0f
            imageView.pivotY = if (revealFromTopToBottom) 0f else originalHeightInPx.toFloat()
            lengthInPx = 0f
        }
    }

}