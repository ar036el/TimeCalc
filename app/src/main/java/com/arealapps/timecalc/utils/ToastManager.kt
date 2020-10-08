/*******************************************************
 * Copyright (C) 2020-2021 ArealApps areal.apps@gmail.com
 *
 * This file and project cannot be copied and/or distributed without the explicit
 * permission of ArealApps. All Rights Reserved.
 *******************************************************/

package com.arealapps.timecalc.utils

import android.R
import android.content.Context
import android.view.Gravity
import android.widget.TextView
import android.widget.Toast
import java.lang.ref.WeakReference

interface ToastManager {
    fun showLong(text: String)
    fun showShort(text: String)
    fun cancelIfAny()
}

class ToastManagerImpl(private val applicationContext: Context) : ToastManager {
    private var currentToast: WeakReference<Toast>? = null

    override fun showLong(text: String) {
        makeToast(text, false)
    }

    override fun showShort(text: String) {
        makeToast(text, true)
    }

    override fun cancelIfAny() {
        currentToast?.get()?.cancel()
    }

    private fun makeToast(text: String, isShort: Boolean) {
        cancelIfAny()
        val toast = Toast.makeText(applicationContext, text, if (isShort) Toast.LENGTH_SHORT else Toast.LENGTH_LONG)
        val v = toast.view.findViewById<TextView>(R.id.message)
        if (v != null) v.gravity = Gravity.CENTER
        toast.show()
        currentToast = WeakReference(toast)
    }
}