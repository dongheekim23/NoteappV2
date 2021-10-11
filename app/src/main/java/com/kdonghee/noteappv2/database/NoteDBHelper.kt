package com.kdonghee.noteappv2.database

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteOpenHelper
import android.database.sqlite.SQLiteDatabase
import android.provider.BaseColumns
import android.util.Log
import com.kdonghee.noteappv2.item.ItemUtils
import com.kdonghee.noteappv2.item.NoteItem

const val DATABASE_NAME = "notelist.db"
const val DATABASE_VERSION = 2

const val SECONDARY_TABLE_NAME = "noteList2"

interface TableNameChangeListener {

    fun onTableNameChanged(newName: String)
}

class NoteDBHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    override fun onCreate(db: SQLiteDatabase) {
        val SQL_CREATE_NOTE_LIST_TABLE = "CREATE TABLE " +
                NoteEntry.TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                NoteEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
        db.execSQL(SQL_CREATE_NOTE_LIST_TABLE)

        val SQL_CREATE_NOTE_LIST_TABLE2 = "CREATE TABLE " +
                SECONDARY_TABLE_NAME + " (" +
                BaseColumns._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                NoteEntry.COLUMN_NAME + " TEXT NOT NULL, " +
                NoteEntry.COLUMN_AMOUNT + " INTEGER NOT NULL, " +
                NoteEntry.COLUMN_TIMESTAMP + " TIMESTAMP DEFAULT CURRENT_TIMESTAMP" +
                ");"
        db.execSQL(SQL_CREATE_NOTE_LIST_TABLE2)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS " + NoteEntry.TABLE_NAME)
        db.execSQL("DROP TABLE IF EXISTS $SECONDARY_TABLE_NAME")
        onCreate(db)
    }

    @Synchronized
    fun addRandomItem(): NoteItem {
        val db = writableDatabase

        val newItemName = ItemUtils.getRandomItemName()
        val newItemAmount = ItemUtils.getRandomAmount()
        val contentValues = ContentValues()
        contentValues.apply {
            put(NoteEntry.COLUMN_NAME, newItemName)
            put(NoteEntry.COLUMN_AMOUNT, newItemAmount)
        }
        val rowId = db.insert(DBTableUtils.currentTableName, null, contentValues)
        db.close()

        Log.i("dh5031", "${Thread.currentThread().name} - addRandomItem()")

        return NoteItem(rowId, newItemName, newItemAmount)
    }

    @Synchronized
    fun addItem(name: String, amount: Int): NoteItem {
        val db = writableDatabase
        val contentValues = ContentValues().apply {
            put(NoteEntry.COLUMN_NAME, name)
            put(NoteEntry.COLUMN_AMOUNT, amount)
        }
        val rowId = db.insert(DBTableUtils.currentTableName, null, contentValues)
        db.close()

        Log.i("dh5031", "${Thread.currentThread().name} - addItem()")

        return NoteItem(rowId, name, amount)
    }

    @Synchronized
    fun removeItem(rowId: Long) {
        val db = writableDatabase

        db.delete(DBTableUtils.currentTableName, "${BaseColumns._ID} = $rowId", null)
        db.close()

        Log.i("dh5031", "${Thread.currentThread().name} - removeItem()")
    }

    @Synchronized
    fun clearAllItems() {
        val db = writableDatabase

        db.delete(DBTableUtils.currentTableName, null, null)
        db.close()

        Log.i("dh5031", "${Thread.currentThread().name} - clearAllItems()")
    }

    @Synchronized
    fun getAllItems(db: SQLiteDatabase): Cursor {
        Log.i("dh5031", "${Thread.currentThread().name} - getAllItems()")

        return db.rawQuery("SELECT * FROM ${DBTableUtils.currentTableName}", null)
    }
}
