package com.arealapps.timecalc

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.arealapps.timecalc.utils.externalIntentInvoker.GooglePlayStoreAppPageInvoker


class SettingsActivity : AppCompatActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)


        findViewById<View>(R.id.actionItem_rateUs).setOnClickListener {
            GooglePlayStoreAppPageInvoker(this).open()
        }
        findViewById<View>(R.id.actionItem_moreBlabla).setOnClickListener {
            //todo
        }

    }

}