package com.yinzcam.scheduleexercise.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.yinzcam.scheduleexercise.model.ScheduleListItem
import com.yinzcam.scheduleexercise.model.toListItems
import com.yinzcam.scheduleexercise.network.ScheduleRepository
import kotlinx.coroutines.launch

sealed class ScheduleUiState {
    object Loading : ScheduleUiState()
    data class Success(val items: List<ScheduleListItem>, val defaultGameId: Long) : ScheduleUiState()
    data class Error(val message: String) : ScheduleUiState()
}

class ScheduleViewModel(
    private val repository: ScheduleRepository = ScheduleRepository()
) : ViewModel() {

    private val _uiState = MutableLiveData<ScheduleUiState>(ScheduleUiState.Loading)
    val uiState: LiveData<ScheduleUiState> = _uiState

    init {
        loadSchedule()
    }

    fun loadSchedule() {
        _uiState.value = ScheduleUiState.Loading
        viewModelScope.launch {
            repository.fetchSchedule()
                .onSuccess { response ->
                    _uiState.value = ScheduleUiState.Success(
                        items = response.toListItems(),
                        defaultGameId = response.defaultGameId
                    )
                }
                .onFailure { error ->
                    _uiState.value = ScheduleUiState.Error(
                        error.message ?: "Unknown error loading schedule"
                    )
                }
        }
    }
}
