package el.arn.timecalc

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.AttributeSet
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import el.arn.timecalc.calculator_core.calculation_engine.*

class MainActivity : AppCompatActivity() {

    private lateinit var editText: EditText
    private val expression = appRoot.expression
    private val expressionStringAdapter = appRoot.expressionStringAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        editText = findViewById(R.id.calculator_expressionDisplayEditText)
        editText.showSoftInputOnFocus = false
        expression.addListener(expressionListener)


        val backspaceButton: Button = findViewById(R.id.backspace)
        backspaceButton.setOnLongClickListener { expression.clearAll(); true}
        backspaceButton.setOnClickListener { expression.backspaceSymbolFrom(
            expressionStringAdapter.stringIndexToExpressionIndex(editText.selectionStart)) }
    }

    override fun onDestroy() {
        super.onDestroy()
        expression.removeListener(expressionListener)
    }

    fun onCalculatorSymbolButtonClick(view: View) {
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

    private val expressionListener = object: Expression.Listener {
        override fun expressionWasCleared() {
            editText.setText(expressionStringAdapter.expressionAsString(), TextView.BufferType.EDITABLE)
        }

        override fun exprTokenWasAddedAt(token: ExpressionToken, index: Int) {
            editText.setText(expressionStringAdapter.expressionAsString(), TextView.BufferType.EDITABLE)
            val a= expressionStringAdapter.expressionIndexToStringIndex(index+1)
            editText.setSelection(a)
        }

        override fun exprTokenWasReplacedAt(token: ExpressionToken, replaced: ExpressionToken, index: Int) {
            editText.setText(expressionStringAdapter.expressionAsString(), TextView.BufferType.EDITABLE)
            editText.setSelection(expressionStringAdapter.expressionIndexToStringIndex(index+1))
        }

        override fun exprTokenWasRemovedAt(token: ExpressionToken, index: Int) {
            editText.setText(expressionStringAdapter.expressionAsString(), TextView.BufferType.EDITABLE)
            editText.setSelection(expressionStringAdapter.expressionIndexToStringIndex(index))
        }
    }
}


class CustomEditText : androidx.appcompat.widget.AppCompatEditText {
    constructor(context: Context) : super(context)
    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context, attrs: AttributeSet?, defStyle: Int) : super(context, attrs, defStyle)

    override fun onSelectionChanged(selStart: Int, selEnd: Int) {
        val selStart = appRoot.expressionStringAdapter.expressionIndexToStringIndex(
            appRoot.expressionStringAdapter.stringIndexToExpressionIndex(selStart))
        val selEnd = appRoot.expressionStringAdapter.expressionIndexToStringIndex(
            appRoot.expressionStringAdapter.stringIndexToExpressionIndex(selEnd))
        setSelection(selStart, selEnd)
    }
}
