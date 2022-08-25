package com.brycecampbell.bcheckbook

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun DatePicker(selectedDate: MutableState<LocalDate>) {
    val context = LocalContext.current
    val expanded = remember { mutableStateOf(false) }
    val datePicker = DatePickerDialog(context, { _: DatePicker, year: Int, month: Int, day: Int ->
        selectedDate.value = LocalDate(year, month+1, day)
        expanded.value = false
    }, selectedDate.value.year, selectedDate.value.monthNumber-1, selectedDate.value.dayOfMonth)

    ExposedDropdownMenuBox(expanded.value, onExpandedChange = {
            expanded.value = !expanded.value
    }, modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(selectedDate.value.toString(), onValueChange = {}, label = {
            Text("Date")
        }, modifier = Modifier.fillMaxWidth().clickable {
            expanded.value != expanded.value
        }, trailingIcon = {
            Icon(Icons.Filled.DateRange, "")
        }, readOnly = true)

        if (expanded.value) {
            datePicker.show()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun DatePickerPreview() {
    val date = remember { mutableStateOf(Clock.System.todayIn(TimeZone.currentSystemDefault())) }

    DatePicker(date)
}