package com.brycecampbell.bcheckbook

import android.content.res.Configuration.UI_MODE_NIGHT_YES
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
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
import me.brycecampbell.bcheck.Record
import me.brycecampbell.bcheck.Transaction
import me.brycecampbell.bcheck.TransactionType

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun RecordTable(navController: NavHostController? = null, records: MutableList<Record>) {
    Column {
        TopAppBar(title = {
            Text("Ledger")
        }, actions = {
            IconButton(onClick = {
                records.add(Record())
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
                    optionsExpanded.value = false
                }) {
                    Text("Import Transactions")
                }

                DropdownMenuItem(onClick = {
                    optionsExpanded.value = false
                }) {
                    Text("Export Transactions")
                }
            }
        })

        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp), modifier = Modifier.fillMaxWidth()) {
            /*
            iterate over list with a way to grab element and index,
            to avoid crash caused by Index out of bounds Error.
             */
            itemsIndexed(records) { index, record ->
                val dismissState = rememberDismissState(
                    confirmStateChange = { dismissValue ->
                        if (dismissValue == DismissValue.DismissedToStart) {
                            records.remove(record)
                        }
                        true
                    }
                )



                SwipeToDismiss(dismissState,
                    directions = setOf(DismissDirection.EndToStart),
                    dismissThresholds = {direction ->
                        FractionalThreshold(if (direction == DismissDirection.EndToStart) 0.5F else 0.05F)
                    },
                    background = {
                        val color by animateColorAsState(
                            when (dismissState.targetValue) {
                                DismissValue.Default -> Color.White
                                else -> Color.Red
                            }
                        )

                        val scale by animateFloatAsState(
                            if (dismissState.targetValue == DismissValue.Default) 0.75F else 1F
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
                    RecordView(record) {
                        navController?.navigate("recordDetail/$index")
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