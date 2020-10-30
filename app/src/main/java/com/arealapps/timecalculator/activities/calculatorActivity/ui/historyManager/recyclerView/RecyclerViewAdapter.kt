package com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.recyclerView

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.arealapps.timecalculator.R
import com.arealapps.timecalculator.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalculator.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistoryDatabaseManager

class RecyclerViewAdapter(
    private val databaseManager: HistoryDatabaseManager,
    private val listenersMgr: ListenersManager<Listener> = ListenersManager()
) : RecyclerView.Adapter<Item>(), HoldsListeners<RecyclerViewAdapter.Listener> by listenersMgr {

    interface Listener {
        fun triggeredAnAction(entry: HistoryDatabaseManager.Entry, action: ActionTypes)
        enum class ActionTypes { CopyExpression, CopyResult, ShowInDisplay }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): Item {
        val itemLayout = LayoutInflater.from(parent.context)
            .inflate(R.layout.artifact_history_item, parent, false) as ViewGroup
        return Item(itemLayout)
    }

    override fun onBindViewHolder(item: Item, position: Int) {
        if (databaseManager.isEmpty()) {
            item.setLayoutAsEmptyPlaceholder()
        } else {
            val entry = databaseManager.getEntry(position)
            val date = if (databaseManager.isFirstUniqueFormattedDate(position)) entry.getFormattedDate() else null
            item.setLayoutAsNormal(date, entry.expression, entry.getResultAsReadableString(),
                { listenersMgr.notifyAll { it.triggeredAnAction(entry, Listener.ActionTypes.CopyResult) } },
                { listenersMgr.notifyAll { it.triggeredAnAction(entry, Listener.ActionTypes.CopyExpression) } },
                { listenersMgr.notifyAll { it.triggeredAnAction(entry, Listener.ActionTypes.ShowInDisplay) } })
        }
    }

    override fun getItemCount(): Int {
        return if (databaseManager.isEmpty()) {
            1
        } else {
            databaseManager.lastEntryIndex + 1
        }
    }
}