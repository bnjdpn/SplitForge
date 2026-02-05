package com.splitforge.app.ui

import com.splitforge.app.AppDependencies
import com.splitforge.app.data.AppStateRepository
import com.splitforge.app.data.BillingRepository
import com.splitforge.app.data.BillingState
import com.splitforge.app.data.DefaultDataFactory
import com.splitforge.app.domain.AppState
import com.splitforge.app.domain.Athlete
import com.splitforge.app.domain.AutoPlanEngine
import com.splitforge.app.domain.EstimationEngine
import com.splitforge.app.domain.Plan
import com.splitforge.app.domain.Segment
import com.splitforge.app.domain.Template
import com.splitforge.app.util.IdGenerator
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AppViewModel(private val dependencies: AppDependencies) {
    private val repository: AppStateRepository = dependencies.appStateRepository
    private val billingRepository: BillingRepository = dependencies.billingRepository
    private val scope = MainScope()

    val appState: StateFlow<AppState> = repository.state
    val billingState: StateFlow<BillingState> = billingRepository.state

    private val _previousTotal = MutableStateFlow<Int?>(null)
    val previousTotal: StateFlow<Int?> = _previousTotal.asStateFlow()

    init {
        scope.launch {
            billingRepository.state.collect { billing ->
                val cached = repository.state.value.isProCached
                if (billing.isPro != cached) {
                    repository.update { it.copy(isProCached = billing.isPro) }
                }
            }
        }
    }

    fun refreshBilling() {
        scope.launch { billingRepository.refresh() }
    }

    fun purchasePro() {
        scope.launch { billingRepository.purchasePro() }
    }

    fun restorePurchases() {
        scope.launch { billingRepository.restore() }
    }

    fun addTemplate(): Boolean {
        val state = repository.state.value
        val isPro = billingRepository.state.value.isPro
        if (!isPro && state.templates.size >= 1) {
            return false
        }
        val newTemplate = Template(
            id = IdGenerator.newId(),
            name = dependencies.strings.newTemplateName,
            segments = DefaultDataFactory.defaultTemplate(dependencies.strings).segments
        )
        repository.update { current ->
            val updatedTemplates = current.templates + newTemplate
            current.copy(
                templates = updatedTemplates,
                lastSelectedTemplateId = newTemplate.id,
                lastPlan = defaultPlanFor(newTemplate, current)
            )
        }
        return true
    }

    fun deleteTemplate(templateId: String) {
        repository.update { current ->
            val remaining = current.templates.filterNot { it.id == templateId }
            val finalTemplates = if (remaining.isEmpty()) {
                listOf(DefaultDataFactory.defaultTemplate(dependencies.strings))
            } else {
                remaining
            }
            val selectedId = if (current.lastSelectedTemplateId == templateId) {
                finalTemplates.first().id
            } else {
                current.lastSelectedTemplateId
            }
            current.copy(
                templates = finalTemplates,
                lastSelectedTemplateId = selectedId,
                lastPlan = selectedId?.let { defaultPlanFor(findTemplate(finalTemplates, it), current) }
            )
        }
    }

    fun selectTemplate(templateId: String) {
        repository.update { current ->
            val template = findTemplate(current.templates, templateId)
            val plan = if (current.lastPlan?.templateId == templateId) {
                current.lastPlan
            } else {
                defaultPlanFor(template, current)
            }
            current.copy(lastSelectedTemplateId = templateId, lastPlan = plan)
        }
    }

    fun updateTemplateName(templateId: String, name: String) {
        repository.update { current ->
            current.copy(templates = current.templates.map { if (it.id == templateId) it.copy(name = name) else it })
        }
    }

    fun updateSegment(templateId: String, index: Int, segment: Segment) {
        capturePreviousTotal()
        updateTemplateSegments(templateId) { segments ->
            segments.toMutableList().also { it[index] = segment }
        }
    }

    fun addSegment(templateId: String, segment: Segment) {
        capturePreviousTotal()
        updateTemplateSegments(templateId) { segments ->
            segments + segment
        }
    }

    fun removeSegment(templateId: String, index: Int) {
        capturePreviousTotal()
        updateTemplateSegments(templateId) { segments ->
            segments.toMutableList().also { if (index in it.indices) it.removeAt(index) }
        }
    }

    fun moveSegment(templateId: String, index: Int, delta: Int) {
        capturePreviousTotal()
        updateTemplateSegments(templateId) { segments ->
            val list = segments.toMutableList()
            val newIndex = index + delta
            if (index !in list.indices || newIndex !in list.indices) return@updateTemplateSegments list
            val item = list.removeAt(index)
            list.add(newIndex, item)
            list
        }
    }

    private fun updateTemplateSegments(templateId: String, transform: (List<Segment>) -> List<Segment>) {
        repository.update { current ->
            val oldTemplate = findTemplate(current.templates, templateId)
            val newSegments = transform(oldTemplate.segments)
            val updatedTemplate = oldTemplate.copy(segments = newSegments)
            val updatedTemplates = current.templates.map { if (it.id == templateId) updatedTemplate else it }
            val updatedPlan = remapPlan(current, oldTemplate, updatedTemplate)
            current.copy(templates = updatedTemplates, lastPlan = updatedPlan)
        }
    }

    fun updateAthleteA(update: Athlete) {
        capturePreviousTotal()
        repository.update { current ->
            current.copy(athleteA = update, lastPlan = ensurePlan(current.copy(athleteA = update)))
        }
    }

    fun updateAthleteB(update: Athlete) {
        capturePreviousTotal()
        repository.update { current ->
            current.copy(athleteB = update, lastPlan = ensurePlan(current.copy(athleteB = update)))
        }
    }

    fun setDoubles(isDoubles: Boolean) {
        capturePreviousTotal()
        repository.update { current ->
            val updated = current.copy(isDoubles = isDoubles)
            updated.copy(lastPlan = ensurePlan(updated))
        }
    }

    fun updateAssignment(segmentIndex: Int, athleteId: String) {
        capturePreviousTotal()
        repository.update { current ->
            val plan = ensurePlan(current)
            val updatedAssignments = plan.stationAssignments.toMutableMap()
            updatedAssignments[segmentIndex] = athleteId
            val updatedPlan = plan.copy(stationAssignments = updatedAssignments)
            current.copy(lastPlan = updatedPlan)
        }
    }

    fun suggestSplit() {
        capturePreviousTotal()
        repository.update { current ->
            val template = selectedTemplate(current)
            val athleteB = current.athleteB ?: return@update current
            val assignments = AutoPlanEngine.suggestAssignments(template, current.athleteA, athleteB)
            val plan = ensurePlan(current).copy(stationAssignments = assignments)
            current.copy(lastPlan = plan)
        }
    }

    private fun capturePreviousTotal() {
        val current = repository.state.value
        val template = selectedTemplate(current)
        val plan = ensurePlan(current)
        val total = EstimationEngine.estimateTotalSeconds(template, current.athleteA, current.athleteB, plan)
        _previousTotal.value = total
    }

    fun resetData() {
        repository.reset()
    }

    fun clear() {
        scope.cancel()
    }

    private fun ensurePlan(state: AppState): Plan {
        val template = selectedTemplate(state)
        val existing = state.lastPlan
        val plan = if (existing != null && existing.templateId == template.id) {
            existing
        } else {
            defaultPlanFor(template, state)
        }
        return if (state.isDoubles) {
            plan.copy(isDoubles = true)
        } else {
            plan.copy(isDoubles = false, stationAssignments = assignAllToAthleteA(template, state.athleteA.id))
        }
    }

    private fun remapPlan(state: AppState, oldTemplate: Template, newTemplate: Template): Plan {
        val oldPlan = state.lastPlan
        val basePlan = defaultPlanFor(newTemplate, state)
        if (oldPlan == null || oldPlan.templateId != oldTemplate.id) return basePlan
        val oldAssignments = oldTemplate.segments.mapIndexedNotNull { index, segment ->
            if (segment is Segment.Station) oldPlan.stationAssignments[index] ?: state.athleteA.id else null
        }
        val iterator = oldAssignments.iterator()
        val remappedAssignments = mutableMapOf<Int, String>()
        newTemplate.segments.forEachIndexed { index, segment ->
            if (segment is Segment.Station) {
                val assigned = if (iterator.hasNext()) iterator.next() else state.athleteA.id
                remappedAssignments[index] = assigned
            }
        }
        val sanitized = if (state.isDoubles) {
            remappedAssignments
        } else {
            assignAllToAthleteA(newTemplate, state.athleteA.id)
        }
        return basePlan.copy(stationAssignments = sanitized)
    }

    private fun assignAllToAthleteA(template: Template, athleteAId: String): Map<Int, String> {
        return template.segments.mapIndexedNotNull { index, segment ->
            if (segment is Segment.Station) index to athleteAId else null
        }.toMap()
    }

    private fun defaultPlanFor(template: Template, state: AppState): Plan {
        val assignments = assignAllToAthleteA(template, state.athleteA.id)
        return Plan(
            templateId = template.id,
            isDoubles = state.isDoubles,
            stationAssignments = assignments
        )
    }

    private fun selectedTemplate(state: AppState): Template {
        return state.templates.find { it.id == state.lastSelectedTemplateId } ?: state.templates.first()
    }

    private fun findTemplate(templates: List<Template>, templateId: String): Template {
        return templates.find { it.id == templateId } ?: templates.first()
    }
}
