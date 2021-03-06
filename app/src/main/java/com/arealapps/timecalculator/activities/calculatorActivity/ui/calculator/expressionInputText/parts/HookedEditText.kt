package com.arealapps.timecalculator.activities.calculatorActivity.ui.calculator.expressionInputText.parts

import android.R
import android.content.Context
import android.util.AttributeSet
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager


class HookedEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context,
        attrs,
        defStyle)


    val listenersHolder: HoldsListeners<Listener> get() = listenersMgr!!
    private val listenersMgr: ListenersManager<Listener>? = ListenersManager() //this is nullable because crappy parent ctor calls to onSelectionChanged before its being initialized -.-

    override fun onSelectionChanged(selectionStart: Int, selectionEnd: Int) {
        super.onSelectionChanged(selectionStart, selectionEnd)
        listenersMgr?.notifyAll { it.onSelectionChanged(this, selectionStart, selectionEnd) }
    }

    override fun onTextContextMenuItem(id: Int): Boolean {
        if  (id == R.id.paste) {
            listenersMgr?.notifyAll { it.onTextPaste(text.toString()) }
        }
        return super.onTextContextMenuItem(id)
    }

    interface Listener {
        fun onSelectionChanged(subject: HookedEditText, selectionStart: Int, selectionEnd: Int)
        fun onTextPaste(textBeforePaste: String)
    }
}
