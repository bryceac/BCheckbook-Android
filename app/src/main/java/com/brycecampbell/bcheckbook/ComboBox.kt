package com.brycecampbell.bcheckbook

import android.content.res.Configuration
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ComboBox(list: MutableList<String>, selected: String, onSelectionChanged: (String) -> Unit) {
    val selectedItem = remember { mutableStateOf(selected) }
    val expanded = remember { mutableStateOf(false) }

    Row {
        TextField(selectedItem.value, onValueChange = {
            selectedItem.value = it
        })

        PopopButton(list, onSelection = {
            selectedItem.value = it
        })
    }

    onSelectionChanged(selectedItem.value)
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun ComboBoxPreview() {
    BCheckbookTheme {
        ComboBox(mutableListOf("Hello", "World", "7"), "Hello", onSelectionChanged = {})
    }
}