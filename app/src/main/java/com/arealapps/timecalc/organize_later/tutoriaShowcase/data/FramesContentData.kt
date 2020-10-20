package com.arealapps.timecalc.organize_later.tutoriaShowcase.data

import android.view.View
import androidx.annotation.IdRes
import androidx.annotation.StringRes
import com.arealapps.timecalc.R
import com.arealapps.timecalc.activities.calculatorActivity.CalculatorActivity

class FramesContentData(activity: CalculatorActivity) {



    var timeBlockTarget: View? = null
    var centerPointTarget: View? = null

    fun getFrameData(frame: Script.Frames): FrameData {
        return framesWithFrameData.getValue(frame)
    }

    private val delayInStart = 1000
    private val delayBetweenFrames = 100
    private val delayAfterEvent = 500
    @StringRes private val buttonNext = R.string.showcaseTutorial_button_next
    @StringRes private val buttonTry = R.string.showcaseTutorial_button_try
    @StringRes private val buttonFinish = R.string.showcaseTutorial_button_finish
    
    private val framesWithFrameData = mapOf(
        Script.Frames._0 to FrameData(delayInStart, { centerPointTarget ?: error("forgot to assign centerPointTarget") }, R.string.showcaseTutorial_frame0_title, R.string.showcaseTutorial_frame0_content, buttonNext),
        Script.Frames._1a to FrameData(delayBetweenFrames, { activity.findViewById(R.id.regularButtonsContainer) }, null, R.string.showcaseTutorial_frame1a, buttonNext),
        Script.Frames._1b to FrameData(delayBetweenFrames, { activity.findViewById(R.id.timeUnitButtonsTopRowContainer) }, null, R.string.showcaseTutorial_frame1b, buttonNext),
        Script.Frames._1c to FrameData(delayBetweenFrames, { activity.findViewById(R.id.calculator_separatorIcon) }, null, R.string.showcaseTutorial_frame1c, buttonNext),
        Script.Frames._2a1 to FrameData(delayBetweenFrames, { activity.findViewById(R.id.regularButtonsContainer) }, null, R.string.showcaseTutorial_frame2a1, buttonTry),
        Script.Frames._2a2 to FrameData(delayAfterEvent, { activity.findViewById(R.id.resultLayout_containerForResize) }, null, R.string.showcaseTutorial_frame2a2, buttonNext),
        Script.Frames._2b1 to FrameData(delayBetweenFrames, { activity.findViewById(R.id.regularButtonsContainer) }, null, R.string.showcaseTutorial_frame2b1, buttonTry),
        Script.Frames._2b2 to FrameData(delayAfterEvent, { activity.findViewById(R.id.resultLayout_containerForResize) }, null, R.string.showcaseTutorial_frame2b2, buttonNext),
        Script.Frames._2c1 to FrameData(delayBetweenFrames, { activity.findViewById(R.id.regularButtonsContainer) }, null, R.string.showcaseTutorial_frame2c1, buttonTry),
        Script.Frames._2c2 to FrameData(delayAfterEvent, { activity.findViewById(R.id.resultLayout_containerForResize) }, null, R.string.showcaseTutorial_frame2c2, buttonNext),
        Script.Frames._3a to FrameData(delayAfterEvent,  { timeBlockTarget ?: error("forgot to assign timeBlockTarget") }, null, R.string.showcaseTutorial_frame3a, buttonNext),
        Script.Frames._3b1 to FrameData(delayBetweenFrames, { timeBlockTarget ?: error("forgot to assign timeBlockTarget") }, null, R.string.showcaseTutorial_frame3b1, buttonTry),
        Script.Frames._3b2 to FrameData(delayAfterEvent, { timeBlockTarget ?: error("forgot to assign timeBlockTarget") }, null, R.string.showcaseTutorial_frame3b2, buttonNext),
        Script.Frames._3c1 to FrameData(delayBetweenFrames, { timeBlockTarget ?: error("forgot to assign timeBlockTarget") }, null, R.string.showcaseTutorial_frame3c1, buttonTry),
        Script.Frames._3c2 to FrameData(delayBetweenFrames, { timeBlockTarget ?: error("forgot to assign timeBlockTarget") }, null, R.string.showcaseTutorial_frame3c2, buttonNext),
        Script.Frames._4a to FrameData(delayBetweenFrames, { activity.findViewById(R.id.calculator_showHistoryButton) }, R.string.showcaseTutorial_frame4a_title, R.string.showcaseTutorial_frame4a_content, buttonNext),
        Script.Frames._4b to FrameData(delayBetweenFrames, { activity.findViewById(R.id.calculator_settingsButton) }, R.string.showcaseTutorial_frame4b_title, R.string.showcaseTutorial_frame4b_content, buttonNext),
        Script.Frames._5 to FrameData(delayBetweenFrames, { centerPointTarget ?: error("forgot to assign centerPointTarget") }, R.string.showcaseTutorial_frame5_title, R.string.showcaseTutorial_frame5_content, buttonFinish)
    )

    data class FrameData(
        val delayInMillis: Int,
        val target: () -> View,
        @StringRes val titleText: Int?,
        @StringRes val contentText: Int,
        @StringRes val button: Int,
    )

}