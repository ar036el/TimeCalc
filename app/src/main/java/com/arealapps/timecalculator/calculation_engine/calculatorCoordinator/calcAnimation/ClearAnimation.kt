package com.arealapps.timecalculator.calculation_engine.calculatorCoordinator.calcAnimation

import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import com.arealapps.timecalculator.calculation_engine.calculatorCoordinator.DisplayCoordinator
import com.arealapps.timecalculator.utils.RevealManager


class ClearAnimation(
    private val revealManager: RevealManager,
    private val RevealManagerDrawingSurface: ViewGroup,
    private val clearDisplayFunc: () -> Unit,
    private val displayCoordinator: DisplayCoordinator,
    private val doWhenFinished: () -> Unit
): CalculatorAnimation {
    override var isRunning = true

    private val RECT_REVEAL_EXPAND_DURATION = 300L
    private val RECT_REVEAL_FADE_DURATION = 250L
    private val RECT_REVEAL_DELAY_BEFORE_FADE = 50L

    override fun finish() {
        if (!isRunning) { return }
        isRunning = false

        revealManager.removeListener(revealListener)
        revealManager.clearRevealIfRunning()

        clearDisplayFunc.invoke()
        displayCoordinator.setResultRevealPercentage(0f)
        displayCoordinator.areResultGesturesEnabled = true
        doWhenFinished()
    }

    private fun start() {
        displayCoordinator.isExpressionTextEditEnabled = false
        displayCoordinator.areResultGesturesEnabled = false
        startClearRectReveal()
    }

    private fun startClearRectReveal() {
        revealManager.addListener(revealListener)
        revealManager.startVerticalRectReveal(
            0f,
            RevealManagerDrawingSurface.width.toFloat(),
            RevealManagerDrawingSurface.height.toFloat(),
            0f,
            RECT_REVEAL_EXPAND_DURATION,
            RECT_REVEAL_DELAY_BEFORE_FADE,
            RECT_REVEAL_FADE_DURATION,
            RevealManager.RevealStyles.Normal,
            AccelerateDecelerateInterpolator()
        )
    }

    private val revealListener = object : RevealManager.Listener {
        override fun stateHasChanged(
            subject: RevealManager,
            oldState: RevealManager.States,
            newState: RevealManager.States,
        ) {
            when (oldState) {
                RevealManager.States.Inactive -> Unit
                RevealManager.States.IsExpanding -> {
                    clearDisplayFunc.invoke()
                    displayCoordinator.setResultRevealPercentage(0f)
                }
                RevealManager.States.IsFading -> {
                    revealManager.removeListener(this)
                    displayCoordinator.areResultGesturesEnabled = true
                    isRunning = false
                    doWhenFinished()
                }
            }
        }
    }



    init { start() }
}
