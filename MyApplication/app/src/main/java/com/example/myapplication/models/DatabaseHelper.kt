package com.example.myapplication.models

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.content.ContentValues
import android.database.Cursor

class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        private const val DATABASE_NAME = "lostandfound.db"
        private const val DATABASE_VERSION = 2
        private const val TABLE_NAME = "items"
        private const val COLUMN_ID = "id"
        private const val COLUMN_TITLE = "title"
        private const val COLUMN_DESCRIPTION = "description"
        private const val COLUMN_CATEGORY = "category"
        private const val COLUMN_TYPE = "type"
        private const val COLUMN_LOCATION = "location"
        private const val COLUMN_CONTACT = "contact_info"
        private const val COLUMN_DATE = "date"
        private const val COLUMN_IMAGE = "image_path"
        private const val COLUMN_TIMESTAMP = "timestamp"
        private const val COLUMN_RESOLVED = "is_resolved"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTable = """
            CREATE TABLE $TABLE_NAME (
                $COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT,
                $COLUMN_TITLE TEXT NOT NULL,
                $COLUMN_DESCRIPTION TEXT NOT NULL,
                $COLUMN_CATEGORY TEXT NOT NULL,
                $COLUMN_TYPE TEXT NOT NULL,
                $COLUMN_LOCATION TEXT NOT NULL,
                $COLUMN_CONTACT TEXT NOT NULL,
                $COLUMN_DATE TEXT NOT NULL,
                $COLUMN_IMAGE TEXT NOT NULL,
                $COLUMN_TIMESTAMP INTEGER NOT NULL,
                $COLUMN_RESOLVED INTEGER DEFAULT 0
            )
        """.trimIndent()
        db.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun insertItem(item: Item): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COLUMN_TITLE, item.title)
            put(COLUMN_DESCRIPTION, item.description)
            put(COLUMN_CATEGORY, item.category)
            put(COLUMN_TYPE, item.type)
            put(COLUMN_LOCATION, item.location)
            put(COLUMN_CONTACT, item.contactInfo)
            put(COLUMN_DATE, item.date)
            put(COLUMN_IMAGE, item.imagePath)
            put(COLUMN_TIMESTAMP, item.timestamp)
            put(COLUMN_RESOLVED, if (item.isResolved) 1 else 0)
        }
        return db.insert(TABLE_NAME, null, values)
    }

    fun getAllItems(): List<Item> {
        val items = mutableListOf<Item>()
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, null, null, null, null, "$COLUMN_TIMESTAMP DESC")
        
        cursor.use { 
            while (it.moveToNext()) {
                items.add(extractItemFromCursor(it))
            }
        }
        return items
    }

    fun getItemById(id: Long): Item? {
        val db = readableDatabase
        val cursor = db.query(TABLE_NAME, null, "$COLUMN_ID = ?", arrayOf(id.toString()), null, null, null)
        
        return cursor.use {
            if (it.moveToFirst()) extractItemFromCursor(it) else null
        }
    }

    fun deleteItem(id: Long): Int {
        return writableDatabase.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    fun updateItemResolved(id: Long, resolved: Boolean): Int {
        val values = ContentValues().apply {
            put(COLUMN_RESOLVED, if (resolved) 1 else 0)
        }
        return writableDatabase.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }

    private fun extractItemFromCursor(cursor: Cursor): Item {
        return Item(
            id = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_ID)),
            title = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TITLE)),
            description = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)),
            category = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)),
            type = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_TYPE)),
            location = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)),
            contactInfo = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CONTACT)),
            date = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)),
            imagePath = cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE)),
            timestamp = cursor.getLong(cursor.getColumnIndexOrThrow(COLUMN_TIMESTAMP)),
            isResolved = cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_RESOLVED)) == 1
        )
    }
}
