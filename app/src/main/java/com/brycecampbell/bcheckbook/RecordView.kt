package com.brycecampbell.bcheckbook

import android.content.res.Configuration
import android.graphics.Paint.Align
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.brycecampbell.bcheckbook.ui.theme.BCheckbookTheme
import me.brycecampbell.bcheck.Record
import me.brycecampbell.bcheck.Transaction
import me.brycecampbell.bcheck.TransactionType
import java.text.NumberFormat

@Composable
fun RecordView(record: Record, tapAction: () -> Unit) {
    val currencyFormatter = NumberFormat.getCurrencyInstance()
    val maxWidth = 0.8F

    Row(modifier = Modifier.fillMaxWidth().pointerInput("SomeKey?") {
        detectTapGestures(
            onTap = { tapAction() }
        )
    }, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Column(modifier = Modifier) {
            Text(record.transaction.date.toString(), Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))

            if (record.transaction.checkNumber != null) {
                Text(record.transaction.checkNumber.toString(), Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
            }

            if (record.transaction.isReconciled) {
                Icon(Icons.Filled.Check, "")
            }

            if (record.transaction.vendor.isNotEmpty()) {
                Text(record.transaction.vendor, modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
            } else {
                Text("N/A", modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
            }

            if (record.transaction.memo.isNotEmpty()) {
                Text(record.transaction.memo, modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
            } else {
                Text("N/A", modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
            }

            when (record.transaction.type) {
                TransactionType.Deposit -> {
                    Text(currencyFormatter.format(record.transaction.amount), modifier = Modifier.wrapContentWidth().align(
                        Alignment.Start).fillMaxWidth(maxWidth))
                    Text("N/A", modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
                }
                TransactionType.Withdrawal -> {
                    Text("N/A", modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
                    Text(currencyFormatter.format(record.transaction.amount), modifier = Modifier.wrapContentWidth().align(
                        Alignment.Start).fillMaxWidth(maxWidth))
                }
            }

            Text(currencyFormatter.format(0), modifier = Modifier.wrapContentWidth().fillMaxWidth(maxWidth))

            if (record.transaction.category != null) {
                Text(record.transaction.category.toString(), modifier = Modifier.wrapContentWidth().align(Alignment.Start).fillMaxWidth(maxWidth))
            }
        }

        Icon(
            imageVector = Icons.Filled.KeyboardArrowRight,
            contentDescription = "Reveal Details",
            modifier = Modifier.align(Alignment.CenterVertically).fillMaxWidth()
        )
    }
}

@Preview(name = "Light", showBackground = true)
@Preview(name = "Dark", showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun RecordViewPreview() {
    BCheckbookTheme {
        RecordView(Record(transaction = Transaction(isReconciled = true))) {}
    }
}