package com.brycecampbell.bcheckbook

import android.content.res.Configuration
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

@Composable
fun <T> PopopButton(list: List<T>, onSelection: (T) -> Unit) {
    val expanded = remember { mutableStateOf(false) }

    Button(onClick = {
        expanded.value = !expanded.value
    }) {
        Icon(Icons.Filled.ArrowDropDown, "")
    }

    DropdownMenu(expanded.value, modifier = Modifier.fillMaxWidth(), onDismissRequest = {
        expanded.value = false
    }) {
        list.forEach {
            DropdownMenuItem(modifier = Modifier.fillMaxWidth(), onClick = {
                expanded.value = false
                onSelection(it)
            }) {
                Text(text = it.toString(),
                    modifier = Modifier.wrapContentWidth())
            }
        }
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PopopButtonPreview() {
    BCheckbookTheme {
        PopopButton(mutableListOf("Hello", "World","7"), onSelection = {})
    }
}