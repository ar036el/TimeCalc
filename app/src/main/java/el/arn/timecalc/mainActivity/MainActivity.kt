package el.arn.timecalc.mainActivity

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import el.arn.timecalc.R
import el.arn.timecalc.appRoot
import el.arn.timecalc.calculation_engine.TimeExpressionConfig
import el.arn.timecalc.calculation_engine.TimeExpressionFactory
import el.arn.timecalc.calculation_engine.result.ResultBuilder
import el.arn.timecalc.calculation_engine.result.ResultBuilderImpl
import el.arn.timecalc.calculation_engine.result.TimeResult
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
import el.arn.timecalc.mainActivity.ui.TimeResultLayoutManager
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandler
import el.arn.timecalc.mainActivity.ui.swipeGestureHandler.SwipeGestureHandlerImpl
import el.arn.timecalc.rootUtils

class MainActivity : AppCompatActivity() {

    private lateinit var expressionEditText: CustomEditText
    private val expressionBuilder = appRoot.calculatorCoordinator.expressionBuilder
    private val expressionStringAdapter = appRoot.calculatorCoordinator.expressionToStringConverter
    private lateinit var timeResultLayoutManager: TimeResultLayoutManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        expressionEditText = findViewById(R.id.calculator_expressionDisplayEditText)
        expressionEditText.showSoftInputOnFocus = false //todo put this at class itself. test and see if not crash


        initCalculatorActionButtons()
        initAll.grantOneAccess()


        EditTextFontAutosizeMaker(
            expressionEditText,
            dimenFromResAsPx(R.dimen.expressionEditText_minTextSize),
            dimenFromResAsPx(R.dimen.expressionEditText_maxTextSize)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun onCalculatorSymbolButtonClick(view: View) {
        val selectedSymbol = Symbol.charOf(view.getTagAsChar())
        expressionBuilder.insertSymbolAt(selectedSymbol, getExpressionCurrentLocation())
    }

    private fun initCalculatorActionButtons() {

        //backspace button
        val backspaceButton: Button = findViewById(R.id.calculator_actionButton_backspace)
        backspaceButton.setOnLongClickListener {
            expressionBuilder.clearAll(); true
        }
        backspaceButton.setOnClickListener {
            expressionBuilder.backspaceSymbolFrom(getExpressionCurrentLocation())
        }

        val resultBuilder: ResultBuilder = ResultBuilderImpl(rootUtils.timeConverter, TimeExpressionFactory(TimeExpressionConfig(30f, 365f)))
        //equals button
        val equalsButton: Button = findViewById(R.id.calculator_actionButton_equals)
        equalsButton.setOnClickListener {
            val result = resultBuilder.solveAndGetResult(expressionBuilder.getExpression())
            if (result is TimeResult) {
                timeResultLayoutManager.consumeTimeResult(result)
            }
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
        return expressionStringAdapter.stringIndexToExpressionIndex(expressionEditText.selectionStart)
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

        timeResultLayoutManager = TimeResultLayoutManager(
            findViewById(R.id.timeResultLayout),
            null,
            rootUtils.configManager.getConfigForTimeResultLayoutManager(),
            1080f,
            70f,
            200f,
        )


        appRoot.calculatorCoordinator.setActivityComponents(
            this,
            expressionEditText,
            buttonsContainerTopPart,
            buttonsContainerBottomPart,
            swipeGestureHandler,
            timeResultLayoutManager
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