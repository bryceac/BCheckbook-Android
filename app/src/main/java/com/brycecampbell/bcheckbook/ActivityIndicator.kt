package com.brycecampbell.bcheckbook

import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable

@Composable
fun ActivityIndicator(isDisplayed: Boolean) {
    if (isDisplayed) {
        Column {
            CircularProgressIndicator()
        }
    }
}