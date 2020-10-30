package com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.resultLayout

import android.view.ViewGroup
import com.arealapps.timecalculator.helpers.android.*
import kotlin.math.max
import kotlin.math.min

interface AutosizeApplier {
    fun updateLayoutSize(maxHeight: Float? = null, doWhenFinished: (() -> Unit)? = null)
    fun getActualMaxHeightForCurrentResult(): Float
}

class AutosizeApplierImpl(
    private val containerForResize: ViewGroup,
    private val containerForScaleAndSourceSize: ViewGroup,
    private val widthThresholdInPx: Float,
    private val minHeight: Float,
    private var maxHeight: Float,
): AutosizeApplier {


    private var currentSizeScaleFactor = 1f
    private var prevUnscaledWidth: Float? = null
    private var prevUnscaledHeight: Float? = null

    override fun getActualMaxHeightForCurrentResult(): Float {
        val unscaledWidth = containerForScaleAndSourceSize.width.toFloat()
        val unscaledHeight = containerForScaleAndSourceSize.height.toFloat()

        if (unscaledWidth == 0f || unscaledHeight == 0f) {
            return 0f
        }
        val unboundedScale = widthThresholdInPx / unscaledWidth
        return max(unboundedScale * unscaledHeight, minHeight)
    }

    override fun updateLayoutSize(maxHeight: Float?, doWhenFinished: (() -> Unit)?) {
        if (maxHeight != null) {
            this.maxHeight = maxHeight
            checkIfMinMaxBoundsIsLegal()
        }

        containerForResize.doWhenDynamicVariablesAreReady {
            containerForScaleAndSourceSize.doWhenDynamicVariablesAreReady {

                //containerSource.width and containerSource.height are never affected by scaleX/scaleY changes. been tested! :#
                val unscaledWidth = containerForScaleAndSourceSize.width.toFloat().let { if (it == 0f) 1f else it } //making it 1f for no error in calculation (divide by 0)
                val unscaledHeight = containerForScaleAndSourceSize.height.toFloat().let { if (it == 0f) 1f else it }

                val unboundedScale = widthThresholdInPx / unscaledWidth
                val unboundedHeight = unboundedScale * unscaledHeight
                val boundedHeight = min(max(unboundedScale * unscaledHeight, minHeight), this.maxHeight)
                val boundedScale = unboundedScale * (boundedHeight / unboundedHeight)


                if (currentSizeScaleFactor == boundedScale && prevUnscaledWidth == unscaledWidth && prevUnscaledHeight == unscaledHeight) {
                    doWhenFinished?.invoke()
                    return@doWhenDynamicVariablesAreReady
                }
                prevUnscaledWidth = unscaledWidth
                prevUnscaledHeight = unscaledHeight

                currentSizeScaleFactor = boundedScale

                containerForScaleAndSourceSize.scaleX = currentSizeScaleFactor
                containerForScaleAndSourceSize.scaleY = currentSizeScaleFactor
                containerForResize.widthByLayoutParams = (unscaledWidth*currentSizeScaleFactor).toInt() + containerForResize.paddingX
                containerForResize.heightByLayoutParams = (unscaledHeight*currentSizeScaleFactor).toInt() + containerForResize.paddingY
                containerForScaleAndSourceSize.invalidate()
                containerForScaleAndSourceSize.requestLayout()
                containerForResize.doWhenDynamicVariablesAreReady {
                    containerForScaleAndSourceSize.doWhenDynamicVariablesAreReady {
                        doWhenFinished?.invoke()
                    }
                }
            }
        }
    }

    private fun checkIfMinMaxBoundsIsLegal() {
        if (minHeight > maxHeight) {
            throw InternalError("minWidth[$minHeight] > maxWidth[$maxHeight]")
        }
    }
    init {
        checkIfMinMaxBoundsIsLegal()
    }
}