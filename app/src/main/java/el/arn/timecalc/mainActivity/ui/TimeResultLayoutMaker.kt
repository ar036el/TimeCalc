package el.arn.timecalc.mainActivity.ui

import android.animation.ValueAnimator
import android.util.Log
import android.view.GestureDetector
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.marginEnd
import androidx.core.view.marginStart
import el.arn.timecalc.R
import el.arn.timecalc.RootUtils
import el.arn.timecalc.helpers.android.doWhenDynamicVariablesAreReady
import el.arn.timecalc.helpers.android.widthByLayoutParams
import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.atoms.createZero
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.helpers.android.measureTextWidth
import el.arn.timecalc.helpers.android.stringFromRes
import el.arn.timecalc.helpers.native_.NonNullMap
import el.arn.timecalc.helpers.native_.checkIfPercentIsLegal
import el.arn.timecalc.helpers.native_.percentToValue
import el.arn.timecalc.helpers.listeners_engine.HoldsListeners
import el.arn.timecalc.helpers.listeners_engine.ListenersManager
import el.arn.timecalc.rootUtils
import kotlin.math.max
import kotlin.properties.Delegates

class TimeResultLayoutMaker(
    private val timeResultLayout: ConstraintLayout,
    private val timeResult: TimeResult
) {
    
    private val timeResultAsExpression = rootUtils.timeConverter.millisToTimeExpression(timeResult.totalMillis)

    private val blocks: Map<TimeUnit, TimeResultBlockLayout>


    private enum class BlockStates { Visible, VisibleMaximized, Hidden, Collapsed }
    private val blocksWithBlockStates = TimeUnit.asList.map { Pair(it, BlockStates.Visible)}.toMap().toMutableMap()


    private var TimeUnit.blockState: BlockStates
        get() = blocksWithBlockStates.getValue(this)
        set(value) { blocksWithBlockStates[this] = value }


    private fun collapseNextVisibleBlock(block: TimeUnit): Boolean {
        if (valueAnimator?.isRunning == true) {
            return false
        }
        val blockToCollapse = block.allNext().firstOrNull { it.blockState == BlockStates.Visible || it.blockState == BlockStates.VisibleMaximized }
            ?: return false


        collapseOrExpandAnimation(blockToCollapse, block, true)
        blockToCollapse.blockState = BlockStates.Collapsed
        block.blockState = BlockStates.VisibleMaximized
        return true
    }

    private fun expandNextBlock(block: TimeUnit): Boolean {
        if (valueAnimator?.isRunning == true) {
            return false
        }
        val blockToExpand = block.allNext().firstOrNull { it.blockState == BlockStates.Visible || it.blockState == BlockStates.VisibleMaximized }?.prev()
            ?: block.allNext().lastOrNull()

        if (blockToExpand == null || blockToExpand.blockState != BlockStates.Collapsed) {
            return false
        }

        collapseOrExpandAnimation(blockToExpand, block, false)
        blockToExpand.blockState = BlockStates.Visible
        return true
    }

    val EXPAND_DURATION = 200L
    val MAXIMIZE_ICON_VISIBILITY_THRESHOLD = 0.3f


    private var valueAnimator: ValueAnimator? = null


    private fun collapseOrExpandAnimation(blockToAnimate: TimeUnit, hidingBlock: TimeUnit, isCollapse: Boolean): Boolean {
        if (valueAnimator?.isRunning == true) {
            throw InternalError()
        }

        valueAnimator = ValueAnimator.ofFloat(1f, 0f)
        var maximizedIconVisibilityChangeWasInvoked = false
        var wasInvoked2 = false

        valueAnimator!!.apply {
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float

                val blockHiddenPercentage = if (isCollapse) animatedValue else 1 - animatedValue
                blockToAnimate.setVisibilityPercentage(blockHiddenPercentage)


                if (animatedValue < MAXIMIZE_ICON_VISIBILITY_THRESHOLD && !maximizedIconVisibilityChangeWasInvoked) {
                    blockToAnimate.prev()?.let { blocks.getValue(it).isMaximizeSymbolVisible = isCollapse }
                    maximizedIconVisibilityChangeWasInvoked = true
                }

                if (animatedValue < 0.5f && !wasInvoked2) {
                    updateBlockTimeValue(hidingBlock)
                    wasInvoked2 = true
                }

                if (animatedValue == 1f) {
                    updateBlockTimeValue(blockToAnimate)
                }

            }
            duration = EXPAND_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        return true
    }

    private fun updateBlockTimeValue(timeUnit: TimeUnit) {
        val block = blocks.getValue(timeUnit)
        val collapsedBlocksAfter = timeUnit.allNext().takeWhile { it.blockState == BlockStates.Collapsed }
        var newTimeValue = timeResultAsExpression[timeUnit]
        collapsedBlocksAfter.forEach {
            newTimeValue += rootUtils.timeConverter.convertTimeUnit(timeResultAsExpression[it], it, timeUnit)
        }
        block.numberValue = newTimeValue
    }



    private fun TimeUnit.setVisibilityPercentage(percent: Float) { //todo better name
        checkIfPercentIsLegal(percent)
        blocks.getValue(this).widthInPercent = percent
    }

    private fun initTimeBlocksAndGetMap(): NonNullMap<TimeUnit, TimeResultBlockLayout> {
        val map = mutableMapOf<TimeUnit, TimeResultBlockLayout>()

        map[TimeUnit.Milli] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Milli,
            R.id.timeResultBlock_millisecond,
            R.color.timeResultBackground_millisecond,
            R.string.calculator_timeUnit_millisecond_full,
            timeResultAsExpression.millis
        )
        map[TimeUnit.Second] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Second,
            R.id.timeResultBlock_second,
            R.color.timeResultBackground_second,
            R.string.calculator_timeUnit_second_full,
            timeResultAsExpression.seconds
        )
        map[TimeUnit.Minute] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Minute,
            R.id.timeResultBlock_minute,
            R.color.timeResultBackground_minute,
            R.string.calculator_timeUnit_minute_full,
            timeResultAsExpression.minutes
        )
        map[TimeUnit.Hour] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Hour,
            R.id.timeResultBlock_hour,
            R.color.timeResultBackground_hour,
            R.string.calculator_timeUnit_hour_full,
            timeResultAsExpression.hours
        )
        map[TimeUnit.Day] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Day,
            R.id.timeResultBlock_day,
            R.color.timeResultBackground_day,
            R.string.calculator_timeUnit_day_full,
            timeResultAsExpression.days
        )
        map[TimeUnit.Week] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Week,
            R.id.timeResultBlock_week,
            R.color.timeResultBackground_week,
            R.string.calculator_timeUnit_week_full,
            timeResultAsExpression.weeks
        )
        map[TimeUnit.Month] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Month,
            R.id.timeResultBlock_month,
            R.color.timeResultBackground_month,
            R.string.calculator_timeUnit_month_full,
            timeResultAsExpression.months
        )
        map[TimeUnit.Year] = TimeResultBlockLayoutImpl(
            timeResultLayout,
            TimeUnit.Year,
            R.id.timeResultBlock_year,
            R.color.timeResultBackground_year,
            R.string.calculator_timeUnit_year_full,
            timeResultAsExpression.years
        )
        return NonNullMap(map.toMap())
    }

    private fun initBlocksClickListeners() {
        blocks.values.forEach{
            it.addListener(object: TimeResultBlockLayout.Listener {
                override fun onBlockSingleClick(subject: TimeResultBlockLayout) {
                    collapseNextVisibleBlock(subject.timeUnit)
                }

                override fun onBlockDoubleClick(subject: TimeResultBlockLayout) {
                    expandNextBlock(subject.timeUnit)
                }
            })
        }
    }

    private fun initBlocksStates() {
        blocks.values.forEach{

            val block = it.timeUnit

            if (timeResultAsExpression[block].equals(createZero())) {
                block.blockState = BlockStates.Hidden
                block.setVisibilityPercentage(0f)
            }


        }
    }

    init {
        blocks = initTimeBlocksAndGetMap()
        initBlocksClickListeners()

        //initBlocksStates()
    }
}

interface TimeResultBlockLayout : HoldsListeners<TimeResultBlockLayout.Listener> {
    val timeUnit: TimeUnit
    var numberValue: Num
    var widthInPercent: Float
    var isMaximizeSymbolVisible: Boolean

    interface Listener {
        fun onBlockSingleClick(subject: TimeResultBlockLayout)
        fun onBlockDoubleClick(subject: TimeResultBlockLayout)
    }
}

class TimeResultBlockLayoutImpl(
    private val containerLayout: ViewGroup,
    override val timeUnit: TimeUnit,
    @IdRes layout: Int,
    @ColorRes color: Int,
    @StringRes timeUnitString: Int,
    numberValue: Num,
    private val listenersMgr: ListenersManager<TimeResultBlockLayout.Listener> = ListenersManager()
): TimeResultBlockLayout, HoldsListeners<TimeResultBlockLayout.Listener> by listenersMgr {

    private val blockLayout: ViewGroup = containerLayout.findViewById(layout)
    private val blockBackground = blockLayout.findViewById<LinearLayout>(R.id.timeResultBlock_background)
    private val blockResizableContainer = blockLayout.findViewById<ViewGroup>(R.id.timeResultBlock_resizableContainer)
    private val blockFixedSizeContainer = blockLayout.findViewById<ViewGroup>(R.id.timeResultBlock_fixedSizeContainer)
    private val maximizeIcon = blockLayout.findViewById<ImageView>(R.id.timeResultBlock_maximizeIcon)
    private val numberValueTextView = blockLayout.findViewById<TextView>(R.id.timeResultBlock_numberTextView)
    private val timeUnitTextView = blockLayout.findViewById<TextView>(R.id.timeResultBlock_timeUnit)


    override var numberValue = createZero()
        set(value) {
            setNumberValueTextViewAndUpdateContainersWidth(value)
            field = value
        }

    override var widthInPercent: Float = 0f //lateinit
        set(percent) {
            checkIfPercentIsLegal(percent)
            blockResizableContainer.widthByLayoutParams = percentToValue(percent, 0f, blockMaxWidthByTextSizeMeasurement.get().toFloat()).toInt()
            field = percent
        }


    override var isMaximizeSymbolVisible: Boolean
        get() = maximizeIcon.visibility == View.INVISIBLE
        set(value) { maximizeIcon.visibility = if (value) View.VISIBLE else View.INVISIBLE }


    private fun setNumberValueTextViewAndUpdateContainersWidth(numberValue: Num) {
        numberValueTextView.text = numberValue.toStringWithGroupingFormatting()
        numberValueTextView.requestLayout()
        numberValueTextView.invalidate()
        widthInPercent = widthInPercent
    }

    private val blockMaxWidthByTextSizeMeasurement = object {
        private var blockMaxWidthWithoutAnyTextWidth by Delegates.notNull<Float>()
        fun get(): Int {
            return (blockMaxWidthWithoutAnyTextWidth + max(numberValueTextView.measureTextWidth(), timeUnitTextView.measureTextWidth())).toInt()
        }
        fun init() {
            fun View.getAllPaddingAndMarginsInWidth() = paddingStart + paddingEnd + marginStart + marginEnd
            val numberValueTextWidthAtInit = numberValueTextView.measureTextWidth()
            val timeUnitTextWidthAtInit = timeUnitTextView.measureTextWidth()
            val blockWidthMock = blockFixedSizeContainer.getAllPaddingAndMarginsInWidth() + blockBackground.getAllPaddingAndMarginsInWidth() + max(numberValueTextView.measureTextWidth() + numberValueTextView.getAllPaddingAndMarginsInWidth(), timeUnitTextView.measureTextWidth() + timeUnitTextView.getAllPaddingAndMarginsInWidth())
            blockMaxWidthWithoutAnyTextWidth = blockWidthMock - max(numberValueTextWidthAtInit, timeUnitTextWidthAtInit)
            blockFixedSizeContainer.doWhenDynamicVariablesAreReady {
                if (blockWidthMock != blockFixedSizeContainer.width.toFloat()) {
                    Log.w("", "widths are lot the same. blockFixedSizeContainer.width[${ blockFixedSizeContainer.width}] blockWidthMock[$blockWidthMock]") //todo take care of that!! somehow it works but it does not suppose to!!
                }
            }
        }
    }


    private val blockLayoutGestureDetector = GestureDetector(containerLayout.context, object: GestureDetector.SimpleOnGestureListener() {
        override fun onDown(e: MotionEvent?): Boolean {
            return true
        }
        override fun onSingleTapConfirmed(e: MotionEvent?): Boolean {
            listenersMgr.notifyAll { it.onBlockSingleClick(this@TimeResultBlockLayoutImpl) }
            return true
        }
        override fun onDoubleTap(e: MotionEvent?): Boolean {
            listenersMgr.notifyAll { it.onBlockDoubleClick(this@TimeResultBlockLayoutImpl) }
            return true
        }
    })


    init {
        blockBackground.setBackgroundResource(color)
        timeUnitTextView.text = stringFromRes(timeUnitString)

        blockLayout.setOnTouchListener { view, motionEvent -> blockLayoutGestureDetector.onTouchEvent(motionEvent) }

        isMaximizeSymbolVisible = false

        blockMaxWidthByTextSizeMeasurement.init()
        widthInPercent = 1f
        this.numberValue = numberValue

    }
}