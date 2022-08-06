package com.brycecampbell.bcheckbook

import android.os.Bundle
import android.os.Environment
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme
import me.brycecampbell.bcheck.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths


class MainActivity : ComponentActivity() {

    lateinit var recordStore: SnapshotStateList<Record>
    lateinit var categoryStore: SnapshotStateList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val manager = DBHelper(this)

        setContent {
            val navController = rememberNavController()
            recordStore = remember { mutableStateListOf() }
            categoryStore = remember { mutableStateListOf() }
            if (!manager.records.isEmpty()) {
                recordStore.addAll(manager.records)
            }
            categoryStore.addAll(manager.categories)

            BCheckbookTheme {
                // A surface container using the 'background' color from the theme
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colors.background) {
                    NavHost(navController, "recordTable") {
                        composable("recordTable") { RecordTable(navController, recordStore) }
                        composable("recordDetail/{index}", arguments = listOf(
                            navArgument("index") { type = NavType.IntType }
                        )) {
                            it.arguments?.getInt("index")?.let { it1 -> RecordDetailView(navController, recordStore, categoryStore, recordIndex = it1) }
                        }
                    }
                }
            }
        }
    }
}