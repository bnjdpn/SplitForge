package com.splitforge.app.ui

import androidx.compose.animation.Crossfade
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Settings
import com.splitforge.app.AppDependencies
import com.splitforge.app.domain.Template
import com.splitforge.app.ui.screens.AthletesScreen
import com.splitforge.app.ui.screens.PaywallDialog
import com.splitforge.app.ui.screens.PlannerScreen
import com.splitforge.app.ui.screens.SettingsScreen
import com.splitforge.app.ui.screens.SummaryScreen
import com.splitforge.app.ui.screens.TemplatesScreen
import com.splitforge.app.ui.strings.Strings
import com.splitforge.app.ui.theme.AppGradient
import kotlinx.coroutines.launch

private enum class AppTab(val icon: ImageVector) {
    Templates(Icons.Default.List),
    Athletes(Icons.Default.Person),
    Planner(Icons.Default.Schedule),
    Settings(Icons.Default.Settings)
}

@Composable
fun SplitForgeApp(dependencies: AppDependencies) {
    val viewModel = remember { AppViewModel(dependencies) }
    val state by viewModel.appState.collectAsState()
    val billingState by viewModel.billingState.collectAsState()
    val previousTotal by viewModel.previousTotal.collectAsState()
    val strings = dependencies.strings
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var currentTab by remember { mutableStateOf(AppTab.Planner) }
    var showPaywall by remember { mutableStateOf(false) }
    var showSummary by remember { mutableStateOf(false) }

    val isPro = billingState.isPro || state.isProCached
    val selectedTemplate = state.templates.find { it.id == state.lastSelectedTemplateId } ?: state.templates.firstOrNull()
    val plan = state.lastPlan

    LaunchedEffect(Unit) {
        viewModel.refreshBilling()
    }

    DisposableEffect(Unit) {
        onDispose { viewModel.clear() }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        bottomBar = {
            NavigationBar {
                AppTab.values().forEach { tab ->
                    NavigationBarItem(
                        selected = currentTab == tab,
                        onClick = { currentTab = tab },
                        icon = { androidx.compose.material3.Icon(tab.icon, contentDescription = null) },
                        label = { androidx.compose.material3.Text(tabLabel(tab, strings)) }
                    )
                }
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(AppGradient())
                .padding(padding)
        ) {
            Crossfade(targetState = currentTab, label = "tab-crossfade") { tab ->
                when (tab) {
                AppTab.Templates -> TemplatesScreen(
                    state = state,
                    isPro = isPro,
                    strings = strings,
                    onAddTemplate = {
                        val added = viewModel.addTemplate()
                        if (!added) showPaywall = true
                    },
                    onSelectTemplate = viewModel::selectTemplate,
                    onDeleteTemplate = viewModel::deleteTemplate,
                    onUpdateTemplateName = viewModel::updateTemplateName,
                    onAddSegment = viewModel::addSegment,
                    onUpdateSegment = viewModel::updateSegment,
                    onRemoveSegment = viewModel::removeSegment,
                    onMoveSegment = viewModel::moveSegment,
                    onShowPaywall = { showPaywall = true }
                )
                AppTab.Athletes -> AthletesScreen(
                    state = state,
                    strings = strings,
                    onUpdateAthleteA = viewModel::updateAthleteA,
                    onUpdateAthleteB = viewModel::updateAthleteB,
                    onToggleDoubles = viewModel::setDoubles
                )
                AppTab.Planner -> {
                    if (selectedTemplate != null && plan != null) {
                        PlannerScreen(
                            template = selectedTemplate,
                            plan = plan,
                            athleteA = state.athleteA,
                            athleteB = state.athleteB,
                            isPro = isPro,
                            previousTotalSeconds = previousTotal,
                            strings = strings,
                            onAssignAthlete = viewModel::updateAssignment,
                            onSuggestSplit = {
                                if (isPro) {
                                    viewModel.suggestSplit()
                                } else {
                                    showPaywall = true
                                }
                            },
                            onRequestSummary = {
                                if (isPro) showSummary = true else showPaywall = true
                            },
                            onShareSummary = { summaryData ->
                                scope.launch {
                                    try {
                                        val bytes = dependencies.summaryImageExporter.export(summaryData)
                                        dependencies.shareService.shareImage(bytes, "image/png", "splitforge-summary.png")
                                    } catch (throwable: Throwable) {
                                        snackbarHostState.showSnackbar(strings.exportFailed)
                                    }
                                }
                            }
                        )
                    } else {
                        androidx.compose.material3.Text(
                            text = strings.noTemplateSelected,
                            modifier = Modifier.padding(24.dp)
                        )
                    }
                }
                AppTab.Settings -> SettingsScreen(
                    state = state,
                    billingState = billingState,
                    strings = strings,
                    isPro = isPro,
                    onRestore = viewModel::restorePurchases,
                    onResetData = viewModel::resetData,
                    onShowPaywall = { showPaywall = true }
                )
                }
            }
        }
    }

    if (showPaywall) {
        PaywallDialog(
            strings = strings,
            billingState = billingState,
            onBuy = { viewModel.purchasePro() },
            onRestore = { viewModel.restorePurchases() },
            onDismiss = { showPaywall = false }
        )
    }

    if (showSummary && selectedTemplate != null && plan != null) {
        val summaryData = SummaryBuilder.build(
            template = selectedTemplate,
            plan = plan,
            athleteA = state.athleteA,
            athleteB = state.athleteB,
            strings = strings
        )
        SummaryScreen(
            summary = summaryData,
            strings = strings,
            onShare = { data ->
                scope.launch {
                    try {
                        val bytes = dependencies.summaryImageExporter.export(data)
                        dependencies.shareService.shareImage(bytes, "image/png", "splitforge-summary.png")
                    } catch (throwable: Throwable) {
                        snackbarHostState.showSnackbar(strings.exportFailed)
                    }
                }
            },
            onClose = { showSummary = false }
        )
    }
}

private fun tabLabel(tab: AppTab, strings: Strings): String = when (tab) {
    AppTab.Templates -> strings.tabTemplates
    AppTab.Athletes -> strings.tabAthletes
    AppTab.Planner -> strings.tabPlanner
    AppTab.Settings -> strings.tabSettings
}
