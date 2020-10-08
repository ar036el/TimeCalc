package com.arealapps.timecalc.calculatorActivity.ui.historyManager.database

import android.content.Context
import android.text.format.DateUtils
import com.arealapps.timecalc.rootUtils

interface HistoryDatabaseManager {
    fun addEntry(dateInMillis: Long, expression: String, result: String)
    fun getEntry(entryIndex: Int): Entry
    fun isEmpty(): Boolean
    fun isFirstUniqueFormattedDate(entryIndex: Int): Boolean
    fun clearAll()
    val lastEntryIndex: Int

    data class Entry(val dateInMillis: Long, val expression: String, val result: String) {
        fun getFormattedDate(): String = DateUtils.getRelativeTimeSpanString(dateInMillis).toString()
        fun getResultAsReadableString(): String {
            val toResult = rootUtils.resultToDatabaseStringConverter.stringToResult(result)
            return rootUtils.resultToReadableStringConverter.resultToString(toResult)
        }
    }

}

class HistoryDatabaseManagerImpl(context: Context) : HistoryDatabaseManager {

    override val lastEntryIndex get() = list.lastIndex

    private val sqliteDatabase: HistorySQLiteDatabase = HistorySQLiteDatabaseImpl(context)
    private val list: MutableList<HistoryDatabaseManager.Entry>


    override fun addEntry(dateInMillis: Long, expression: String, result: String) {
        list.add(0, HistoryDatabaseManager.Entry(dateInMillis, expression, result))
        sqliteDatabase.insertEntry(dateInMillis, expression, result)
    }

    override fun getEntry(entryIndex: Int) = list[entryIndex]

    override fun isEmpty() = list.isEmpty()

    override fun clearAll() {
        sqliteDatabase.clearAllEntries()
        list.clear()
    }

    override fun isFirstUniqueFormattedDate(entryIndex: Int): Boolean {
        if (entryIndex < 0 || entryIndex > list.lastIndex) { throw InternalError() }
        return (list.getOrNull(entryIndex-1)?.getFormattedDate() != list[entryIndex].getFormattedDate())
    }

    init {
        list = sqliteDatabase.retrieveAllEntries()
            .map { HistoryDatabaseManager.Entry(it.dateInMillis, it.expression, it.result) }
            .toMutableList()
    }

}
