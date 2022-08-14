package com.brycecampbell.bcheckbook

import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import me.brycecampbell.bcheck.Record
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

    suspend fun addRecords(givenRecords: MutableList<Record>) {
        viewModelScope.launch(Dispatchers.Default) {
            manager?.addRecords(givenRecords)
        }
    }

    fun reloadRecords() {
        if (manager != null) {
            records.clear()
            records.addAll(manager.records)
        }
    }

    suspend fun writeContent(context: Context, uri: Uri, content: String) {

        try {
            context.contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
                FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                    fileOutputStream.write(content.toByteArray())
                    fileOutputStream.flush()
                }
            }
        } catch (exception: FileNotFoundException) {
            print(exception.localizedMessage)
        } catch (exception: IOException) {
            print(exception.localizedMessage)
        }
    }

    suspend fun loadContent(context: Context, uri: Uri, completion: (MutableList<Record>) -> Unit) {
        val json = StringBuilder()

        try {
            context.contentResolver.openInputStream(uri).use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    var line = reader.readLine()

                    while(line != null) {
                        json.append(line)
                        line = reader.readLine()
                    }
                }
            }
            completion(Record.decodeFromString(json.toString()).toMutableList())
        } catch (exception: IOException) {
            Toast.makeText(context, "File could not be found or read.", Toast.LENGTH_SHORT).show()
            print(exception.localizedMessage)
        }
    }
}