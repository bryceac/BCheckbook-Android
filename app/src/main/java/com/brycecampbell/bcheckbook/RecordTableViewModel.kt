package com.brycecampbell.bcheckbook

import android.content.Context
import android.net.Uri
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.brycecampbell.bcheck.Record
import me.brycecampbell.bcheck.encodeToJSONString
import java.io.*

class RecordTableViewModel(val manager: DBHelper? = null, val records: MutableList<Record>, val queryState: MutableState<String>): ViewModel() {
    val filteredRecords = when {
        queryState.value.startsWith("category:") -> {
            val categoryRegex = "category:\\s(.*)".toRegex()
            val categoryResult = categoryRegex.find(queryState.value)
            val category = if (categoryResult != null) {
                categoryResult.groupValues[1]
            } else {
                null
            }

            if (category.isNullOrEmpty()) {
                records
            } else {
                records.filter { record ->
                    val recordCategory = record.transaction.category

                    when {
                        category.equals("uncategorized", ignoreCase = true) -> recordCategory.isNullOrEmpty()
                        !recordCategory.isNullOrEmpty() -> recordCategory.contains(category, ignoreCase = true) ||
                                recordCategory.equals(category, ignoreCase = true)
                        else -> false
                    }
                }
            }
        }
        queryState.value.contains("category:") -> {
            val categoryRegex = "category:\\s(.*)".toRegex()
            val categoryResult = categoryRegex.find(queryState.value)
            val category = if (categoryResult != null) {
                categoryResult.groupValues[1]
            } else {
                null
            }

            val vendor = queryState.value.substringBefore("category").trim()

            val recordsFilteredByCategory = if (category.isNullOrEmpty()) {
                records
            } else {
                records.filter { record ->
                    val recordCategory = record.transaction.category

                    when {
                        category.equals("uncategorized", ignoreCase = true) -> recordCategory.isNullOrEmpty()
                        !recordCategory.isNullOrEmpty() -> recordCategory.contains(category, ignoreCase = true) ||
                                recordCategory.equals(category, ignoreCase = true)
                        else -> false
                    }
                }
            }

            recordsFilteredByCategory.filter { record ->
                record.transaction.vendor.contains(vendor, ignoreCase = true) ||
                        record.transaction.vendor.equals(vendor, ignoreCase = true)
            }
        }
        queryState.value.isNotEmpty() -> records.filter { record ->
            record.transaction.vendor.contains(queryState.value, ignoreCase = true) ||
                    record.transaction.vendor.equals(queryState.value, ignoreCase = true)
        }
        else -> records
    }

    fun addRecord(record: Record) {
        records.add(record)
        manager?.addRecord(record)
    }

    suspend fun addRecords(givenRecords: MutableList<Record>) {
            manager?.addRecords(givenRecords)
    }

    fun reloadRecords() {
        if (manager != null) {
            records.clear()
            records.addAll(manager.records)
        }
    }

    suspend fun writeContent(context: Context, uri: Uri, content: String): Result<Unit> {
        var result: Result<Unit>? = null

        runCatching {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                    fileOutputStream.write(content.toByteArray())
                    fileOutputStream.flush()
                }
            }
            result = Result.success(Unit)
        }.onFailure {
            result = Result.failure(it)
        }

        return result!!
    }

    suspend fun loadContent(context: Context, uri: Uri): Result<List<Record>> {
        val json = StringBuilder()
        var result: Result<List<Record>>? = null

        runCatching {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()

                    while(line != null) {
                        json.append(line)
                        line = reader.readLine()
                    }
                }
            }
            result = Result.success(Record.decodeFromString(json.toString()))
        }.onFailure {
            result = Result.failure(it)
        }

        return result!!
    }

    fun exportRecords(uri: Uri): Result<Unit>? {
        var results: Result<Unit>? = null

        viewModelScope.launch(Dispatchers.IO) {
            if (manager != null) {
                results = writeContent(manager.context, uri, manager.records.encodeToJSONString())
            }
        }

        return results
    }

    fun importRecords(uri: Uri) {
        viewModelScope.launch(Dispatchers.IO) {
            if (manager != null) {
                val retrievedRecordsResult = loadContent(manager.context, uri)

                retrievedRecordsResult.onSuccess { retrievedRecords ->
                    addRecords(retrievedRecords.toMutableList())
                }
            }
        }

        reloadRecords()
    }
}