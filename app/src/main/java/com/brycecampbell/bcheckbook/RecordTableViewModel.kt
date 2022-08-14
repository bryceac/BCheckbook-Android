package com.brycecampbell.bcheckbook

import androidx.compose.runtime.MutableState
import androidx.lifecycle.ViewModel
import me.brycecampbell.bcheck.Record

class RecordTableViewModel(val manager: DBHelper? = null, var records: MutableList<Record>, val queryState: MutableState<String>): ViewModel() {

}