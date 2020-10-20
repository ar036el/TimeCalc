import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.basics.Num
import com.arealapps.timecalc.calculation_engine.basics.createZero
import com.arealapps.timecalc.calculation_engine.symbol.TimeUnit
import com.arealapps.timecalc.helpers.android.*
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.helpers.native_.checkIfPercentIsLegal
import com.arealapps.timecalc.helpers.native_.percentToValue

interface TimeBlock : HoldsListeners<TimeBlock.Listener> {
    val timeUnit: TimeUnit
    var number: Num
    var visibilityPercentage: Float
    var isMaximizedSymbolVisible: Boolean

    fun getBlockView(): View

    interface Listener {
        fun onBlockSingleClick(subject: TimeBlock)
        fun onBlockDoubleClick(subject: TimeBlock)
        fun blockWidthHasChanged(subject: TimeBlock, newWidth: Int)
    }
}

class TimeBlockImpl(
    private val containerLayout: ViewGroup,
    override val timeUnit: TimeUnit,
    @IdRes layout: Int,
    @ColorRes color: Int,
    @StringRes timeUnitString: Int,
    number: Num,
    private val listenersMgr: ListenersManager<TimeBlock.Listener> = ListenersManager()
): TimeBlock, HoldsListeners<TimeBlock.Listener> by listenersMgr {

    private val blockLayout: ViewGroup = containerLayout.findViewById(layout)
    private val blockBackground = blockLayout.findViewById<LinearLayout>(R.id.timeResultBlock_background)
    private val blockResizableContainer = blockLayout.findViewById<ViewGroup>(R.id.timeResultBlock_resizableContainer)
    private val blockFixedSizeContainer = blockLayout.findViewById<ViewGroup>(R.id.timeResultBlock_fixedSizeContainer)
    private val maximizeIcon = blockLayout.findViewById<ImageView>(R.id.timeResultBlock_maximizeIcon)
    private val numberTextView = blockLayout.findViewById<TextView>(R.id.timeResultBlock_numberTextView)
    private val timeUnitTextView = blockLayout.findViewById<TextView>(R.id.timeResultBlock_timeUnit)
    private val blockPaddingEnd = dimenFromResAsPx(R.dimen.timeResultBlock_layoutPaddingEnd)

    override var number = createZero()
        set(value) {
            setNumberTextView(value)
            field = value
        }

    override var visibilityPercentage: Float = 0f //lateinit
        set(percent) {
            checkIfPercentIsLegal(percent)
            field = percent
            updateWidth()
        }

    private fun updateWidth() {

        val fullWidth = blockFixedSizeContainer.width + blockPaddingEnd
        val percentedWidth = percentToValue(visibilityPercentage, 0f, fullWidth).toInt()
        if (blockResizableContainer.width == percentedWidth) {
            return
        }
        blockResizableContainer.widthByLayoutParams = percentedWidth
        blockResizableContainer.doWhenDynamicVariablesAreReady {
            listenersMgr.notifyAll { it.blockWidthHasChanged(this, percentedWidth) }
        }
    }


    override var isMaximizedSymbolVisible: Boolean
        get() = maximizeIcon.visibility == View.INVISIBLE
        set(isVisible) { maximizeIcon.visibility = if (isVisible) View.VISIBLE else View.INVISIBLE }

    override fun getBlockView(): View {
        return blockLayout
    }

    private fun setNumberTextView(number: Num) {
        numberTextView.text = number.toStringWithGroupingFormatting()
        numberTextView.requestLayout()
        numberTextView.invalidate()
        numberTextView.doWhenDynamicVariablesAreReady {
            updateWidth()
        }
    }

//    private val estimatedLayoutMaxWidth = object {
//        private var widthWithoutAnyTextContent by Delegates.notNull<Float>()
//        fun get(): Float {
//            return (widthWithoutAnyTextContent + max(numberTextView.measureTextWidth(), timeUnitTextView.measureTextWidth()))
//        }
//        fun init() {
//            fun View.getAllPaddingAndMarginsInWidth() = paddingStart + paddingEnd + marginStart + marginEnd
//            val numberValueTextWidthAtInit = numberTextView.measureTextWidth()
//            val timeUnitTextWidthAtInit = timeUnitTextView.measureTextWidth()
//            val blockWidthMock = blockFixedSizeContainer.getAllPaddingAndMarginsInWidth() + blockBackground.getAllPaddingAndMarginsInWidth() + max(numberTextView.measureTextWidth() + numberTextView.getAllPaddingAndMarginsInWidth(), timeUnitTextView.measureTextWidth() + timeUnitTextView.getAllPaddingAndMarginsInWidth())
//            widthWithoutAnyTextContent = blockWidthMock - max(numberValueTextWidthAtInit, timeUnitTextWidthAtInit)
//            blockFixedSizeContainer.doWhenDynamicVariablesAreReady {
//                if (blockWidthMock != blockFixedSizeContainer.width.toFloat()) {
//                    Log.w("", "widths are tot the same. blockFixedSizeContainer.width[${ blockFixedSizeContainer.width}] blockWidthMock[$blockWidthMock]") //todo take care of that!! somehow it works but it does not suppose to!!
//                }
//            }
//        }
//    }


    private val blockLayoutGestureDetector = GestureDetector(containerLayout.context, object: GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            listenersMgr.notifyAll { it.onBlockSingleClick(this@TimeBlockImpl) }
            return true
        }
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            listenersMgr.notifyAll { it.onBlockDoubleClick(this@TimeBlockImpl) }
            return true
        }
    })


    init {
        blockBackground.setBackgroundResource(color)
        timeUnitTextView.text = stringFromRes(timeUnitString)

        blockLayout.setOnTouchListener { view, motionEvent -> blockLayoutGestureDetector.onTouchEvent(motionEvent) }

        isMaximizedSymbolVisible = false

//        estimatedLayoutMaxWidth.init()
        visibilityPercentage = 1f
        this.number = number

    }
}