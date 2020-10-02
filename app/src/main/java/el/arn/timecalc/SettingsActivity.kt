package el.arn.timecalc

import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import el.arn.timecalc.R
import el.arn.timecalc.utils.externalIntentInvoker.GooglePlayStoreAppPageInvoker


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