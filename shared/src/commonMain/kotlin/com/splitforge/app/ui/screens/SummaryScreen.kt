package com.splitforge.app.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.splitforge.app.domain.Segment
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.util.SummaryData
import com.splitforge.app.util.TimeFormatter

@Composable
fun SummaryScreen(
    summary: SummaryData,
    strings: Strings,
    onShare: (SummaryData) -> Unit,
    onClose: () -> Unit
) {
    Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                Text(strings.summaryTitle, style = MaterialTheme.typography.headlineMedium, modifier = Modifier.weight(1f))
                IconButton(onClick = onClose) { Icon(Icons.Default.Close, contentDescription = strings.back) }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(summary.templateName, style = MaterialTheme.typography.titleMedium)
                    Text("${strings.totalTime}: ${TimeFormatter.formatSeconds(summary.totalSeconds, strings)}")
                    Text(if (summary.isDoubles) strings.doubles else strings.solo)
                }
            }

            summary.segments.forEach { segment ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        val label = when (val seg = segment.segment) {
                            is Segment.Run -> "${strings.runLabel} ${strings.formatDistance(seg.distanceKm)}"
                            is Segment.Station -> seg.name
                        }
                        Text("${segment.index + 1}. $label", style = MaterialTheme.typography.titleSmall)
                        segment.assignedAthlete?.let { Text("${strings.assignTo}: $it") }
                        Text(TimeFormatter.formatSeconds(segment.seconds, strings))
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = { onShare(summary) }) {
                Icon(Icons.Default.Share, contentDescription = strings.share)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.share)
            }
        }
    }
}
