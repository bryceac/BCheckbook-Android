package com.brycecampbell.bcheckbook

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.brycecampbell.bcheck.Record
import java.io.File
import java.io.FileOutputStream

class DBHelper(val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {
    private val preferences = context.getSharedPreferences("${context.packageName}.database_versions",
    Context.MODE_PRIVATE)

    private val databaseIsOutdated: Boolean get() {
        return preferences.getInt(DATABASE_NAME, 0) < VERSION
    }

    override fun onCreate(p0: SQLiteDatabase?) {}

    override fun onUpgrade(p0: SQLiteDatabase?, p1: Int, p2: Int) {}

    private fun copyDatabaseFromAssets() {
        val inputStream = context.assets.open("$DATABASE_NAME.db")

        try {
            val outputFile = File(context.getDatabasePath(DATABASE_NAME).path)
            val outputStream = FileOutputStream(outputFile)

            inputStream.copyTo(outputStream)
            inputStream.close()

            outputStream.flush()
            outputStream.close()
        } catch (exception: Throwable) {
            throw RuntimeException("$DATABASE_NAME could not be copied", exception)
        }
    }

    private fun writeDatabaseVersionInPreferences() {
        preferences.edit().apply {
            putInt(DATABASE_NAME, VERSION)
            apply()
        }
    }

    @Synchronized
    private fun installOrUpdateIfNeeded() {
        if (databaseIsOutdated) {
            context.deleteDatabase(DATABASE_NAME)
            copyDatabaseFromAssets()
            writeDatabaseVersionInPreferences()
        }
    }

    override fun getWritableDatabase(): SQLiteDatabase {
        installOrUpdateIfNeeded()
        return super.getWritableDatabase()
    }

    override fun getReadableDatabase(): SQLiteDatabase {
        installOrUpdateIfNeeded()
        return super.getReadableDatabase()
    }

    fun databaseContainsRecord(record: Record): Boolean {
        val db = super.getReadableDatabase()
        val id = record.id.uppercase()

        val cursor = db.rawQuery("SELECT id FROM ledger WHERE = id", null)
        return cursor.count == 1
    }

    fun databaseContainsCategory(category: String): Boolean {
        val db = super.getReadableDatabase()

        val cursor = db.rawQuery("SELECT id FROM ledger WHERE = id", null)
        return cursor.count == 1
    }

    companion object {
        const val DATABASE_NAME = "register"
        const val VERSION = 1
    }
}