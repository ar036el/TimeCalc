package com.arealapps.timecalc.calculation_engine.calculatorCoordinator.calcAnimation

import android.view.ViewGroup
import com.arealapps.timecalc.calculation_engine.calculatorCoordinator.DisplayCoordinator
import com.arealapps.timecalc.calculation_engine.result.ErrorResult
import com.arealapps.timecalc.calculatorActivity.ui.calculator.resultLayout.ResultLayout
import com.arealapps.timecalc.helpers.native_.PxPoint
import com.arealapps.timecalc.utils.RevealManager


class ErrorResultRevealAnimation(
    private val revealManager: RevealManager,
    private val RevealManagerDrawingSurface: ViewGroup,
    private val displayCoordinator: DisplayCoordinator,
    private val errorResultToDisplay: ErrorResult,
    private val resultLayout: ResultLayout,
    private val doWhenFinished: () -> Unit
): CalcAnimation {
    override var isRunning = true

    private val BUBBLE_REVEAL_EXPAND_DURATION = 400L
    private val BUBBLE_REVEAL_FADE_DURATION = 350L
    private val BUBBLE_REVEAL_DELAY_BEFORE_FADE = 50L

    override fun finish() {
        if (!isRunning) { return }
        isRunning = false

        revealManager.removeListener(revealListener)
        revealManager.clearRevealIfRunning()
        displayCoordinator.setResultRevealPercentage(1f)
        resultLayout.updateResult(errorResultToDisplay)
        displayCoordinator.areResultGesturesEnabled = true
        doWhenFinished()
    }

    private fun start() {
        displayCoordinator.isExpressionTextEditEnabled = false
        displayCoordinator.areResultGesturesEnabled = false
        startErrorBubbleReveal()
    }

    private fun startErrorBubbleReveal() {
        revealManager.addListener(revealListener)
        revealManager.startBubbleReveal(
            PxPoint(RevealManagerDrawingSurface.width.toFloat(), RevealManagerDrawingSurface.height.toFloat()),
            PxPoint(0f, 0f),
            BUBBLE_REVEAL_EXPAND_DURATION,
            BUBBLE_REVEAL_DELAY_BEFORE_FADE,
            BUBBLE_REVEAL_FADE_DURATION,
            RevealManager.RevealStyles.Error
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
                    displayCoordinator.setResultRevealPercentage(1f)
                    resultLayout.updateResult(errorResultToDisplay)
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
