package el.arn.timecalc.mainActivity

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import el.arn.timecalc.R
import el.arn.timecalc.appRoot
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerBottomPart
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerBottomPartImpl
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerTopPart
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerTopPartImpl
import el.arn.timecalc.mainActivity.custom_views.CustomEditText
import el.arn.timecalc.mainActivity.ui.EditTextFontAutosizeMaker
import el.arn.timecalc.helpers.android.dimenFromResAsPx
import el.arn.timecalc.helpers.native_.LimitedAccessFunction
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.mainActivity.ui.ResultLayoutManager
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandler
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandlerImpl
import el.arn.timecalc.organize_later.SettingsActivity
import el.arn.timecalc.rootUtils

class MainActivity : AppCompatActivity() {

    private lateinit var expressionEditText: CustomEditText
    private lateinit var resultLayoutManager: ResultLayoutManager

    private val calculatorCoordinator by lazy { appRoot.calculatorCoordinator }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        expressionEditText = findViewById(R.id.calculator_expressionDisplayEditText)
        expressionEditText.showSoftInputOnFocus = false //todo put this at class itself. test and see if not crash

        initAll.grantOneAccess()


        EditTextFontAutosizeMaker(
            expressionEditText,
            dimenFromResAsPx(R.dimen.expressionEditText_minTextSize),
            dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize)
        )



    findViewById<ImageButton>(R.id.settingsButton).setOnClickListener {
        openSettingsActivity()
    }

}


private fun openSettingsActivity() {
        val settingsActivity = Intent(this, SettingsActivity::class.java)
        startActivity(settingsActivity)
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun onCalculatorSymbolButtonClick(view: View) {
        calculatorCoordinator.symbolButtonPressed(view as Button)
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {

        initAll.invokeIfHasAccess()
    }

    private val initAll = LimitedAccessFunction({
        val buttonsContainerTopPart: ButtonsContainerTopPart = ButtonsContainerTopPartImpl(this)
        val buttonsContainerBottomPart: ButtonsContainerBottomPart = ButtonsContainerBottomPartImpl(this)

        val swipeGestureHandler: SwipeGestureHandler = SwipeGestureHandlerImpl(
            this,
            findViewById(R.id.touchSurface),
            findViewById(R.id.calculatorButtonsDrawerlikeLayout),
            SwipeGestureHandler.Bound.Static,
            SwipeGestureHandler.Bound.Range(buttonsContainerTopPart.minHeight, buttonsContainerTopPart.maxHeight),
            PxPoint(0f, buttonsContainerTopPart.minHeight),
            false,
            1.1f
        )

        resultLayoutManager = ResultLayoutManager(
            findViewById(R.id.resultLayout),
            null,
            rootUtils.configManager.getConfigForTimeResultLayoutManager(),
            1080f,
            150f,
            200f,
        )


        appRoot.calculatorCoordinator.setActivityComponents(
            this,
            expressionEditText,
            buttonsContainerTopPart,
            buttonsContainerBottomPart,
            swipeGestureHandler,
            resultLayoutManager
        )



    })




    private fun attachClickEventsToAllDrawerLayoutButtons() {
        //this is done in order to allow a non-consuming behaviour so the swipe-behaviour will be ok

//        val drawerLayout: FrameLayout = findViewById(R.id.calculatorButtonsDrawerlikeLayout)
//
//        val allButtonsYo = getAllButtonsInViewGroupDeeply(drawerLayout)
//
//        allButtonsYo.forEach {
////            it.setOnClickListener()
//        }

    }

    private fun getAllButtonsInViewGroupDeeply(viewGroup: ViewGroup): Set<Button> {
        val buttons = mutableSetOf<Button>()
        for (i in 0 until viewGroup.childCount) {
            val child: View = viewGroup.getChildAt(i)
            if (child is ViewGroup) {
                buttons.addAll(getAllButtonsInViewGroupDeeply(child))
            }
            else if (child is Button) {
                buttons.add(child)
            }
        }
        return buttons
    }

}