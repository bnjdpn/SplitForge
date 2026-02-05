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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.splitforge.app.domain.AppState
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.StationCategory
import com.splitforge.app.domain.Template
import com.splitforge.app.ui.strings.Strings
import kotlin.math.roundToInt

@Composable
fun TemplatesScreen(
    state: AppState,
    isPro: Boolean,
    strings: Strings,
    onAddTemplate: () -> Unit,
    onSelectTemplate: (String) -> Unit,
    onDeleteTemplate: (String) -> Unit,
    onUpdateTemplateName: (String, String) -> Unit,
    onAddSegment: (String, Segment) -> Unit,
    onUpdateSegment: (String, Int, Segment) -> Unit,
    onRemoveSegment: (String, Int) -> Unit,
    onMoveSegment: (String, Int, Int) -> Unit,
    onShowPaywall: () -> Unit
) {
    val selectedTemplate = state.templates.find { it.id == state.lastSelectedTemplateId } ?: state.templates.first()
    var deleteTemplateId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
            Text(text = strings.tabTemplates, modifier = Modifier.weight(1f), style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
            Button(onClick = {
                if (!isPro && state.templates.size >= 1) onShowPaywall() else onAddTemplate()
            }) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.addTemplate)
            }
        }

        if (!isPro && state.templates.size >= 1) {
            Card(colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.secondary)) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(strings.templateLimitTitle, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(strings.templateLimitBody, style = androidx.compose.material3.MaterialTheme.typography.bodyMedium)
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedButton(onClick = onShowPaywall) { Text(strings.upgradeToPro) }
                }
            }
        }

        state.templates.forEach { template ->
            TemplateCard(
                template = template,
                strings = strings,
                isSelected = template.id == selectedTemplate.id,
                onSelect = { onSelectTemplate(template.id) },
                onDelete = { deleteTemplateId = template.id }
            )
        }

        TemplateEditor(
            template = selectedTemplate,
            strings = strings,
            onUpdateTemplateName = onUpdateTemplateName,
            onAddSegment = onAddSegment,
            onUpdateSegment = onUpdateSegment,
            onRemoveSegment = onRemoveSegment,
            onMoveSegment = onMoveSegment
        )
    }

    deleteTemplateId?.let { templateId ->
        AlertDialog(
            onDismissRequest = { deleteTemplateId = null },
            confirmButton = {
                TextButton(onClick = {
                    onDeleteTemplate(templateId)
                    deleteTemplateId = null
                }) { Text(strings.confirm) }
            },
            dismissButton = {
                TextButton(onClick = { deleteTemplateId = null }) { Text(strings.cancel) }
            },
            title = { Text(strings.deleteTemplate) },
            text = { Text(strings.deleteTemplateConfirm) }
        )
    }
}

@Composable
private fun TemplateCard(
    template: Template,
    strings: Strings,
    isSelected: Boolean,
    onSelect: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) androidx.compose.material3.MaterialTheme.colorScheme.primary.copy(alpha = 0.12f)
            else androidx.compose.material3.MaterialTheme.colorScheme.surface
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(template.name, style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(6.dp))
                Text("${strings.segments}: ${template.segments.size}")
            }
            OutlinedButton(onClick = onSelect) { Text(strings.editTemplate) }
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onDelete) { Icon(Icons.Default.Delete, contentDescription = strings.deleteTemplate) }
        }
    }
}

@Composable
private fun TemplateEditor(
    template: Template,
    strings: Strings,
    onUpdateTemplateName: (String, String) -> Unit,
    onAddSegment: (String, Segment) -> Unit,
    onUpdateSegment: (String, Int, Segment) -> Unit,
    onRemoveSegment: (String, Int) -> Unit,
    onMoveSegment: (String, Int, Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(strings.editTemplate, style = androidx.compose.material3.MaterialTheme.typography.titleLarge)
            OutlinedTextField(
                value = template.name,
                onValueChange = { onUpdateTemplateName(template.id, it) },
                label = { Text(strings.templateName) },
                modifier = Modifier.fillMaxWidth()
            )
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ElevatedButton(onClick = {
                    onAddSegment(template.id, Segment.Run(distanceKm = 1.0))
                }) { Text(strings.addRun) }
                ElevatedButton(onClick = {
                    val stationIndex = template.segments.count { it is Segment.Station } + 1
                    onAddSegment(
                        template.id,
                        Segment.Station(
                            name = strings.stationName(stationIndex),
                            category = StationCategory.ENGINE,
                            difficulty = 3,
                            baseTimeSeconds = 180
                        )
                    )
                }) { Text(strings.addStation) }
            }
            template.segments.forEachIndexed { index, segment ->
                SegmentEditorCard(
                    segment = segment,
                    index = index,
                    strings = strings,
                    onUpdate = { updated -> onUpdateSegment(template.id, index, updated) },
                    onRemove = { onRemoveSegment(template.id, index) },
                    onMoveUp = { onMoveSegment(template.id, index, -1) },
                    onMoveDown = { onMoveSegment(template.id, index, 1) }
                )
            }
        }
    }
}

@Composable
private fun SegmentEditorCard(
    segment: Segment,
    index: Int,
    strings: Strings,
    onUpdate: (Segment) -> Unit,
    onRemove: () -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("${strings.segments} ${index + 1}", style = androidx.compose.material3.MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.weight(1f))
                IconButton(onClick = onMoveUp) { Icon(Icons.Default.KeyboardArrowUp, contentDescription = strings.moveUp) }
                IconButton(onClick = onMoveDown) { Icon(Icons.Default.KeyboardArrowDown, contentDescription = strings.moveDown) }
                IconButton(onClick = onRemove) { Icon(Icons.Default.Delete, contentDescription = strings.deleteTemplate) }
            }
            when (segment) {
                is Segment.Run -> {
                    val distanceText = if (segment.distanceKm % 1.0 == 0.0) {
                        segment.distanceKm.toInt().toString()
                    } else {
                        segment.distanceKm.toString()
                    }
                    OutlinedTextField(
                        value = distanceText,
                        onValueChange = { value ->
                            val parsed = value.toDoubleOrNull()
                            if (parsed != null && parsed > 0) {
                                onUpdate(segment.copy(distanceKm = parsed))
                            }
                        },
                        label = { Text(strings.distanceKm) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }
                is Segment.Station -> {
                    OutlinedTextField(
                        value = segment.name,
                        onValueChange = { onUpdate(segment.copy(name = it)) },
                        label = { Text(strings.stationLabel) },
                        modifier = Modifier.fillMaxWidth()
                    )
                    StationCategoryPicker(
                        category = segment.category,
                        strings = strings,
                        onChange = { onUpdate(segment.copy(category = it)) }
                    )
                    Column {
                        Text("${strings.difficulty}: ${segment.difficulty}")
                        Slider(
                            value = segment.difficulty.toFloat(),
                            onValueChange = { value ->
                                onUpdate(segment.copy(difficulty = value.roundToInt().coerceIn(1, 5)))
                            },
                            valueRange = 1f..5f,
                            steps = 3
                        )
                    }
                    OutlinedTextField(
                        value = segment.baseTimeSeconds.toString(),
                        onValueChange = { value ->
                            val parsed = value.toIntOrNull()
                            if (parsed != null && parsed > 0) {
                                onUpdate(segment.copy(baseTimeSeconds = parsed))
                            }
                        },
                        label = { Text(strings.baseTimeSeconds) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors()
                    )
                }
            }
        }
    }
}

@Composable
private fun StationCategoryPicker(
    category: StationCategory,
    strings: Strings,
    onChange: (StationCategory) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Column {
        Text(strings.category)
        OutlinedButton(onClick = { expanded = true }) {
            Text(categoryLabel(category, strings))
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            StationCategory.values().forEach { option ->
                DropdownMenuItem(
                    text = { Text(categoryLabel(option, strings)) },
                    onClick = {
                        onChange(option)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun categoryLabel(category: StationCategory, strings: Strings): String = when (category) {
    StationCategory.ENGINE -> strings.categoryEngine
    StationCategory.STRENGTH -> strings.categoryStrength
    StationCategory.MIXED -> strings.categoryMixed
}
