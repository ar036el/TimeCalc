package com.arealapps.timecalc.activities.calculatorActivity.ui.historyManager.recyclerView

import android.view.*
import android.widget.ImageButton
import android.widget.PopupMenu
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.arealapps.timecalc.R


class Item(val layout: ViewGroup) : RecyclerView.ViewHolder(layout) {
    fun setLayoutAsEmptyPlaceholder() {
        layout.findViewById<ViewGroup>(R.id.item_empty).visibility = View.VISIBLE
        layout.findViewById<ViewGroup>(R.id.item_normal).visibility = View.GONE
    }

    fun setLayoutAsNormal(
        date: String?,
        expression: String,
        result: String,
        onButtonClickCopyResult: () -> Unit,
        onButtonClickCopyExpression: () -> Unit,
        onButtonClickShowInDisplay: () -> Unit,
    ) {
        layout.findViewById<ViewGroup>(R.id.item_empty).visibility = View.GONE
        layout.findViewById<ViewGroup>(R.id.item_normal).visibility = View.VISIBLE


        val dateHeader: ViewGroup = layout.findViewById(R.id.dateHeader)
        if (date != null) {
            dateHeader.visibility =  View.VISIBLE
            dateHeader.findViewById<TextView>(R.id.dateTextView).text = date
        } else {
            dateHeader.visibility =  View.GONE
        }




        layout.findViewById<ImageButton>(R.id.historyItem_copyButton).setOnClickListener {
            showCopyToClipboardPopup(it, onButtonClickCopyResult, onButtonClickCopyExpression)
        }
        layout.findViewById<ImageButton>(R.id.historyItem_showInDisplayButton).setOnClickListener {
            onButtonClickShowInDisplay()
        }
        layout.findViewById<ViewGroup>(R.id.historyItem_entryContainer).setOnClickListener {
            onButtonClickShowInDisplay()
        }

        layout.findViewById<TextView>(R.id.historyItem_resultTextView).text = result
        layout.findViewById<TextView>(R.id.historyItem_expressionTextView).text = expression
    }

    private fun showCopyToClipboardPopup(from: View, copyResultClicked: () -> Unit, copyExpressionClicked: () -> Unit, ) {
        val popup = PopupMenu(layout.context, from)
        popup.inflate(R.menu.copy_to_clipboard_popup_menu)
        popup.setOnMenuItemClickListener{item: MenuItem? ->
            when (item!!.itemId) {
                R.id.copyToClipboard_result -> {
                    copyResultClicked()
                }
                R.id.copyToClipboard_expression -> {
                    copyExpressionClicked()
                }
                else -> throw NotImplementedError()
            }
            true
        }
        popup.show()
    }
}

