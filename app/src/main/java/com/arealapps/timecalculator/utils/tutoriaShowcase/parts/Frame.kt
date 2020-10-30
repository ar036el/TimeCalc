package com.arealapps.timecalculator.utils.tutoriaShowcase.parts

import android.app.Activity
import androidx.core.content.ContextCompat
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.helpers.native_.errorIf
import com.arealapps.timecalculator.utils.tutoriaShowcase.data.FramesContentData
import uk.co.deanwild.materialshowcaseview.IShowcaseListener
import uk.co.deanwild.materialshowcaseview.MaterialShowcaseView


interface Frame {
    fun show()
    fun dismiss()

    val state: States
    val isRunning: Boolean
    enum class States { Ready, Running, Finished}
}

class FrameImpl(
    private val data: FramesContentData.FrameData,
    private val activity: Activity,
    private val doOnDismiss: () -> Unit,
) : Frame {

    override var state = Frame.States.Ready
    override val isRunning: Boolean
        get() = state == Frame.States.Running

    private var materialShowcaseView: MaterialShowcaseView? = null
    private val target = data.target.invoke()


    override fun show() {
        errorIf { state != Frame.States.Ready }

        state = Frame.States.Running
        materialShowcaseView = MaterialShowcaseView.Builder(activity)
            .setTarget(target)
            .setDismissText(data.button)
            .apply { data.titleText?.let { setTitleText(it) } }
//            .setGravity(Gravity.CENTER_VERTICAL) todo??
            .setMaskColour(ContextCompat.getColor(activity, R.color.tutorialShowcase_maskColor))
            .setContentText(data.contentText)
            .setDelay(data.delayInMillis)
            .show()

        materialShowcaseView!!.addShowcaseListener(object : IShowcaseListener {
                override fun onShowcaseDisplayed(showcaseView: MaterialShowcaseView?) {}
                override fun onShowcaseDismissed(showcaseView: MaterialShowcaseView?) {
                    state = Frame.States.Finished
                    doOnDismiss.invoke()
                }
            })
    }

    override fun dismiss() {
        if (!isRunning) return
        materialShowcaseView!!.hide() //todo will it work???
    }


}