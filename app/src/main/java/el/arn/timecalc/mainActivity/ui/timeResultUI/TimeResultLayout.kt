package el.arn.timecalc.mainActivity.ui

import TimeBlock
import TimeBlockImpl
import android.animation.ValueAnimator
import android.util.Log
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import el.arn.timecalc.R
import el.arn.timecalc.calculation_engine.atoms.Num
import el.arn.timecalc.calculation_engine.atoms.TimeVariable
import el.arn.timecalc.calculation_engine.result.TimeResult
import el.arn.timecalc.calculation_engine.symbol.TimeUnit
import el.arn.timecalc.helpers.android.doWhenDynamicVariablesAreReady
import el.arn.timecalc.helpers.android.heightByLayoutParams
import el.arn.timecalc.helpers.android.widthByLayoutParams
import el.arn.timecalc.organize_later.DynamicFieldsDispatcher
import el.arn.timecalc.rootUtils

class TimeResultLayout(
    private val layout: ViewGroup,
    timeResult: TimeResult,
    private val config: TimeResultUIConfig
) {

    private val originalWidth: Int by lazy { layout.width }
    private val originalHeight: Int by lazy { container.height }

    private val container = layout.findViewById<ViewGroup>(R.id.timeResult_container)

//    var customWidth: Float? = null
//        set(width) {
//            field = width
//            layout.doWhenDynamicVariablesAreReady {
//                val scaleFactor =  if (width != null) width / originalWidth else 1f
////                setLayoutSize(scaleFactor)
//            }
//        }
//    var customHeight: Float? = null
//        set(height) {
//            field = height
//            container.doWhenDynamicVariablesAreReady {
//                val scaleFactor =  if (height != null) height / originalHeight else 1f
//
//                container.pivotX = (originalWidth/2).toFloat()
//                container.pivotY = (originalHeight/2).toFloat()
//                container.scaleY = scaleFactor
//                container.scaleX = scaleFactor
//                layout.heightByLayoutParams = (layout.height*scaleFactor).toInt()
//                layout.widthByLayoutParams = (layout.width*scaleFactor).toInt()
//                val a= 3
//            }
//
//        }


    var customHeight: Float? = null
        set(height) {
            field = height
            container.doWhenDynamicVariablesAreReady {
                blocks.toList().forEach {
                    val a = (height ?: originalHeight.toFloat()) / originalHeight
                    it.sizePercentage = a
                }
            }

        }

//    private fun setLayoutSize(scaleFactor: Float) {
//        val oldScale = container.scaleX
//        val oldHeight = container.height
//        println("mamam a[${oldScale * originalHeight}] b[$oldHeight]")
//
//
//        container.scaleY = scaleFactor
//        container.scaleX = scaleFactor
//
//
//        layout.requestLayout()
//    }



    private var timeResult: TimeResult = timeResult
        set(value) {
            field = value
//            updateTimeResult()
        }
    init { this.timeResult = timeResult}

    private val fullTimeResult = rootUtils.timeConverter.millisToTimeVariable(timeResult.totalMillis)

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
                fullTimeResult.millis
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Second,
                R.id.timeResultBlock_second,
                R.color.timeResultBackground_second,
                R.string.calculator_timeUnit_second_full,
                fullTimeResult.seconds
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Minute,
                R.id.timeResultBlock_minute,
                R.color.timeResultBackground_minute,
                R.string.calculator_timeUnit_minute_full,
                fullTimeResult.minutes
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Hour,
                R.id.timeResultBlock_hour,
                R.color.timeResultBackground_hour,
                R.string.calculator_timeUnit_hour_full,
                fullTimeResult.hours
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Day,
                R.id.timeResultBlock_day,
                R.color.timeResultBackground_day,
                R.string.calculator_timeUnit_day_full,
                fullTimeResult.days
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Week,
                R.id.timeResultBlock_week,
                R.color.timeResultBackground_week,
                R.string.calculator_timeUnit_week_full,
                fullTimeResult.weeks
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Month,
                R.id.timeResultBlock_month,
                R.color.timeResultBackground_month,
                R.string.calculator_timeUnit_month_full,
                fullTimeResult.months
            )
            ,
            TimeBlockImpl(
                layout,
                TimeUnit.Year,
                R.id.timeResultBlock_year,
                R.color.timeResultBackground_year,
                R.string.calculator_timeUnit_year_full,
                fullTimeResult.years
            )
        )
    }

    private fun setBlocksClickListeners() {
        blocks.toList().forEach{
            it.addListener(object: TimeBlock.Listener {
                override fun onBlockSingleClick(subject: TimeBlock) {
                    collapseInAnimation(subject.timeUnit)
                }

                override fun onBlockDoubleClick(subject: TimeBlock) {
                    revealInAnimation(subject.timeUnit)
                }
            })
        }
    }


    init {


        blocks = createTimeBlocks()
        blocksAsList = blocks.toList()
        blocksExtensions = TimeVariable { DynamicFieldsDispatcher(blocks[it]) }
        setBlocksClickListeners()


        blocks.toList().forEach{
            it.number = fullTimeResult[it.timeUnit]
            blocksExtensions.toList().forEach { it2 -> it2[ORIGINAL_NUMBER_PROP_KEY] = it2.obj.number }

            if (config.hideEmptyTimeValues && it.isAllegHidden) {
                it.visibilityPercentage = 0f
            }
            else if (config.autoCollapseTimeValues[it.timeUnit]) {
                val successful = tryToCollapse(it, false)
                if (!successful) {
                    Log.e("TimeResultUI", "cannot auto collapse ${it.timeUnit}")
                }
            }
        }
    }
}

class TimeResultUIConfig(
    val hideEmptyTimeValues: Boolean,
    val autoCollapseTimeValues: TimeVariable<Boolean> //todo auto??
)