package cat.dam.andy.sqlite_compose

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper


class DatabaseHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        private const val DB_VERSION = 1
        private const val DB_NAME = "Contacts"
        private const val TABLE_NAME = "contacts"
        private const val COLUMN_ID = "_id"
        private const val COLUMN_NAME = "name"
        private const val COLUMN_PHONE = "phone"
    }

    override fun onCreate(db: SQLiteDatabase) {
        val query =
            "CREATE TABLE IF NOT EXISTS $TABLE_NAME ($COLUMN_ID INTEGER PRIMARY KEY AUTOINCREMENT, $COLUMN_NAME VARCHAR, $COLUMN_PHONE VARCHAR)"
        db.execSQL(query)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_NAME")
        onCreate(db)
    }

    fun listAll(): List<Item> {
        val sql = "select * from $TABLE_NAME"
        val db = readableDatabase
        val items = mutableListOf<Item>()
        val cursor: Cursor = db.rawQuery(sql, null)
        if (cursor.moveToFirst()) {
            do {
                val id = cursor.getInt(0)
                val name = cursor.getString(1)
                val tel = cursor.getString(2)
                items.add(Item(id, name, tel))
            } while (cursor.moveToNext())
        }
        cursor.close()
        return items
    }

    fun addContact(item: Item) {
        val values = ContentValues()
        values.put(COLUMN_NAME, item.name)
        values.put(COLUMN_PHONE, item.tel)
        val db = writableDatabase
        db.insert(TABLE_NAME, null, values)
    }

    fun updateContact(item: Item) {
        val values = ContentValues()
        values.put(COLUMN_NAME, item.name)
        values.put(COLUMN_PHONE, item.tel)
        val db = writableDatabase
        db.update(TABLE_NAME, values, "$COLUMN_ID = ?", arrayOf(item.id.toString()))
    }

    fun findContacts(name: String): List<Item> {
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_NAME LIKE '%$name%';"
        val items = mutableListOf<Item>()
        readableDatabase.use { db ->
            db.rawQuery(query, null).use { cursor ->
                if (cursor.moveToFirst()) {
                    do {
                        val id = cursor.getInt(0)
                        val contactName = cursor.getString(1)
                        val contactPhone = cursor.getString(2)
                        items.add(Item(id, contactName, contactPhone))
                    } while (cursor.moveToNext())
                }
            }
        }
        return items
    }

    fun findFirstContact(name: String): Item? {
        val query = "SELECT * FROM $TABLE_NAME WHERE $COLUMN_NAME = ?"
        val db = readableDatabase
        var item: Item? = null

        db.rawQuery(query, arrayOf(name)).use { cursor ->
            if (cursor.moveToFirst()) {
                val idIndex = cursor.getColumnIndex(COLUMN_ID)
                val nameIndex = cursor.getColumnIndex(COLUMN_NAME)
                val phoneIndex = cursor.getColumnIndex(COLUMN_PHONE)

                if (idIndex >= 0 && nameIndex >= 0 && phoneIndex >= 0) {
                    val id = cursor.getInt(idIndex)
                    val contactName = cursor.getString(nameIndex)
                    val contactPhone = cursor.getString(phoneIndex)
                    item = Item(id, contactName, contactPhone)
                }
            }
        }

        return item
    }

    fun removeContact(id: Int) {
        val db = writableDatabase
        db.delete(TABLE_NAME, "$COLUMN_ID = ?", arrayOf(id.toString()))
    }
}