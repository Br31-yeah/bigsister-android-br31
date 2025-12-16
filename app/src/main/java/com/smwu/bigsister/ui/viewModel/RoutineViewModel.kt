package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import com.smwu.bigsister.ui.viewModel.RoutineEditState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    private val _routineListWithSteps =
        MutableStateFlow<List<RoutineWithSteps>>(emptyList())
    val routineListWithSteps = _routineListWithSteps.asStateFlow()

    init {
        viewModelScope.launch {
            routineRepository.getRoutineListWithSteps().collect {
                _routineListWithSteps.value = it
            }
        }
    }

    /* ---------- 편집 상태 ---------- */

    private val _editState = MutableStateFlow(RoutineEditState())
    val editState = _editState.asStateFlow()

    fun loadRoutineForEdit(routineId: Long?) {
        if (routineId == null) {
            _editState.value = RoutineEditState()
            return
        }

        viewModelScope.launch {
            val routine = routineRepository.getRoutineByIdOnce(routineId)
            val steps = stepRepository.getStepsByRoutineOnce(routineId)

            _editState.value = RoutineEditState(
                routineId = routine.id,
                title = routine.title,
                steps = steps
            )
        }
    }

    fun updateTitle(title: String) =
        _editState.update { it.copy(title = title) }

    fun updateStep(step: StepEntity) =
        _editState.update {
            it.copy(steps = it.steps.map { s -> if (s.id == step.id) step else s })
        }

    fun removeStep(step: StepEntity) =
        _editState.update { it.copy(steps = it.steps.filterNot { s -> s.id == step.id }) }

    fun addBlankStep() {
        _editState.update {
            it.copy(
                steps = it.steps + StepEntity(
                    routineId = 0L,
                    name = "",
                    duration = 0
                )
            )
        }
    }

    fun addMovementStep() {
        _editState.update {
            it.copy(
                steps = it.steps + StepEntity(
                    routineId = 0L,
                    name = "이동",
                    duration = 0,
                    isTransport = true
                )
            )
        }
    }

    fun saveRoutine(onFinished: () -> Unit) {
        viewModelScope.launch {
            val s = _editState.value
            routineRepository.saveRoutineWithSteps(
                RoutineEntity(id = s.routineId ?: 0L, title = s.title),
                s.steps
            )
            onFinished()
        }
    }

    /* ---------- 역 검색 ---------- */

    private val _searchResults = MutableStateFlow<List<StationInfo>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun searchStation(query: String) {
        _searchResults.value =
            if (query.isBlank()) emptyList()
            else listOf(
                StationInfo("강남", "2호선", 127.0276, 37.4979),
                StationInfo("역삼", "2호선", 127.0365, 37.5006)
            )
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}