package com.brycecampbell.bcheckbook

import android.app.DatePickerDialog
import android.content.res.Configuration
import android.widget.DatePicker
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme
import kotlinx.datetime.LocalDate
import me.brycecampbell.bcheck.Record
import me.brycecampbell.bcheck.Transaction
import me.brycecampbell.bcheck.TransactionType
import java.text.NumberFormat

@Composable
fun RecordDetailView(navController: NavHostController? = null, records: MutableList<Record>, categories: MutableList<String>, recordIndex: Int) {
    val currencyFormatter = NumberFormat.getCurrencyInstance()
    val recordState = remember { mutableStateOf(records[recordIndex]) }
    val recordDateString = remember { mutableStateOf(recordState.value.transaction.date.toString()) }
    val recordCheckNumberString = remember { mutableStateOf(if (recordState.value.transaction.checkNumber != null) recordState.value.transaction.checkNumber.toString() else "") }
    val recordVendor = remember { mutableStateOf(recordState.value.transaction.vendor) }
    val recordMemo = remember { mutableStateOf(recordState.value.transaction.memo) }
    val recordAmountString = remember { mutableStateOf(currencyFormatter.format(recordState.value.transaction.amount)) }
    val selectedType = remember { mutableStateOf(recordState.value.transaction.type) }
    val recordCategory = remember { mutableStateOf(recordState.value.transaction.category) }
    val recordReconciledState = remember { mutableStateOf(recordState.value.transaction.isReconciled) }

    Column {
        TopAppBar(title = {
            Text("Record Details")
        }, navigationIcon = {
            IconButton(onClick = {
                navController?.navigate("recordTable")
            }) {
                Icon(Icons.Filled.ArrowBack, "")
            }
        })

        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            TextField(recordDateString.value, onValueChange = {
                recordDateString.value = it
            }, modifier = Modifier.fillMaxWidth())


            TextField(recordCheckNumberString.value, onValueChange = {
                recordCheckNumberString.value = it
            }, modifier = Modifier.fillMaxWidth(), placeholder = {
                Text("Check No.")
            })

            TextField(recordVendor.value, onValueChange = {
                recordVendor.value = it
            }, placeholder = {
                Text("Vendor")
            }, modifier = Modifier.fillMaxWidth())

            TextField(recordMemo.value, onValueChange = {
                recordMemo.value = it
            }, placeholder = {
                Text("Description")
            }, modifier = Modifier.fillMaxWidth())

            TextField(recordAmountString.value, onValueChange = {
                recordAmountString.value = it
            }, modifier = Modifier.fillMaxWidth())

            Picker(TransactionType.values().toList(), selectedType.value, onSelectionChanged = {
                selectedType.value = it
            })

            ComboBox(categories, if (recordCategory.value == null) { "Uncategorized" } else { recordCategory.value.toString() }, onSelectionChanged = {
                recordCategory.value = it
            })

            Row(horizontalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
                Text("Reconciled", Modifier.wrapContentWidth().align(Alignment.CenterVertically))
                Switch(recordReconciledState.value, onCheckedChange = {
                    recordReconciledState.value = it
                })
            }

            Button(modifier = Modifier.fillMaxWidth(), onClick = {
                val newDate = runCatching { LocalDate.parse(recordDateString.value) }.getOrNull()
                val newCheckNumber = recordCheckNumberString.value.toIntOrNull()
                val newAmount = {
                    val dollarInput = runCatching { currencyFormatter.parse(recordAmountString.value) }.getOrNull()
                    val doubleInput = recordAmountString.value.toDoubleOrNull()
                    var newValue: Double? = null

                    if (dollarInput != null) {
                        newValue = dollarInput.toDouble()
                    } else if (doubleInput != null) {
                        newValue = doubleInput
                    }

                    newValue
                }

                val newType = selectedType.value
                val newCategory = recordCategory.value
                val newReconciledStatus = recordReconciledState.value

                if (newDate != null) {
                    recordState.value.transaction.date = newDate
                }

                recordState.value.transaction.checkNumber = newCheckNumber
                recordState.value.transaction.vendor = recordVendor.value
                recordState.value.transaction.memo = recordMemo.value

                if (newAmount() != null && newAmount() != recordState.value.transaction.amount) {
                    recordState.value.transaction.amount = newAmount()!!
                }

                if (newType != recordState.value.transaction.type) {
                    recordState.value.transaction.type = newType
                }

                if (newCategory != recordState.value.transaction.category) {
                    if (newCategory != null && !newCategory.equals("uncategorized", ignoreCase = true)) {
                        if (!categories.contains(newCategory)) {
                            categories.add(newCategory.toString())
                        }

                        recordState.value.transaction.category = newCategory
                    } else {
                        recordState.value.transaction.category = null
                    }
                }

                if (newReconciledStatus != recordState.value.transaction.isReconciled) {
                    recordState.value.transaction.isReconciled = newReconciledStatus
                }
            }) {
                Text("Submit", modifier = Modifier.fillMaxWidth().wrapContentWidth())
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecordDetailPreview() {
    BCheckbookTheme {
        RecordDetailView(records = mutableListOf(Record("FF04C3DC-F0FE-472E-8737-0F4034C049F0",
            Transaction("2021-07-08", 1260,
                "Opening Balance", "Sam Hill Credit Union", "Open Account", 500.0, TransactionType.Deposit, true)
        ),
            Record("1422CBC6-7B0B-4584-B7AB-35167CC5647B",
                Transaction("2021-07-08", 1260,
                    null, "Fake Street Electronics", "Head set", 200.0, TransactionType.Withdrawal, false)
            ),
            Record("BB22187E-0BD3-41E8-B3D8-8136BD700865",
                Transaction("2021-07-08", 1260,
                    null, "Velociraptor Entertainment", "Pay Day", 50000.0, TransactionType.Deposit, false)
            )), categories = mutableListOf("Hello", "World", "7"), recordIndex = 0)
    }
}