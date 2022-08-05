package com.brycecampbell.bcheckbook

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.modifier.modifierLocalOf
import androidx.compose.ui.tooling.preview.Preview
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme
import kotlinx.coroutines.selects.select
import me.brycecampbell.bcheck.Record

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun <T> Picker(list: List<T>, selected: T, onSelectionChanged: (T) -> Unit) {
    val selectedItem = remember { mutableStateOf(selected) }
    val expanded = remember { mutableStateOf(false) }

    ExposedDropdownMenuBox(expanded.value, onExpandedChange = {
        expanded.value = !expanded.value
    }) {
        OutlinedTextField(selectedItem.value.toString(),
            onValueChange = {}, label = {
                if (isSystemInDarkTheme()) {
                    Text("Type", color = Color.Black)
                } else {
                    Text("Type")
                }
            }, modifier = Modifier.fillMaxWidth().clickable {
                expanded.value = !expanded.value
            },
            trailingIcon = {
                if (isSystemInDarkTheme()) {
                    Icon(Icons.Filled.ArrowDropDown,
                        "",
                        tint = Color.Black
                    )
                } else {
                    Icon(Icons.Filled.ArrowDropDown, "")
                }
            }, readOnly = true)

        ExposedDropdownMenu(expanded.value, modifier = Modifier.fillMaxWidth(), onDismissRequest = {
            expanded.value = false
        }) {
            list.forEach {
                DropdownMenuItem(modifier = Modifier.fillMaxWidth(), onClick = {
                    selectedItem.value = it
                    expanded.value = false
                }) {
                    Text(text = it.toString(),
                        modifier = Modifier.wrapContentWidth())
                }
            }
        }
    }

    onSelectionChanged(selectedItem.value)
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PickerPreview() {
    BCheckbookTheme {
       Picker(listOf<String>("Hello", "World", "7"), "Hello", onSelectionChanged = {})
    }
}