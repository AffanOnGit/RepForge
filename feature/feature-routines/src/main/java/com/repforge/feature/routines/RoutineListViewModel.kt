package com.repforge.feature.routines

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.repforge.core.domain.model.Routine
import com.repforge.core.domain.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineListUiState(
    val searchQuery: String = "",
    val selectedFilterSubMuscle: String? = null,
    val lastDeletedRoutine: Routine? = null,
    val showDeleteUndoSnackbar: Boolean = false
)

@HiltViewModel
class RoutineListViewModel @Inject constructor(
    private val routineRepository: RoutineRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RoutineListUiState())
    val uiState: StateFlow<RoutineListUiState> = _uiState

    val routines: StateFlow<List<Routine>> = combine(
        routineRepository.getAllRoutines(),
        _uiState
    ) { allRoutines, state ->
        allRoutines.filter { routine ->
            val matchesQuery = state.searchQuery.isBlank() ||
                    routine.name.contains(state.searchQuery, ignoreCase = true) ||
                    routine.description.contains(state.searchQuery, ignoreCase = true)
            matchesQuery
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun onSearchQueryChanged(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun deleteRoutine(routine: Routine) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    lastDeletedRoutine = routine,
                    showDeleteUndoSnackbar = true
                )
            }
            routineRepository.deleteRoutine(routine.id)
        }
    }

    fun duplicateRoutine(routine: Routine) {
        viewModelScope.launch {
            routineRepository.duplicateRoutine(routine.id, "${routine.name} (Variation)")
        }
    }

    fun dismissUndoSnackbar() {
        _uiState.update { it.copy(showDeleteUndoSnackbar = false, lastDeletedRoutine = null) }
    }
}
