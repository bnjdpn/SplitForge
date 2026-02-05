package com.splitforge.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitforge.app.data.BillingState
import com.splitforge.app.ui.strings.Strings

@Composable
fun PaywallDialog(
    strings: Strings,
    billingState: BillingState,
    onBuy: () -> Unit,
    onRestore: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            Button(onClick = onBuy, enabled = billingState.isConfigured && !billingState.isBusy) {
                Text(if (billingState.priceText.isBlank()) strings.buyPro else "${strings.buyPro} ${billingState.priceText}")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text(strings.cancel) }
        },
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Star, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.proTitle)
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.proSubtitle, style = MaterialTheme.typography.titleSmall)
                Text("- ${strings.proFeatureTemplates}")
                Text("- ${strings.proFeatureAutoPlan}")
                Text("- ${strings.proFeatureExport}")
                Text("- ${strings.proFeatureWhatIf}")
                Spacer(modifier = Modifier.height(4.dp))
                if (!billingState.isConfigured) {
                    Text(strings.purchaseUnavailable, color = MaterialTheme.colorScheme.error)
                }
                OutlinedButton(onClick = onRestore, enabled = billingState.isConfigured && !billingState.isBusy) {
                    Text(strings.restorePurchases)
                }
            }
        }
    )
}
