package el.arn.timecalc.mainActivity.ui

import TimeBlock
import TimeBlockImpl
import android.animation.Animator
import android.animation.ValueAnimator
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import el.arn.timecalc.R
import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.helpers.android.*
import el.arn.timecalc.organize_later.DynamicFieldsDispatcher
import el.arn.timecalc.rootUtils
import kotlin.math.max
import kotlin.math.min

class TimeResultLayout(
    private val layout: ViewGroup,
    timeResult: TimeResult,
    private val config: TimeResultUIConfig,
    desiredWidth: Float,
    minHeight: Float,
    maxHeight: Float,
) {

    var desiredWidth = desiredWidth
        set(value) {
            field = value
            updateLayoutSize()
        }
    var minHeight = minHeight
        set(value) {
            field = value
            updateLayoutSize()
        }
    var maxHeight = maxHeight
        set(value) {
            field = value
            updateLayoutSize()
        }


    private val containerResizable: ViewGroup by lazy { layout.findViewById<ViewGroup>(R.id.timeResultLayout_containerResizable) }
    private val containerSource: ViewGroup by lazy { layout.findViewById<ViewGroup>(R.id.timeResultLayout_containerSource) }
    private var layoutSizeScale = 1f
    private var prevUnscaledWidth: Float? = null
    private var prevUnscaledHeight: Float? = null

    var areGesturedEnabled: Boolean = true

    private fun updateLayoutSize() {
        containerResizable.doWhenDynamicVariablesAreReady {
            containerSource.doWhenDynamicVariablesAreReady {

                //containerSource.width and containerSource.height are never affected by scaleX/scaleY changes. been tested! :#
                val unscaledWidth = containerSource.width.toFloat()
                val unscaledHeight = containerSource.height.toFloat()

                prevUnscaledWidth = unscaledWidth
                prevUnscaledHeight = unscaledHeight

                val unboundedScale = desiredWidth / unscaledWidth
                val unboundedHeight = unboundedScale * unscaledHeight
                val boundedHeight = min(max(unboundedScale * unscaledHeight, minHeight), maxHeight)
                val boundedScale = unboundedScale * (boundedHeight / unboundedHeight)


                if (layoutSizeScale == boundedScale) {
                    return@doWhenDynamicVariablesAreReady
                }
                layoutSizeScale = boundedScale

                containerSource.scaleX = layoutSizeScale
                containerSource.scaleY = layoutSizeScale
                containerResizable.widthByLayoutParams = (unscaledWidth*layoutSizeScale).toInt() + containerResizable.paddingX
                containerResizable.heightByLayoutParams = (unscaledHeight*layoutSizeScale).toInt() + containerResizable.paddingY
                containerSource.invalidate()
                containerSource.requestLayout()
            }
        }
    }



    private var timeResult: TimeResult = timeResult
        set(value) {
            field = value
            blocksAsList.forEach {
                setBlockInitialState(it)
            }
        }

    private val blocks: TimeVariable<TimeBlock>
    private val blocksAsList: List<TimeBlock>

    private val blocksExtensions: TimeVariable<DynamicFieldsDispatcher<TimeBlock>>
    private val TimeBlock.extension get() = blocksExtensions[this.timeUnit]
    private val ORIGINAL_NUMBER_PROP_KEY = "originalNumber"




        val TIMEBLOCK_VISIBILITY_THRESHOLD = 0.3

        val TimeBlock.isHidden get() = visibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD
        val TimeBlock.isCompletelyHidden get() = visibilityPercentage == 0f
        val TimeBlock.isAllegHidden get() = extension.get<Num>(ORIGINAL_NUMBER_PROP_KEY).isZero()
        val TimeBlock.isCollapsed get() = isHidden && !isAllegHidden

        fun List<TimeBlock>.getAllCollapsedIn(timeBlock: TimeBlock): List<TimeBlock>? {
            val timeBlock1 = timeBlock.timeUnit
            var firstVisibleBlockAfterThis = indexOfFirst {
                indexOf(it) > indexOf(timeBlock)
                        && !it.isHidden }
            val untilIndex = if (firstVisibleBlockAfterThis == -1) lastIndex+1 else firstVisibleBlockAfterThis

            return filter {
                indexOf(it) > indexOf(timeBlock)
                        && indexOf(it) < untilIndex
                        && it.isCollapsed }.ifEmpty { null }


        }


    val VISIBITITY_ANIMATION_DURATION = 200L

    private fun setBlocksVisibility(subject: TimeBlock, source: TimeBlock, visibilityPercentage: Float, treatSourceAsHidden: Boolean) {

        val lastVisibilityPercentage = subject.visibilityPercentage
        subject.visibilityPercentage = visibilityPercentage

        if (treatSourceAsHidden) {
             source.visibilityPercentage = 1f - visibilityPercentage
        }

        if (visibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD && lastVisibilityPercentage >= TIMEBLOCK_VISIBILITY_THRESHOLD) {
            updateBlockMaximizationState(source)
            Log.v("TimeResultUI", "${subject.timeUnit} was collapsed into ${source.timeUnit}")
        } else if (visibilityPercentage >= TIMEBLOCK_VISIBILITY_THRESHOLD && lastVisibilityPercentage < TIMEBLOCK_VISIBILITY_THRESHOLD){
            updateBlockMaximizationState(source)
            Log.v("TimeResultUI", "${subject.timeUnit} was revealed from ${source.timeUnit}")
        }
        if (visibilityPercentage == 0f) {
            updateBlockMaximizationState(subject)
        }
    }

    private var valueAnimator: ValueAnimator? = null

    private fun tryToCollapse(toCollapse: TimeBlock, animate: Boolean): Boolean {
        val source = blocksAsList.lastOrNull { blocksAsList.indexOf(it) < blocksAsList.indexOf(toCollapse) && !it.isCollapsed }
        if (toCollapse.isHidden || toCollapse.isCollapsed || source == null || valueAnimator?.isRunning == true) { return false }
        val treatSourceAsHidden = (source.isAllegHidden && (blocksAsList.getAllCollapsedIn(source).isNullOrEmpty()))

        if (animate) {
            startValueAnimation(1f, 0f) { setBlocksVisibility(toCollapse, source, it, treatSourceAsHidden) }
        } else {
            setBlocksVisibility(toCollapse, source, 0f, treatSourceAsHidden)
        }
        return true

    }


    private fun tryToReveal(source: TimeBlock, animate: Boolean): Boolean {
        val toReveal = blocksAsList.getAllCollapsedIn(source)?.last()
        if (source.isHidden || toReveal == null || !toReveal.isCollapsed || valueAnimator?.isRunning == true) { return false }
        val treatSourceAsHidden = (source.isAllegHidden && (blocksAsList.getAllCollapsedIn(source).orEmpty() - toReveal).isEmpty())

        if (animate) {
            startValueAnimation(0f, 1f) { setBlocksVisibility(toReveal, source, it, treatSourceAsHidden) }
        } else {
            setBlocksVisibility(toReveal, source, 1f, treatSourceAsHidden)
        }
        return true
    }

    private fun startValueAnimation(minValue: Float, maxValue: Float, setBlockVisibilityFun: (Float) -> Unit) {
        valueAnimator = ValueAnimator.ofFloat(minValue, maxValue)
        valueAnimator!!.apply {
            addUpdateListener { animation ->
                setBlockVisibilityFun(animatedValue as Float)
            }
            addListener(object: AnimatorListener {
                override fun onAnimationEnd(animation: Animator?) {
//                    updateContainerSize()
                }
            })
            duration = VISIBITITY_ANIMATION_DURATION
            interpolator = AccelerateDecelerateInterpolator()
            start()

        }
    }

    private fun updateBlockMaximizationState(timeBlock: TimeBlock) {
        blocksAsList.forEach {
        if (blocksAsList.getAllCollapsedIn(timeBlock).isNullOrEmpty()) {
            //set as normal
            timeBlock.number = timeBlock.extension[ORIGINAL_NUMBER_PROP_KEY]
            timeBlock.isMaximizedSymbolVisible = false
        } else {
            //set as maximized
            val allCollapsedInBlock = blocksAsList.getAllCollapsedIn(timeBlock)
            var number: Num = timeBlock.extension[ORIGINAL_NUMBER_PROP_KEY]
            allCollapsedInBlock?.forEach {
                number += rootUtils.timeConverter.convertTimeUnit(it.extension[ORIGINAL_NUMBER_PROP_KEY], it.timeUnit, timeBlock.timeUnit)
            }
            timeBlock.number = number
            timeBlock.isMaximizedSymbolVisible = true
        }
            }
    }


    private fun collapseInAnimation(block: TimeUnit) { //todo remove later
        val successful = tryToCollapse(blocks[block], true)
        if (!successful) {
            rootUtils.toastManager.showShort("collapse not successful")
        }
    }

    private fun revealInAnimation(fromBlock: TimeUnit) { //todo remove later
        val successful = tryToReveal(blocks[fromBlock], true)
        if (!successful) {
            rootUtils.toastManager.showShort("reveal not successful")
        }
    }

    private fun createTimeBlocks(): TimeVariable<TimeBlock> {
        return TimeVariable(
            TimeBlockImpl(
                layout,
                TimeUnit.Milli,
                R.id.timeResultBlock_millisecond,
                R.color.timeResultBackground_millisecond,
                R.string.calculator_timeUnit_millisecond_full,
                timeResult.time.units.millis
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Second,
                R.id.timeResultBlock_second,
                R.color.timeResultBackground_second,
                R.string.calculator_timeUnit_second_full,
                timeResult.time.units.seconds
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Minute,
                R.id.timeResultBlock_minute,
                R.color.timeResultBackground_minute,
                R.string.calculator_timeUnit_minute_full,
                timeResult.time.units.minutes
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Hour,
                R.id.timeResultBlock_hour,
                R.color.timeResultBackground_hour,
                R.string.calculator_timeUnit_hour_full,
                timeResult.time.units.hours
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Day,
                R.id.timeResultBlock_day,
                R.color.timeResultBackground_day,
                R.string.calculator_timeUnit_day_full,
                timeResult.time.units.days
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Week,
                R.id.timeResultBlock_week,
                R.color.timeResultBackground_week,
                R.string.calculator_timeUnit_week_full,
                timeResult.time.units.weeks
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Month,
                R.id.timeResultBlock_month,
                R.color.timeResultBackground_month,
                R.string.calculator_timeUnit_month_full,
                timeResult.time.units.months
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Year,
                R.id.timeResultBlock_year,
                R.color.timeResultBackground_year,
                R.string.calculator_timeUnit_year_full,
                timeResult.time.units.years
            )
        )
    }

    private val timeBlockListener = object: TimeBlock.Listener {
        override fun onBlockSingleClick(subject: TimeBlock) {
            if (areGesturedEnabled) {
                collapseInAnimation(subject.timeUnit)
            }
        }
        override fun onBlockDoubleClick(subject: TimeBlock) {
            if (areGesturedEnabled) {
                revealInAnimation(subject.timeUnit)
            }
        }
        override fun blockWidthHasChanged(subject: TimeBlock, newWidth: Int) {
            updateLayoutSize()
        }
    }

    private fun setBlockInitialState(block: TimeBlock) {
        block.number = timeResult.time.units[block.timeUnit]
        blocksExtensions.toList().forEach { it2 -> it2[ORIGINAL_NUMBER_PROP_KEY] = it2.obj.number }

        if (config.hideEmptyTimeValues && block.isAllegHidden) {
            block.visibilityPercentage = 0f
        }
        else if (config.autoCollapseTimeValues[block.timeUnit]) {
            val successful = tryToCollapse(block, false)
            if (!successful) {
                Log.w("TimeResultUI", "cannot auto collapse ${block.timeUnit}")
            }
        }
    }



    init {
        if (minHeight > maxHeight) { throw InternalError("minWidth[$minHeight] > maxWidth[$maxHeight]")}

        blocks = createTimeBlocks()
        blocksAsList = blocks.toList()
        blocksExtensions = TimeVariable { DynamicFieldsDispatcher(blocks[it]) }


        blocksAsList.forEach{
            it.addListener(timeBlockListener)
            setBlockInitialState(it)
        }

        updateLayoutSize()
    }
}

class TimeResultUIConfig(
    val hideEmptyTimeValues: Boolean,
    val autoCollapseTimeValues: TimeVariable<Boolean> //todo auto??
)