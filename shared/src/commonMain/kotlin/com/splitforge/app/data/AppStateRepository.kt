package com.splitforge.app.data

import com.russhwolf.settings.Settings
import com.splitforge.app.domain.AppState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

class AppStateRepository(
    private val settings: Settings,
    private val json: Json,
    private val defaultState: AppState
) {
    private val key = "app_state_json"
    private val _state = MutableStateFlow(load())
    val state: StateFlow<AppState> = _state.asStateFlow()

    private fun load(): AppState {
        val stored = settings.getStringOrNull(key) ?: return defaultState
        return runCatching { json.decodeFromString(AppState.serializer(), stored) }.getOrDefault(defaultState)
    }

    fun update(transform: (AppState) -> AppState) {
        val updated = transform(_state.value)
        _state.value = updated
        settings.putString(key, json.encodeToString(updated))
    }

    fun reset() {
        _state.value = defaultState
        settings.putString(key, json.encodeToString(defaultState))
    }
}
