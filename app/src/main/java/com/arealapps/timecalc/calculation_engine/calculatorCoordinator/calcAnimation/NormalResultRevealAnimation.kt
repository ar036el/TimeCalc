package com.arealapps.timecalc.calculation_engine.calculatorCoordinator.calcAnimation

import android.view.animation.AccelerateDecelerateInterpolator
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.DisplayCoordinator
import com.arealapps.timecalc.utils.PercentAnimation


class NormalResultRevealAnimation(
    private val displayCoordinator: DisplayCoordinator,
    private val doWhenFinished: () -> Unit
): CalculatorAnimation {

    override var isRunning = true

    private val REVEAL_DURATION = 500L
    private var currentPercentAnimation: PercentAnimation? = null

    override fun finish() {
        if (!isRunning) { return }
        isRunning = false

        currentPercentAnimation?.cancel()
        displayCoordinator.setResultRevealPercentage(1f)
        displayCoordinator.areResultGesturesEnabled = true
        doWhenFinished()
    }

    private fun start() {
        displayCoordinator.isExpressionTextEditEnabled = false
        displayCoordinator.areResultGesturesEnabled = false
        startPercentAnimation()
    }

    private fun startPercentAnimation() {
        currentPercentAnimation = PercentAnimation(
            REVEAL_DURATION,
            AccelerateDecelerateInterpolator(),
            { displayCoordinator.setResultRevealPercentage(it) },
            {
                displayCoordinator.areResultGesturesEnabled = true
                isRunning = false
                doWhenFinished()
            },
            true
        )
    }

    init { start() }
}