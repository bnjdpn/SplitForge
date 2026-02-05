package com.splitforge.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitforge.app.data.BillingState
import com.splitforge.app.domain.AppState
import com.splitforge.app.ui.strings.Strings

@Composable
fun SettingsScreen(
    state: AppState,
    billingState: BillingState,
    strings: Strings,
    isPro: Boolean,
    onRestore: () -> Unit,
    onResetData: () -> Unit,
    onShowPaywall: () -> Unit
) {
    var showResetConfirm by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.settingsTitle, style = MaterialTheme.typography.headlineMedium)

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Settings, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPro) strings.proBadge else strings.proTitle, style = MaterialTheme.typography.titleMedium)
                }
                Text(strings.proSubtitle)
                if (isPro) {
                    Text(strings.proFeatureTemplates)
                } else {
                    Button(onClick = { onShowPaywall() }) { Text(strings.upgradeToPro) }
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.restorePurchases, style = MaterialTheme.typography.titleMedium)
                Text(billingState.priceText.ifBlank { strings.priceUnavailable })
                OutlinedButton(onClick = onRestore, enabled = billingState.isConfigured && !billingState.isBusy) {
                    Icon(Icons.Default.Restore, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(strings.restorePurchases)
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.resetData, style = MaterialTheme.typography.titleMedium)
                Text(strings.resetDataConfirm)
                OutlinedButton(onClick = { showResetConfirm = true }) { Text(strings.resetData) }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.aboutTitle, style = MaterialTheme.typography.titleMedium)
                Text(strings.aboutBody)
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.privacyPolicyTitle, style = MaterialTheme.typography.titleMedium)
                Text(strings.privacyPolicyBody)
            }
        }

        Text(strings.languageNote, style = MaterialTheme.typography.bodySmall)
        Spacer(modifier = Modifier.height(16.dp))
    }

    if (showResetConfirm) {
        AlertDialog(
            onDismissRequest = { showResetConfirm = false },
            confirmButton = {
                TextButton(onClick = {
                    onResetData()
                    showResetConfirm = false
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { showResetConfirm = false }) { Text(strings.cancel) }
            },
            title = { Text(strings.resetData) },
            text = { Text(strings.resetDataConfirm) }
        )
    }
}
