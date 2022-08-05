package com.brycecampbell.bcheckbook

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme
import me.brycecampbell.bcheck.*


class MainActivity : ComponentActivity() {

    lateinit var recordStore: SnapshotStateList<Record>
    lateinit var categoryStore: SnapshotStateList<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            val navController = rememberNavController()
            recordStore = remember { mutableStateListOf() }
            categoryStore = remember { mutableStateListOf(
                "Hello",
                "World",
                "7"
            ) }
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