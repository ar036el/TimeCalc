package el.arn.timecalc

import android.animation.ValueAnimator
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
import el.arn.timecalc.android_extensions.widthByLayoutParams
import el.arn.timecalc.calculator_core.calculation_engine.*
import el.arn.timecalc.helperss.checkIfPercentIsLegal
import el.arn.timecalc.helperss.percentToValue
import el.arn.timecalc.listeners_engine.HoldsListeners
import el.arn.timecalc.listeners_engine.ListenersManager

class TimeResultLayout(
    private val timeResultArtifact: ConstraintLayout,
    private val timeResult: TimeResult
) {

    private val blocks: NonNullMap<TimeUnit, TimeResultBlockLayout>

    enum class BlockStates { Visible, VisibleMaximized, Hidden, Collapsed }
    private val blocksWithBlockStates = TimeUnit.asList.map { Pair(it, BlockStates.Visible)}.toMap().toMutableMap()


    fun getBlockState(timeUnit: TimeUnit): BlockStates {
        return blocksWithBlockStates.getValue(timeUnit)
    }
    fun setBlockState(timeUnit: TimeUnit, blockState: BlockStates) {
        blocksWithBlockStates[timeUnit] = blockState
    }

    fun collapseNext(timeUnit: TimeUnit): Boolean {
        if (valueAnimator?.isRunning == true) {
            return false
        }
        val blockToCollapse = timeUnit.allNext().firstOrNull { setOf(BlockStates.Visible, BlockStates.VisibleMaximized).contains(getBlockState(it)) }
            ?: return false

        collapseOrExpandAnimation(blockToCollapse, timeUnit, true)
        setBlockState(blockToCollapse, BlockStates.Collapsed)
        setBlockState(timeUnit, BlockStates.VisibleMaximized)
        return true
    }
    fun expandNext(timeUnit: TimeUnit): Boolean {
        if (valueAnimator?.isRunning == true) {
            return false
        }
        var blockToExpand = timeUnit.allNext().firstOrNull { setOf(BlockStates.Visible, BlockStates.VisibleMaximized).contains(getBlockState(it)) }?.prev()
            ?: timeUnit.allNext().lastOrNull()
        if (blockToExpand == null || getBlockState(blockToExpand) != BlockStates.Collapsed) {
            return false
        }

        collapseOrExpandAnimation(blockToExpand, timeUnit, false)
        setBlockState(blockToExpand, BlockStates.Visible)
        return true
    }

    val EXPAND_DURATION = 200L
    val MAXIMIZE_ICON_VISIBILITY_THRESHOLD = 0.7f


    private var valueAnimator: ValueAnimator? = null

    private fun collapseOrExpandAnimation(blockToAnimate: TimeUnit, hidingBlock: TimeUnit, isCollapse: Boolean): Boolean {
        if (valueAnimator?.isRunning == true) {
            throw InternalError()
        }

        valueAnimator = ValueAnimator.ofFloat(0f, 1f)
        var maximizedIconVisibilityChangeWasInvoked = false
        var wasInvoked2 = false

        valueAnimator!!.apply {
            addUpdateListener { animation ->
                val animatedValue = animation.animatedValue as Float
                val newPositionPercent = if (isCollapse) animatedValue else 1 - animatedValue
                setPositionFor(blockToAnimate, newPositionPercent)

                if (animatedValue > MAXIMIZE_ICON_VISIBILITY_THRESHOLD && !maximizedIconVisibilityChangeWasInvoked) {
                    blockToAnimate.prev()?.let { blocks[it].isMaximizeSymbolVisible = isCollapse }
                    maximizedIconVisibilityChangeWasInvoked = true
                }

                if (animatedValue > 0.5f && !wasInvoked2) {
                    updateBlockTimeValue(hidingBlock)
                    wasInvoked2 = true
                }


            }
            duration = EXPAND_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()
        }
        return true
    }

    private fun showBlock(timeUnit: TimeUnit) {
        if (getBlockState(timeUnit) == BlockStates.Hidden) {
            throw InternalError("cannot show a hidden timeUnit[$timeUnit]")
        }
        if (getBlockState(timeUnit) == BlockStates.Collapsed) {
            blocksWithBlockStates[timeUnit] = BlockStates.Visible
            setPositionFor(timeUnit, 0f)
        }
    }
    private fun setBlockToVisibleMaximized(timeUnit: TimeUnit) {
        if (timeUnit.next() == null) {
            throw InternalError("cannot maximize last time block")
        }
        blocksWithBlockStates[timeUnit] = BlockStates.VisibleMaximized
        setPositionFor(timeUnit, 0f)
    }
    private fun setBlockToCollapsed(timeUnit: TimeUnit) {
        if (timeUnit.prev() == null) {
            throw InternalError("cannot collapse first time block")
        }
        blocksWithBlockStates[timeUnit] = BlockStates.Collapsed
        setPositionFor(timeUnit, 1f)
    }
    private fun setBlockToHidden(timeUnit: TimeUnit) {
        blocksWithBlockStates[timeUnit] = BlockStates.Hidden
        setPositionFor(timeUnit, 1f)
    }

    private fun updateBlockTimeValue(timeUnit: TimeUnit) {
        val block = blocks[timeUnit]
        val collapsedBlocksAfter = timeUnit.allNext().takeWhile { getBlockState(it) == BlockStates.Collapsed }
        var newTimeValue = timeResult[timeUnit]
        collapsedBlocksAfter.forEach {
            val aaatesto = TimeUnitConverter.convert(timeResult[it], it, timeUnit)
            newTimeValue += TimeUnitConverter.convert(timeResult[it], it, timeUnit).toLong()
        }
        block.timeValue = newTimeValue
    }


    private fun getEndBackgroundCoverRelativeXPos(): Float {
        return timeResultArtifact.findViewById<View>(R.id.timeResultBlock_end_backgroundCover).x - timeResultArtifact.x
    }


    fun setPositionFor(timeUnit: TimeUnit, percent: Float) { //todo better name
        checkIfPercentIsLegal(percent)
        val nextTimeUnit = TimeUnit.asList.prev(timeUnit)
        val nextTimeUnitXPos = nextTimeUnit?.let { blocks[it].xPosRelativeToParent } ?: getEndBackgroundCoverRelativeXPos()
        val marginStart = if (nextTimeUnit != null) dimenFromRes(R.dimen.timeResultBlock_marginStart) else 0f
        val minXPos = nextTimeUnitXPos - blocks[timeUnit].blockLayout.width - marginStart
        val maxXPos = nextTimeUnitXPos

        val currentXPos = blocks[timeUnit].xPosRelativeToParent
        val incrementXPosIn = percentToValue(percent, minXPos, maxXPos) - currentXPos

        val toIncrement = mutableSetOf(timeUnit)
        toIncrement.addAll(TimeUnit.asList.allNext(timeUnit))
        toIncrement.forEach {
            blocks[it].xPosRelativeToParent += incrementXPosIn
        }
    }

    private fun initTimeBlocks(): Map<TimeUnit, TimeResultBlockLayout> {
        val map = mutableMapOf<TimeUnit, TimeResultBlockLayout>()

        map[TimeUnit.Milli] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Milli,
            R.id.timeResultBlock_millisecond,
            R.id.timeResultBlock_millisecond_backgroundCover,
            R.color.timeResultBackground_millisecond,
            R.string.calculator_timeUnit_millisecond_full,
            timeResult.millis
        )
        map[TimeUnit.Second] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Second,
            R.id.timeResultBlock_second,
            R.id.timeResultBlock_second_backgroundCover,
            R.color.timeResultBackground_second,
            R.string.calculator_timeUnit_second_full,
            timeResult.seconds
        )
        map[TimeUnit.Minute] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Minute,
            R.id.timeResultBlock_minute,
            R.id.timeResultBlock_minute_backgroundCover,
            R.color.timeResultBackground_minute,
            R.string.calculator_timeUnit_minute_full,
            timeResult.minutes
        )
        map[TimeUnit.Hour] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Hour,
            R.id.timeResultBlock_hour,
            R.id.timeResultBlock_hour_backgroundCover,
            R.color.timeResultBackground_hour,
            R.string.calculator_timeUnit_hour_full,
            timeResult.hours
        )
        map[TimeUnit.Day] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Day,
            R.id.timeResultBlock_day,
            R.id.timeResultBlock_day_backgroundCover,
            R.color.timeResultBackground_day,
            R.string.calculator_timeUnit_day_full,
            timeResult.days
        )
        map[TimeUnit.Week] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Week,
            R.id.timeResultBlock_week,
            R.id.timeResultBlock_week_backgroundCover,
            R.color.timeResultBackground_week,
            R.string.calculator_timeUnit_week_full,
            timeResult.weeks
        )
        map[TimeUnit.Month] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Month,
            R.id.timeResultBlock_month,
            R.id.timeResultBlock_month_backgroundCover,
            R.color.timeResultBackground_month,
            R.string.calculator_timeUnit_month_full,
            timeResult.months
        )
        map[TimeUnit.Year] = TimeResultBlockLayoutImpl(
            timeResultArtifact,
            TimeUnit.Year,
            R.id.timeResultBlock_year,
            R.id.timeResultBlock_year_backgroundCover,
            R.color.timeResultBackground_year,
            R.string.calculator_timeUnit_year_full,
            timeResult.years
        )


        return map.toMap()
    }

    init {
        blocks = NonNullMap(initTimeBlocks())

        blocks.forEach{
            it.value.addListener(object: TimeResultBlockLayout.Listener {
                override fun onBlockSingleClick(subject: TimeResultBlockLayout) {
                    collapseNext(subject.timeUnit)
                }

                override fun onBlockDoubleClick(subject: TimeResultBlockLayout) {
                    expandNext(subject.timeUnit)
                }
            })
        }

        val millisResizeAnchor = timeResultArtifact.findViewById<View>(R.id.timeResultBlock_millisecond_resizeAnchor)
        millisResizeAnchor.widthByLayoutParams = 100
        timeResultArtifact.invalidate()
        timeResultArtifact.requestLayout()



    }
}

interface TimeResultBlockLayout : HoldsListeners<TimeResultBlockLayout.Listener> {
    val blockLayout: ViewGroup
    val blockBackgroundCover: View

    val timeUnit: TimeUnit
    var timeValue: Long
    var xPosRelativeToParent: Float
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
    @IdRes layoutBackgroundCover: Int,
    @ColorRes color: Int,
    @StringRes timeUnitString: Int,
    value: Long,
    private val listenersMgr: ListenersManager<TimeResultBlockLayout.Listener> = ListenersManager()
): TimeResultBlockLayout, HoldsListeners<TimeResultBlockLayout.Listener> by listenersMgr {

    override val blockLayout = containerLayout.findViewById<ViewGroup>(layout)
    override val blockBackgroundCover = containerLayout.findViewById<View>(layoutBackgroundCover)

    override var timeValue = 0L
        set(value) {
            setNumberTo(value)
            field = value
        }

    override var xPosRelativeToParent: Float
        get() = blockLayout.x - containerLayout.x
        set(value) {
            blockLayout.x = containerLayout.x + value
            blockBackgroundCover.x = containerLayout.x + value
        }

    override var isMaximizeSymbolVisible: Boolean
        get() = blockLayout.findViewById<ImageView>(R.id.timeResultBlock_maximizeIcon).visibility == View.INVISIBLE
        set(value) { blockLayout.findViewById<ImageView>(R.id.timeResultBlock_maximizeIcon).visibility = if (value) View.VISIBLE else View.INVISIBLE }

    private fun setNumberTo(value: Long) {
        val numberTextView = blockLayout.findViewById<TextView>(R.id.timeResultBlock_number)
        numberTextView.text = NumberImpl(value.toString()).toStringWithGroupingFormatting()
        numberTextView.requestLayout()
        numberTextView.invalidate()
    }

    init {
        blockLayout.findViewById<LinearLayout>(R.id.timeResultBlock_background).setBackgroundResource(color)
        blockLayout.findViewById<TextView>(R.id.timeResultBlock_timeUnit).text = stringFromRes(timeUnitString)
        isMaximizeSymbolVisible = false

        if (blockLayout.x != blockBackgroundCover.x || blockLayout.y != blockBackgroundCover.y) {
            //todo but maybe they weren't initialized yet so it will be all 0
            throw InternalError("layout and layoutBackgroundCover must be in the same starting position")
        }



        val gestureListener = object: GestureDetector.SimpleOnGestureListener() {

            override fun onDown(e: MotionEvent?): Boolean { //todo necessary??
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
        }
        val gestureDetector = GestureDetector(containerLayout.context, gestureListener)

        blockLayout.setOnTouchListener {
            _, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
        }

        this.timeValue = value
    }
}