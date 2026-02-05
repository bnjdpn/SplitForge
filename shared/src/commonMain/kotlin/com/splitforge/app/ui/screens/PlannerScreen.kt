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
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.splitforge.app.domain.Athlete
import com.splitforge.app.domain.EstimationEngine
import com.splitforge.app.domain.Plan
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.Template
import com.splitforge.app.ui.SummaryBuilder
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.util.SummaryData
import com.splitforge.app.util.TimeFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PlannerScreen(
    template: Template,
    plan: Plan,
    athleteA: Athlete,
    athleteB: Athlete?,
    isPro: Boolean,
    previousTotalSeconds: Int?,
    strings: Strings,
    onAssignAthlete: (Int, String) -> Unit,
    onSuggestSplit: () -> Unit,
    onRequestSummary: () -> Unit,
    onShareSummary: (SummaryData) -> Unit
) {
    val totalSeconds = remember(template, plan, athleteA, athleteB) {
        EstimationEngine.estimateTotalSeconds(template, athleteA, athleteB, plan)
    }
    val deltaSeconds = if (isPro) EstimationEngine.deltaSeconds(previousTotalSeconds, totalSeconds) else 0
    val (athleteASeconds, athleteBSeconds) = EstimationEngine.estimateStationWorkloadSeconds(template, athleteA, athleteB, plan)
    val summary = SummaryBuilder.build(template, plan, athleteA, athleteB, strings)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(strings.tabPlanner, style = MaterialTheme.typography.headlineMedium)

        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.totalTime, color = MaterialTheme.colorScheme.onPrimary)
                Text(
                    TimeFormatter.formatSeconds(totalSeconds, strings),
                    style = MaterialTheme.typography.displayMedium.copy(fontWeight = FontWeight.Bold),
                    color = MaterialTheme.colorScheme.onPrimary
                )
                if (isPro && previousTotalSeconds != null) {
                    val sign = if (deltaSeconds >= 0) "+" else ""
                    Text(
                        "${strings.deltaLabel}: $sign${TimeFormatter.formatSeconds(kotlin.math.abs(deltaSeconds), strings)}",
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
            }
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.athleteWorkload, style = MaterialTheme.typography.titleMedium)
                Text("${athleteA.name}: ${TimeFormatter.formatSeconds(athleteASeconds, strings)}")
                if (plan.isDoubles && athleteB != null) {
                    Text("${athleteB.name}: ${TimeFormatter.formatSeconds(athleteBSeconds, strings)}")
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ElevatedButton(onClick = onSuggestSplit, modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Icon(Icons.Default.AutoFixHigh, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.suggestSplit)
            }
            OutlinedButton(onClick = onRequestSummary, modifier = Modifier.weight(1f)) {
                androidx.compose.material3.Icon(Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.exportSummary)
            }
        }
        Text(strings.suggestSplitHint, style = MaterialTheme.typography.bodySmall)

        template.segments.forEachIndexed { index, segment ->
            SegmentPlanCard(
                index = index,
                segment = segment,
                plan = plan,
                athleteA = athleteA,
                athleteB = athleteB,
                strings = strings,
                onAssignAthlete = onAssignAthlete
            )
        }

        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(strings.summaryTitle, style = MaterialTheme.typography.titleMedium)
                Text(summary.templateName)
                Text("${strings.totalTime}: ${TimeFormatter.formatSeconds(summary.totalSeconds, strings)}")
                Button(onClick = {
                    if (isPro) onShareSummary(summary) else onRequestSummary()
                }) {
                    androidx.compose.material3.Icon(Icons.Default.Share, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(if (isPro) strings.share else strings.upgradeToPro)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SegmentPlanCard(
    index: Int,
    segment: Segment,
    plan: Plan,
    athleteA: Athlete,
    athleteB: Athlete?,
    strings: Strings,
    onAssignAthlete: (Int, String) -> Unit
) {
    val seconds = EstimationEngine.estimateSegmentSeconds(segment, index, athleteA, athleteB, plan)
    val label = when (segment) {
        is Segment.Run -> "${strings.runLabel} ${strings.formatDistance(segment.distanceKm)}"
        is Segment.Station -> segment.name
    }
    val icon = when (segment) {
        is Segment.Run -> Icons.Default.DirectionsRun
        is Segment.Station -> when (segment.category) {
            com.splitforge.app.domain.StationCategory.ENGINE -> Icons.Default.Bolt
            com.splitforge.app.domain.StationCategory.STRENGTH -> Icons.Default.FitnessCenter
            com.splitforge.app.domain.StationCategory.MIXED -> Icons.Default.AutoAwesome
        }
    }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                androidx.compose.material3.Icon(icon, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("${index + 1}. $label", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                Text(TimeFormatter.formatSeconds(seconds, strings))
            }
            if (segment is Segment.Station) {
                if (plan.isDoubles && athleteB != null) {
                    SingleChoiceSegmentedButtonRow {
                        SegmentedButton(
                            selected = plan.stationAssignments[index] == athleteA.id,
                            onClick = { onAssignAthlete(index, athleteA.id) }
                        ) { Text(athleteA.name) }
                        SegmentedButton(
                            selected = plan.stationAssignments[index] == athleteB.id,
                            onClick = { onAssignAthlete(index, athleteB.id) }
                        ) { Text(athleteB.name) }
                    }
                } else {
                    Text("${strings.assignTo}: ${athleteA.name}")
                }
            }
        }
    }
}
