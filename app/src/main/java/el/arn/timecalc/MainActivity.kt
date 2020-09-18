package el.arn.timecalc

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.FrameLayout
import el.arn.timecalc.buttons_drawerlike_layout.ButtonsContainerBottomPart
import el.arn.timecalc.buttons_drawerlike_layout.ButtonsContainerBottomPartImpl
import el.arn.timecalc.buttons_drawerlike_layout.ButtonsContainerTopPart
import el.arn.timecalc.buttons_drawerlike_layout.ButtonsContainerTopPartImpl
import el.arn.timecalc.calculator_core.calculation_engine.*
import el.arn.timecalc.custom_views.CustomEditText
import el.arn.timecalc.custom_views.EditTextFontAutosizeMaker

class MainActivity : AppCompatActivity() {

    private lateinit var editText: CustomEditText
    private val expression = appRoot.calculatorCoordinator.expressionBuilder
    private val expressionStringAdapter = appRoot.calculatorCoordinator.expressionStringAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.calculator_expressionDisplayEditText)
        editText.showSoftInputOnFocus = false //todo put this at class itself. test and see if not crash

        val backspaceButton: Button = findViewById(R.id.backspace)
        backspaceButton.setOnLongClickListener { expression.clearAll(); true}
        backspaceButton.setOnClickListener { expression.backspaceSymbolFrom(
            expressionStringAdapter.stringIndexToExpressionIndex(editText.selectionStart)) }

        initAll.grantOneAccess()


        EditTextFontAutosizeMaker(
            editText,
            dimenFromRes(R.dimen.expressionEditText_minTextSize),
            dimenFromRes(R.dimen.expressionEditText_maxTextSize)
        )
    }

    override fun onDestroy() {
        super.onDestroy()
    }

    fun onCalculatorButtonClick(view: View) {
        val symbolAsChar = view.tag.toString()
        if (symbolAsChar.length != 1) {
            throw InternalError()
        }
        expression.insertSymbolAt(Symbol.charOf(symbolAsChar[0]),
            expressionStringAdapter.stringIndexToExpressionIndex(editText.selectionStart))
    }

    fun onCalculatorActionButtonClick(view: View) {
        when(view.tag) {
            "backspace" -> expression.backspaceSymbolFrom(
                expressionStringAdapter.stringIndexToExpressionIndex(editText.selectionStart))
            else -> TODO("not yet implemented")
        }
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

        val drawerLayout: FrameLayout = findViewById(R.id.calculatorButtonsDrawerlikeLayout)

        val allButtonsYo = getAllButtonsInViewGroupDeeply(drawerLayout)

        allButtonsYo.forEach {
//            it.setOnClickListener()
        }

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