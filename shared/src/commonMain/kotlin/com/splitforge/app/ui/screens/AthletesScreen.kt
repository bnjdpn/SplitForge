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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.splitforge.app.domain.AppState
import com.splitforge.app.domain.Athlete
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.util.PaceParser
import kotlin.math.roundToInt

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AthletesScreen(
    state: AppState,
    strings: Strings,
    onUpdateAthleteA: (Athlete) -> Unit,
    onUpdateAthleteB: (Athlete) -> Unit,
    onToggleDoubles: (Boolean) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Text(text = strings.tabAthletes, style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)

        SingleChoiceSegmentedButtonRow {
            SegmentedButton(
                selected = !state.isDoubles,
                onClick = { onToggleDoubles(false) },
                shape = androidx.compose.material3.MaterialTheme.shapes.small
            ) { Text(strings.solo) }
            SegmentedButton(
                selected = state.isDoubles,
                onClick = { onToggleDoubles(true) },
                shape = androidx.compose.material3.MaterialTheme.shapes.small
            ) { Text(strings.doubles) }
        }

        AthleteEditor(
            athlete = state.athleteA,
            title = strings.athleteATitle,
            strings = strings,
            onUpdate = onUpdateAthleteA
        )

        if (state.isDoubles) {
            state.athleteB?.let { athleteB ->
                AthleteEditor(
                    athlete = athleteB,
                    title = strings.athleteBTitle,
                    strings = strings,
                    onUpdate = onUpdateAthleteB
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AthleteEditor(
    athlete: Athlete,
    title: String,
    strings: Strings,
    onUpdate: (Athlete) -> Unit
) {
    var paceText by remember(athlete.runPaceMinPerKm) { mutableStateOf(PaceParser.format(athlete.runPaceMinPerKm)) }
    var paceValid by remember(athlete.runPaceMinPerKm) { mutableStateOf(true) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(title, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = athlete.name,
                onValueChange = { onUpdate(athlete.copy(name = it)) },
                label = { Text(strings.nameLabel) },
                modifier = Modifier.fillMaxWidth()
            )
            OutlinedTextField(
                value = paceText,
                onValueChange = { value ->
                    paceText = value
                    val parsed = PaceParser.parse(value)
                    paceValid = parsed != null && parsed > 0.0
                    if (paceValid && parsed != null) {
                        onUpdate(athlete.copy(runPaceMinPerKm = parsed))
                    }
                },
                label = { Text(strings.runPace) },
                supportingText = { Text(strings.runPaceHint) },
                isError = !paceValid,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
            )
            if (!paceValid) {
                Text(strings.invalidPace, color = androidx.compose.material3.MaterialTheme.colorScheme.error)
            }
            RatingSlider(
                label = strings.engineRating,
                value = athlete.engineRating,
                strings = strings,
                onChange = { onUpdate(athlete.copy(engineRating = it)) }
            )
            RatingSlider(
                label = strings.strengthRating,
                value = athlete.strengthRating,
                strings = strings,
                onChange = { onUpdate(athlete.copy(strengthRating = it)) }
            )
        }
    }
}

@Composable
private fun RatingSlider(
    label: String,
    value: Int,
    strings: Strings,
    onChange: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(modifier = Modifier.fillMaxWidth()) {
            Text(label, modifier = Modifier.weight(1f))
            Text("$value")
        }
        Slider(
            value = value.toFloat(),
            onValueChange = { onChange(it.roundToInt().coerceIn(1, 5)) },
            valueRange = 1f..5f,
            steps = 3
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(strings.ratingRangeHint, style = androidx.compose.material3.MaterialTheme.typography.bodySmall)
    }
}
