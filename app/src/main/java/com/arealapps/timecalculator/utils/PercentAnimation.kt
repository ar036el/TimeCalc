package com.arealapps.timecalculator.utils

import android.animation.Animator
import android.animation.ValueAnimator
import android.view.animation.Interpolator
import com.arealapps.timecalculator.helpers.android.AnimatorListener

class PercentAnimation(
    private val duration: Long,
    private val interpolator: Interpolator? = null,
    private val doOnUpdate: (percent: Float) -> Unit,
    private val doOnFinish: (() -> Unit)? = null,
    startOnInit: Boolean = false,
    private val direction: Directions = Directions.ZeroToOne,
) {
    enum class States { Ready, Running, Finished, Cancelled }
    enum class Directions { ZeroToOne, OneToZero }

    var state: States = States.Ready
        private set

    var valueAnimator: ValueAnimator? = null
    var wasStopped = false

    fun start(): Boolean {
        if (state != States.Ready) { return false }
        state = States.Running
        valueAnimator = when (direction) {
            Directions.ZeroToOne -> ValueAnimator.ofFloat(0f, 1f)
            Directions.OneToZero -> ValueAnimator.ofFloat(1f, 0f)
        }
        valueAnimator!!.apply {
            addUpdateListener {animation ->
                doOnUpdate(animation.animatedValue as Float)
            }
            addListener(object: AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
                    if (!wasStopped) {
                        state = States.Finished
                        doOnFinish?.invoke()
                    }
                }
            })
            duration = this@PercentAnimation.duration
            interpolator = this@PercentAnimation.interpolator
            start()
        }
        return true
    }

    fun finish(): Boolean {
        if (state != States.Running) { return false }
        wasStopped = true
        valueAnimator?.cancel()
        state = States.Finished
        doOnFinish?.invoke()
        return true
    }

    fun cancel(): Boolean {
        if (state != States.Running) { return false }
        wasStopped = true
        valueAnimator?.cancel()
        state = States.Cancelled
        return true
    }

    init {
        if (startOnInit) {
            start()
        }
    }
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

val PercentAnimation.isRunning get() = state == PercentAnimation.States.Running