package com.brycecampbell.bcheckbook

import androidx.compose.foundation.layout.Column
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme

@Composable
fun ActivityIndicator(isDisplayed: Boolean) {
    if (isDisplayed) {
        BCheckbookTheme {
            Column {
                CircularProgressIndicator()
            }
        }
    }
}