package id.ac.binus.recipeapp.adapter

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

class Helper(context: Context?): SQLiteOpenHelper(context, "recipe.db", null, 6) {

//    private val SQL_CREATE_TABLE_USER = """
//        CREATE TABLE IF NOT EXISTS user(
//            id INTEGER PRIMARY KEY AUTOINCREMENT,
//            email TEXT,
//            username TEXT,
//            password TEXT
//        )
//    """.trimIndent()

    private val SQL_CREATE_TABLE_RECIPE = """
        CREATE TABLE IF NOT EXISTS recipe(
            name TEXT PRIMARY KEY,
            category TEXT,
            rating REAL,
            duration TEXT,
            imageResId TEXT
        )
    """.trimIndent()

//    private val SQL_DROP_TABLE_USER = "DROP TABLE IF EXISTS user"
    private val SQL_DROP_TABLE_RECIPE = "DROP TABLE IF EXISTS recipe"

    override fun onCreate(db: SQLiteDatabase?) {
//        db?.execSQL(SQL_CREATE_TABLE_USER)
        db?.execSQL(SQL_CREATE_TABLE_RECIPE)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
//        db?.execSQL(SQL_DROP_TABLE_USER)
        db?.execSQL(SQL_DROP_TABLE_RECIPE)
        onCreate(db)
    }
}