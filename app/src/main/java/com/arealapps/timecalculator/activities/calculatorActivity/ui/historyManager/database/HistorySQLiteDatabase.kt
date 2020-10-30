package com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Companion.DATABASE_NAME
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Companion.FIELD_DATE
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Companion.FIELD_EXPRESSION
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Companion.FIELD_ID
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Companion.FIELD_RESULT
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Companion.TABLE_NAME
import com.arealapps.timecalculator.activities.calculatorActivity.ui.historyManager.database.HistorySQLiteDatabase.Entry

interface HistorySQLiteDatabase {
    fun insertEntry(dateInMillis: Long, expression: String, result: String)
    fun retrieveAllEntries(): List<Entry>
    fun clearAllEntries()

    data class Entry(val dateInMillis: Long, val expression: String, val result: String)

    companion object {
        const val DATABASE_NAME = "MY DATABASE"
        const val TABLE_NAME = "History"
        const val FIELD_ID = "ID"
        const val FIELD_DATE = "date"
        const val FIELD_EXPRESSION = "expression"
        const val FIELD_RESULT = "result"
    }
}

class HistorySQLiteDatabaseImpl(var context: Context) : HistorySQLiteDatabase {


    private val sqLiteOpenHelper = object : SQLiteOpenHelper(context, DATABASE_NAME, null, 1) {
        override fun onCreate(db: SQLiteDatabase?) {
            val createTable = "CREATE TABLE $TABLE_NAME ($FIELD_ID INTEGER PRIMARY KEY AUTOINCREMENT,$FIELD_DATE INTEGER, $FIELD_EXPRESSION TEXT,$FIELD_RESULT TEXT)"
            db?.execSQL(createTable)
        }
        override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
            //onCreate(db); todo wtf?
        }
    }



    override fun insertEntry(dateInMillis: Long, expression: String, result: String) {
        val database = sqLiteOpenHelper.writableDatabase
        val contentValues = ContentValues()
        contentValues.put(FIELD_DATE, dateInMillis)
        contentValues.put(FIELD_EXPRESSION, expression)
        contentValues.put(FIELD_RESULT, result)
        val response = database.insertOrThrow(TABLE_NAME, null, contentValues)

//        if (response != 0L) {
//            Log.e("HistorySqlDatabase","response is 0L!!")
//        }
//        return Entry(dateInMillis, expression, result)
    }

    override fun retrieveAllEntries(): List<Entry> {
        val list = mutableListOf<Entry>()
        val db = sqLiteOpenHelper.readableDatabase
        val query = "Select * from $TABLE_NAME"
        val result = db.rawQuery(query, null)
        if (result.moveToFirst()) {
            do {
                val entry = Entry(
                    result.getLong(result.getColumnIndex(FIELD_DATE)),
                    result.getString(result.getColumnIndex(FIELD_EXPRESSION)),
                    result.getString(result.getColumnIndex(FIELD_RESULT))
                )
                list.add(entry)
            }
            while (result.moveToNext())
        }
        result.close()
        return list.reversed()
    }

    override fun clearAllEntries() {
        val db: SQLiteDatabase = sqLiteOpenHelper.writableDatabase
        db.delete(TABLE_NAME, null, null)
    }
}