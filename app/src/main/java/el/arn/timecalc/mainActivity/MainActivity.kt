package el.arn.timecalc.mainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import el.arn.timecalc.R
import el.arn.timecalc.appRoot
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerBottomPart
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerBottomPartImpl
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerTopPart
import el.arn.timecalc.mainActivity.ui.calculatorButtonsLayouts.ButtonsContainerTopPartImpl
import el.arn.timecalc.calculation_engine.symbol.Symbol
import el.arn.timecalc.mainActivity.custom_views.CustomEditText
import el.arn.timecalc.mainActivity.ui.EditTextFontAutosizeMaker
import el.arn.timecalc.helpers.android.dimenFromResAsPx
import el.arn.timecalc.helpers.native_.LimitedAccessFunction
import el.arn.timecalc.helpers.native_.PxPoint
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandler
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandlerImpl

class MainActivity : AppCompatActivity() {

    private lateinit var editText: CustomEditText
    private val expression = appRoot.calculatorCoordinator.expressionBuilder
    private val expressionStringAdapter = appRoot.calculatorCoordinator.expressionToStringConverter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.calculator_expressionDisplayEditText)
        editText.showSoftInputOnFocus = false //todo put this at class itself. test and see if not crash


        initCalculatorActionButtons()
        initAll.grantOneAccess()


        EditTextFontAutosizeMaker(
            editText,
            dimenFromResAsPx(R.dimen.expressionEditText_minTextSize),
            dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun onCalculatorSymbolButtonClick(view: View) {
        val selectedSymbol = Symbol.charOf(view.getTagAsChar())
        expression.insertSymbolAt(selectedSymbol, getExpressionCurrentLocation())
    }

    private fun initCalculatorActionButtons() {

        //backspace button
        val backspaceButton: Button = findViewById(R.id.calculator_actionButton_backspace)
        backspaceButton.setOnLongClickListener {
            expression.clearAll(); true
        }
        backspaceButton.setOnClickListener {
            expression.backspaceSymbolFrom(getExpressionCurrentLocation())
        }

        //equals button
        val equalsButton: Button = findViewById(R.id.calculator_actionButton_equals)
        equalsButton.setOnClickListener {
            println("ahahahahahha")
            true
        }
    }


    private fun View.getTagAsChar(): Char {
        val asString = tag.toString()
        if (asString.length != 1) {
            throw InternalError()
        }
        return asString[0]
    }

    private fun getExpressionCurrentLocation(): Int {
        return expressionStringAdapter.stringIndexToExpressionIndex(editText.selectionStart)
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

        appRoot.calculatorCoordinator.setActivityComponents(this, editText, buttonsContainerTopPart, buttonsContainerBottomPart, swipeGestureHandler)

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