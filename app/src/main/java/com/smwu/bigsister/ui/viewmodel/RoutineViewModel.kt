package com.smwu.bigsister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.data.repository.MapRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RoutineEditState(
    val routineId: Long? = null,
    val title: String = "",
    val steps: List<StepEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    val routineListWithSteps: Flow<List<RoutineWithSteps>> = routineRepository.routinesWithSteps

    private val _searchResults = MutableStateFlow<List<StationInfo>>(emptyList())
    val searchResults: StateFlow<List<StationInfo>> = _searchResults.asStateFlow()

    fun searchStation(name: String) {
        if (name.isBlank()) return
        viewModelScope.launch {
            _searchResults.value = mapRepository.searchStationByName(name)
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch { routineRepository.deleteRoutine(routine) }
    }

    private val _editState = MutableStateFlow(RoutineEditState())
    val editState: StateFlow<RoutineEditState> = _editState.asStateFlow()

    fun loadRoutineForEdit(id: Long?) {
        if (id == null) {
            _editState.value = RoutineEditState()
            return
        }

        viewModelScope.launch {
            _editState.update { it.copy(isLoading = true) }
            val routineWithSteps = routineRepository.getRoutineWithSteps(id)
            if (routineWithSteps != null) {
                _editState.value = RoutineEditState(
                    routineId = routineWithSteps.routine.id,
                    title = routineWithSteps.routine.title,
                    steps = routineWithSteps.steps,
                    isLoading = false
                )
            }
        }
    }

    fun updateTitle(title: String) {
        _editState.update { it.copy(title = title) }
    }

    fun addBlankStep() {
        val newStep = StepEntity(
            id = 0L, // autoGenerate → DB가 채움
            routineId = 0L,
            icon = "icon_default",
            name = "새 단계",
            duration = 10,
            orderIndex = editState.value.steps.size,
        )
        _editState.update { it.copy(steps = it.steps + newStep) }
    }

    fun addMovementStep() {
        val newStep = StepEntity(
            id = 0L,
            routineId = 0L,
            icon = "icon_transport",
            name = "이동",
            duration = 20,
            orderIndex = editState.value.steps.size,
            isTransport = true,
            from = "127.0276,37.4979",
            to = "126.9780,37.5665",
            transportMode = "driving"
        )
        _editState.update { it.copy(steps = it.steps + newStep) }
    }

    fun removeStep(step: StepEntity) {
        _editState.update { it.copy(steps = it.steps - step) }
    }

    fun updateStep(step: StepEntity) {
        _editState.update {
            it.copy(steps = it.steps.map { s -> if (s.id == step.id) step else s })
        }
    }

    fun calculateDuration(step: StepEntity) {
        if (!step.isTransport || step.from == null || step.to == null) return

        viewModelScope.launch {
            val duration = mapRepository.getExpectedDuration(step.from!!, step.to!!)
            if (duration > 0) {
                updateStep(step.copy(
                    duration = duration,
                    calculatedDuration = duration
                ))
            }
        }
    }

    fun saveRoutine(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = editState.value
            val routine = RoutineEntity(
                id = state.routineId ?: 0L,
                title = state.title
            )
            routineRepository.saveRoutineWithSteps(routine, state.steps)
            onFinished()
        }
    }
}