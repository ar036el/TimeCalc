package com.arealapps.timecalc.organize_later.tutoriaShowcase

import android.app.Activity
import android.content.Context
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.arealapps.timecalc.R
import com.arealapps.timecalc.activities.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.appRoot
import com.arealapps.timecalc.helpers.android.stringFromRes
import com.arealapps.timecalc.organize_later.errorIf
import com.arealapps.timecalc.organize_later.tutoriaShowcase.data.FramesContentData
import com.arealapps.timecalc.organize_later.tutoriaShowcase.data.Script
import com.arealapps.timecalc.organize_later.tutoriaShowcase.parts.Showcase
import com.arealapps.timecalc.organize_later.tutoriaShowcase.parts.ShowcaseImpl
import com.arealapps.timecalc.utils.preferences_managers.parts.PreferencesManagerImpl

interface TutorialShowcaseManager {
    val wasCompletedAtLeastOnce: Boolean
    val isRunning: Boolean
    fun doIfRunning(): TutorialShowcaseManager?
//    fun doIfNotCompletedAtLeastOnce(): TutorialShowcaseManager?

    fun start(activity: CalculatorActivity)
//    fun finish(treatAsCompleted: Boolean)
    fun notifyEvents(vararg events: Script.Events)

    //↓ for hooking the time block target. returns [null] if not running
    val framesContentData: FramesContentData?
}

class TutorialShowcaseManagerImpl: TutorialShowcaseManager {

    override val wasCompletedAtLeastOnce: Boolean
        get() = prefsManager.wasCompletedAtLeastOnce.value

    override val isRunning: Boolean
        get() = showcase?.isRunning == true

    override val framesContentData: FramesContentData?
        get() = if (showcase?.isRunning == true) showcase?.framesContentData else null


    private var centerPointView: View? = null
    private var showcase: Showcase? = null


    override fun doIfRunning(): TutorialShowcaseManager? {
        return if (isRunning) this else null
    }

    override fun start(activity: CalculatorActivity) {
        errorIf { isRunning }

        showcase = ShowcaseImpl(activity)
        createCenterPointView(activity)
        showcase!!.framesContentData.centerPointTarget = centerPointView!!
        showcase!!.addListener(showcaseListener)
        showcase!!.start()
    }

    override fun notifyEvents(vararg events: Script.Events) {
        if (isRunning && events.isNotEmpty()) {
            showcase!!.notifyEventsWereOccurred(*events)
        }
    }

    //------

    private fun doWhenShowcaseFinished() {
        prefsManager.wasCompletedAtLeastOnce.value = true
        destroyCenterPointView()
    }

    //this is used for showcase frames with no target, so the bubble will appear from the center and will have no mass
    private fun createCenterPointView(activity: Activity) {
        errorIf { centerPointView != null }

        val centerPointView = View(activity)

        val container = LinearLayout(activity)
        container.gravity = Gravity.CENTER

        container.addView(centerPointView, ViewGroup.LayoutParams(0, 0))
        activity.addContentView(container, ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT))
        this.centerPointView = centerPointView
    }

    private fun destroyCenterPointView() {
        errorIf { centerPointView == null }
        val container = centerPointView!!.parent as ViewGroup
        (container.parent as ViewGroup).removeView(container)
        centerPointView = null
    }

    //----

    private val prefsManager = object : PreferencesManagerImpl(
        appRoot.getSharedPreferences(stringFromRes(R.string.internal_prefs_tutorialShowcaseManager), Context.MODE_PRIVATE)
    ) {
        val wasCompletedAtLeastOnce = createBooleanPref("wasCompletedAtLeastOnce", false)
    }

    private val showcaseListener = object: Showcase.Listener {
        override fun stateWasChanged(subject: Showcase, newState: Showcase.States) {
            if (newState == Showcase.States.Finished) {
                doWhenShowcaseFinished()
            }
        }
    }

}