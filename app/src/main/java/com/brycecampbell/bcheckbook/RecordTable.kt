package com.brycecampbell.bcheckbook

import android.content.Context
import android.content.res.Configuration.UI_MODE_NIGHT_YES
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme
import me.brycecampbell.bcheck.*
import java.io.*

fun writeContent(context: Context, uri: Uri, content: String) {

    try {
        context.contentResolver.openFileDescriptor(uri, "w")?.use { parcelFileDescriptor ->
            FileOutputStream(parcelFileDescriptor.fileDescriptor).use { fileOutputStream ->
                fileOutputStream.write(content.toByteArray())
                fileOutputStream.flush()
            }
        }
        Toast.makeText(context, "Data exported Successfully", Toast.LENGTH_SHORT).show()
    } catch (exception: FileNotFoundException) {
        print(exception.localizedMessage)
    } catch (exception: IOException) {
        print(exception.localizedMessage)
    }
}

fun loadContent(context: Context, uri: Uri, completion: (MutableList<Record>) -> Unit) {
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecordTable(navController: NavHostController? = null, records: MutableList<Record>, manager: DBHelper? = null) {
    val exportURI = remember { mutableStateOf<Uri?>(null) }
    val importURI = remember { mutableStateOf<Uri?>(null) }
    val importLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) {
        importURI.value = it

        if (manager != null && importURI.value != null) {
            loadContent(manager.context, importURI.value!!) {retrievedRecords ->
                manager.addRecords(retrievedRecords)
                records.clear()
                records.addAll(manager.records)
            }
        }
    }

    val exportLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/json")) {
        exportURI.value = it

        if (manager != null && exportURI.value != null) {
            writeContent(manager.context, exportURI.value!!, manager.records.encodeToJSONString())
        }
    }

    val query = remember { mutableStateOf("") }

    val filteredRecords = when {
            query.value.startsWith("category:") -> {
                val categoryRegex = "category:\\s(.*)".toRegex()
                val categoryResult = categoryRegex.find(query.value)
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
            query.value.contains("category:") -> {
                val categoryRegex = "category:\\s(.*)".toRegex()
                val categoryResult = categoryRegex.find(query.value)
                val category = if (categoryResult != null) {
                    categoryResult.groupValues[1]
                } else {
                    null
                }

                val vendor = query.value.substringBefore("category").trim()

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
            query.value.isNotEmpty() -> records.filter { record ->
                record.transaction.vendor.contains(query.value, ignoreCase = true) ||
                        record.transaction.vendor.equals(query.value, ignoreCase = true)
            }
            else -> records
        }

    Column {
        TopAppBar(title = {
            Text("Ledger")
        }, actions = {
            IconButton(onClick = {
                val record = Record()
                records.add(record)
                manager?.addRecord(record)
            }) {
                Icon(Icons.Filled.Add, "")
            }

            val optionsExpanded = remember { mutableStateOf(false) }

            IconButton(onClick = {
                optionsExpanded.value = !optionsExpanded.value
            }) {
                Icon(Icons.Filled.MoreVert, "")
            }

            DropdownMenu(optionsExpanded.value, onDismissRequest = {
                optionsExpanded.value = false
            }) {
                DropdownMenuItem(onClick = {
                    importLauncher.launch(arrayOf(
                        "application/json"
                    ))
                    optionsExpanded.value = false
                }) {
                    Text("Import Transactions")
                }

                DropdownMenuItem(onClick = {
                    exportLauncher.launch("transactions")
                    optionsExpanded.value = false
                }) {
                    Text("Export Transactions")
                }
            }
        })

        TextField(query.value, onValueChange = {
            query.value = it
        }, modifier = Modifier.fillMaxWidth(),
            placeholder = {
                Text("Search Transactons...")
            },
            leadingIcon = {
            Icon(Icons.Filled.Search, "")
        }, trailingIcon = {
            if (query.value.isNotEmpty()) {
                IconButton(onClick = {
                    query.value = ""
                }) {
                    Icon(Icons.Filled.Clear, "")
                }
            }
        })

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            /*
            iterate over list with a way to grab element and index,
            to avoid crash caused by Index out of bounds Error.

            Key value is specified, so that swipe to delete functionality works properly.
             */
            itemsIndexed(filteredRecords, {_, record ->
                record.id
            }) { _, record ->
                val dismissState = rememberDismissState(
                    initialValue = DismissValue.Default,
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart) {
                            records.remove(record)
                            manager?.removeRecord(record)
                        }
                        true
                    }
                )

                val mainIndex = records.indexOfFirst { it.id == record.id }



                SwipeToDismiss(dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    dismissThresholds = {direction ->
                        FractionalThreshold(if (direction == DismissDirection.EndToStart) 0.1F else 0.05F)
                    },
                    background = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                DismissValue.Default -> Color.White
                                else -> Color.Red
                            }
                        )

                        Box(Modifier.fillMaxSize()
                            .background(color)
                            .padding(horizontal = Dp(20F)),
                        contentAlignment = Alignment.CenterEnd) {
                            if (dismissState.targetValue != DismissValue.Default) {
                                Icon(Icons.Filled.Delete, "", tint = Color.White)
                            }
                        }
                    }
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        RecordView(record, manager) {
                            navController?.navigate("recordDetail/$mainIndex")
                        }
                    }

                }
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = UI_MODE_NIGHT_YES)
@Composable
fun RecordTablePreview() {
    BCheckbookTheme {
        RecordTable(records = mutableListOf(Record("FF04C3DC-F0FE-472E-8737-0F4034C049F0",
            Transaction("2021-07-08", 1260,
                "Opening Balance", "Sam Hill Credit Union", "Open Account", 500.0, TransactionType.Deposit, true)
        ),
            Record("1422CBC6-7B0B-4584-B7AB-35167CC5647B",
                Transaction("2021-07-08", null,
                    null, "Fake Street Electronics", "Head set", 200.0, TransactionType.Withdrawal, false)
            ),
            Record("BB22187E-0BD3-41E8-B3D8-8136BD700865",
                Transaction("2021-07-08", null,
                    null, "Velociraptor Entertainment", "Pay Day", 50000.0, TransactionType.Deposit, false)
            )))
    }
}