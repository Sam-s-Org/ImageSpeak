package com.sam.imagespeak.ui

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class AppViewModel : ViewModel() {
    var clickButton by mutableStateOf(true)
    var showHistory by mutableStateOf(false)
    private var _uiState = MutableStateFlow(AppUiState())
    val uiState: StateFlow<AppUiState> = _uiState.asStateFlow()

    fun addHistory(history: String) {
        _uiState.update {
            it.copy(history = it.history.plus(history))
        }
    }
}

data class AppUiState (
    var history: List<String> = emptyList(),
)