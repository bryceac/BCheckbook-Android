package com.brycecampbell.bcheckbook

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import me.brycecampbell.bcheck.Record
import me.brycecampbell.bcheck.Transaction
import me.brycecampbell.bcheck.TransactionType
import java.io.File
import java.io.FileOutputStream
import kotlin.math.abs

class DBHelper(val context: Context): SQLiteOpenHelper(context, DATABASE_NAME, null, VERSION) {
    private val preferences = context.getSharedPreferences("${context.packageName}.database_versions",
    Context.MODE_PRIVATE)

    private val databaseIsOutdated: Boolean get() {
        return preferences.getInt(DATABASE_NAME, 0) < VERSION
    }

    val records: MutableList<Record> get() {
        return retrieveRecords()
    }

    val categories: MutableList<String> get() {
        return retrieveCategories()
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

    private fun databaseContainsRecord(record: Record): Boolean {
        val db = this.writableDatabase
        val id = record.id.uppercase()

        val cursor = db.rawQuery("SELECT id FROM ledger WHERE id = \"$id\"", null)
        cursor.moveToFirst()
        val recordExists = cursor.count == 1
        cursor.close()
        return recordExists
    }

    private fun databaseContainsCategory(category: String): Boolean {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT category FROM categories WHERE category = \"$category\"", null)
        cursor.moveToFirst()
        val categoryExists = cursor.count == 1
        cursor.close()
        return categoryExists
    }

    private fun idOfCategory(category: String): Int {
        val db = this.writableDatabase

        val cursor = db.rawQuery("SELECT id FROM categories WHERE category = \"$category\"", null)
        cursor.moveToFirst()
        val id = cursor.getInt(0)
        cursor.close()
        return id
    }

    private fun addCategory(category: String) {
        if (!databaseContainsCategory(category)) {
            val db = this.writableDatabase
            val contentValues = ContentValues()
            contentValues.put("category", category)

            db.insert("categories", "category", contentValues)
        }
    }

    fun addRecord(record: Record) {
        if (!databaseContainsRecord(record)) {
            val db = this.writableDatabase
            val id = record.id.uppercase()
            val date = record.transaction.date.toString()
            val checkNumber = record.transaction.checkNumber
            val category: Int? = record.transaction.category?.let { recordCategory ->
                val categoryID = if (databaseContainsCategory(recordCategory)) {
                    idOfCategory(recordCategory)
                } else {
                    addCategory(recordCategory)

                    idOfCategory(recordCategory)
                }

                categoryID
            }

            val vendor = record.transaction.vendor
            val memo = record.transaction.memo
            val amount = if (record.transaction.type == TransactionType.Withdrawal && record.transaction.amount > 0) {
                record.transaction.amount*-1
            } else {
                record.transaction.amount
            }
            val reconciled = if (record.transaction.isReconciled) {
                1
            } else {
                0
            }

            val contentValues = ContentValues()
            contentValues.put("id", id)
            contentValues.put("date", date)
            contentValues.put("check_number", checkNumber)
            contentValues.put("category", category)
            contentValues.put("vendor", vendor)
            contentValues.put("memo", memo)
            contentValues.put("amount", amount)
            contentValues.put("reconciled", reconciled)

            db.insert("trades", null, contentValues)
            db.close()
        } else {
            updateRecord(record)
        }
    }

    fun updateRecord(record: Record) {
        if (databaseContainsRecord(record)) {
            val db = this.writableDatabase
            val id = record.id.uppercase()
            val date = record.transaction.date.toString()
            val checkNumber = record.transaction.checkNumber
            val category: Int? = record.transaction.category?.let { recordCategory ->
                val categoryID = if (databaseContainsCategory(recordCategory)) {
                    idOfCategory(recordCategory)
                } else {
                    addCategory(recordCategory)

                    idOfCategory(recordCategory)
                }

                categoryID
            }

            val vendor = record.transaction.vendor
            val memo = record.transaction.memo
            val amount = if (record.transaction.type == TransactionType.Withdrawal && record.transaction.amount > 0) {
                record.transaction.amount*-1
            } else {
                record.transaction.amount
            }
            val reconciled = if (record.transaction.isReconciled) {
                1
            } else {
                0
            }

            val contentValues = ContentValues()
            contentValues.put("date", date)
            contentValues.put("check_number", checkNumber)
            contentValues.put("category", category)
            contentValues.put("vendor", vendor)
            contentValues.put("memo", memo)
            contentValues.put("amount", amount)
            contentValues.put("reconciled", reconciled)

            db.update("trades", contentValues, "id = ?", arrayOf(id))
            db.close()
        }
    }

    fun addRecords(records: MutableList<Record>) {
        for (record in records) {
            addRecord(record)
        }
    }

    fun removeRecord(record: Record) {
        val db = this.writableDatabase
        db.delete("trades", "id = ?", arrayOf(record.id.uppercase()))
        db.close()
    }

    fun removeRecords(records: MutableList<Record>) {
        for (record in records) {
            removeRecord(record)
        }
    }

    private fun retrieveRecords(): MutableList<Record> {
        val db = this.readableDatabase
        var records = mutableListOf<Record>()
        val cursor = db.rawQuery("SELECT id, date, check_number, reconciled, vendor, memo, category, amount FROM ledger", null)

        cursor.use { cursor ->
            while (cursor.moveToNext()) {
                val id = cursor.getString(0)
                val date = cursor.getString(1)
                val checkNumber: Int? = if (cursor.isNull(2)) {
                    null
                } else {
                    cursor.getInt(2)
                }
                val reconciled = cursor.getString(3) != "N"
                val vendor = cursor.getString(4)
                val memo = cursor.getString(5)
                val category = if (cursor.isNull(6)) {
                    null
                } else {
                    cursor.getString(6)
                }
                val amount = cursor.getDouble(7)

                val record = Record(id, Transaction(date,
                    checkNumber,
                    category,
                    vendor,
                    memo,
                    abs(amount),
                    if (amount <= 0.0) {
                        TransactionType.Withdrawal
                    } else {
                        TransactionType.Deposit
                    },
                    reconciled))

                records.add(record)
            }
        }
        cursor.close()
        db.close()
        return records
    }

    private fun retrieveCategories(): MutableList<String> {
        val db = this.readableDatabase
        var categories = mutableListOf<String>()
        val cursor = db.rawQuery("SELECT category FROM categories", null)
        cursor.use {cursor ->
            while(cursor.moveToNext()) {
                categories.add(cursor.getString(0))
            }
        }
        cursor.close()
        db.close()
        return categories
    }

    fun balanceForRecord(record: Record): Double {
        return if (databaseContainsRecord(record)) {
            val db = this.readableDatabase
            val cursor = db.rawQuery("SELECT balance FROM ledger WHERE id = \"${record.id.uppercase()}\"", null)
            cursor.moveToFirst()
            val balance = cursor.getDouble(0)
            cursor.close()
            db.close()
            balance
        } else {
            0.0
        }
    }

    companion object {
        const val DATABASE_NAME = "register"
        const val VERSION = 1
    }
}