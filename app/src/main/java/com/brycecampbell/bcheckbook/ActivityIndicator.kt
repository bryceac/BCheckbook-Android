package com.brycecampbell.bcheckbook

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme

@Composable
fun ActivityIndicator(isDisplayed: Boolean) {
    if (isDisplayed) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Column(verticalArrangement = Arrangement.Center) {
                CircularProgressIndicator()
            }
        }
    }
}