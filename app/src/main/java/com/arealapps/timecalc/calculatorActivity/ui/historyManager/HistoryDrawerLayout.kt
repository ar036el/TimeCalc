package com.arealapps.timecalc.calculatorActivity.ui.historyManager

import android.view.View
import android.widget.ImageButton
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.arealapps.timecalc.R
import com.arealapps.timecalc.calculation_engine.expression.Expression
import com.arealapps.timecalc.calculation_engine.result.Result
import com.arealapps.timecalc.calculatorActivity.CalculatorActivity
import com.arealapps.timecalc.calculatorActivity.ui.historyManager.database.HistoryDatabaseManager
import com.arealapps.timecalc.calculatorActivity.ui.historyManager.database.HistoryDatabaseManagerImpl
import com.arealapps.timecalc.helpers.android.stringFromRes
import com.arealapps.timecalc.helpers.listeners_engine.HoldsListeners
import com.arealapps.timecalc.helpers.listeners_engine.ListenersManager
import com.arealapps.timecalc.calculatorActivity.ui.historyManager.recyclerView.RecyclerViewAdapter
import com.arealapps.timecalc.rootUtils

interface HistoryDrawerLayout : HoldsListeners<HistoryDrawerLayout.Listener> {
    fun openDrawer()
    fun closeDrawer()
    fun saveItem(expression: Expression, result: Result)

    interface Listener {
        fun displayItemInCalculator(expressionAsString: String, resultAsString: String)
        fun copyExpression(expressionAsString: String)
        fun copyResult(resultAsString: String)
    }
}

class HistoryDrawerLayoutImpl(
    private val activity: CalculatorActivity,
    private val listenersMgr: ListenersManager<HistoryDrawerLayout.Listener> = ListenersManager()
) : HistoryDrawerLayout, HoldsListeners<HistoryDrawerLayout.Listener> by listenersMgr {

    private val recyclerView: RecyclerView = activity.findViewById(R.id.historyDrawer_recyclerView)
    private val drawerLayout: DrawerLayout = activity.findViewById(R.id.calcActivity_drawerLayout)
    private val clearHistoryButton: ImageButton = activity.findViewById(R.id.historyDrawer_clearHistoryButton)

    private val historyDatabaseManager = HistoryDatabaseManagerImpl(activity)
    private val expressionToStringConverter = rootUtils.expressionToStringConverter
    private val resultToStringConverter = rootUtils.resultToDatabaseStringConverter


    override fun openDrawer() {
        updateRecyclerView()
        drawerLayout.openDrawer(GravityCompat.START)
    }

    override fun closeDrawer() {
        drawerLayout.closeDrawer(GravityCompat.START)
    }


    override fun saveItem(expression: Expression, result: Result) {
        historyDatabaseManager.addEntry(
            System.currentTimeMillis(),
            expressionToStringConverter.expressionToString(expression),
            resultToStringConverter.resultToString(result)
        )
    }

    private fun updateRecyclerView() {
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private val historyRecyclerViewAdapterListener = object : RecyclerViewAdapter.Listener {
        override fun triggeredAnAction(
            entry: HistoryDatabaseManager.Entry,
            action: RecyclerViewAdapter.Listener.ActionTypes,
        ) {
            when (action) {
                RecyclerViewAdapter.Listener.ActionTypes.CopyExpression -> listenersMgr.notifyAll { it.copyExpression(entry.expression) }
                RecyclerViewAdapter.Listener.ActionTypes.CopyResult -> listenersMgr.notifyAll { it.copyResult(entry.getResultAsReadableString()) }
                RecyclerViewAdapter.Listener.ActionTypes.ShowInDisplay ->  listenersMgr.notifyAll { it.displayItemInCalculator(entry.expression, entry.result) }
            }
        }
    }

    private fun openClearHistoryDialog() {
        if (historyDatabaseManager.isEmpty()) {
            return
        }
        AlertDialog.Builder(activity)
            .setMessage(stringFromRes(R.string.clearHistoryDialogMessage))
            .setPositiveButton(stringFromRes(android.R.string.ok)) { _, _ ->  clearHistory() }
            .setNegativeButton(stringFromRes(android.R.string.cancel), null)
            .show()
    }

    private fun clearHistory() {
        historyDatabaseManager.clearAll()
        recyclerView.adapter?.notifyDataSetChanged()
    }

    private fun initRecyclerView() {
        val viewManager = LinearLayoutManager(activity)
        val viewAdapter = RecyclerViewAdapter(historyDatabaseManager)
        recyclerView.apply {
            setHasFixedSize(true)
            layoutManager = viewManager
            adapter = viewAdapter
        }
        viewAdapter.addListener(historyRecyclerViewAdapterListener)
    }


    private fun initDrawerLayoutLockMode() {
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerOpened(drawerView: View) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED)
            }
            override fun onDrawerClosed(drawerView: View) {
                drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
            }
        })
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED)
    }

    init {
        initRecyclerView()
        clearHistoryButton.setOnClickListener {
            openClearHistoryDialog()
        }
        initDrawerLayoutLockMode()
    }

}