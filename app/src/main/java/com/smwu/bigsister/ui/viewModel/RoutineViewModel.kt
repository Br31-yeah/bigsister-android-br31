package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.data.repository.MapRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val stepRepository: StepRepository,
    private val mapRepository: MapRepository
) : ViewModel() {

    /* ────────────────────────────────
       루틴 목록 (RoutineListScreen)
    ──────────────────────────────── */

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

    /* ────────────────────────────────
       루틴 편집 상태 (RoutineAddScreen)
    ──────────────────────────────── */

    private val _editState = MutableStateFlow(RoutineEditState())
    val editState = _editState.asStateFlow()

    /** UI 전용 임시 Step ID (DB autoGenerate와 충돌 방지용) */
    private var tempStepId = -1L

    fun loadRoutineForEdit(routineId: Long?) {
        if (routineId == null) {
            _editState.value = RoutineEditState()
            tempStepId = -1L
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

    /* ────────────────────────────────
       편집 액션
    ──────────────────────────────── */

    fun updateTitle(title: String) {
        _editState.update { it.copy(title = title) }
    }

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }

    fun updateStep(step: StepEntity) {
        _editState.update {
            it.copy(
                steps = it.steps.map { s ->
                    if (s.id == step.id) step else s
                }
            )
        }
    }

    fun removeStep(step: StepEntity) {
        _editState.update {
            it.copy(
                steps = it.steps.filterNot { s -> s.id == step.id }
            )
        }
    }

    fun addBlankStep() {
        _editState.update {
            it.copy(
                steps = it.steps + StepEntity(
                    id = tempStepId--,                     // ⭐ UI 임시 ID
                    routineId = it.routineId ?: 0L,
                    name = "",
                    duration = 0L,
                    orderIndex = it.steps.size
                )
            )
        }
    }

    fun addMovementStep() {
        _editState.update {
            it.copy(
                steps = it.steps + StepEntity(
                    id = tempStepId--,                     // ⭐ UI 임시 ID
                    routineId = it.routineId ?: 0L,
                    name = "이동",
                    duration = 0L,
                    isTransport = true,
                    orderIndex = it.steps.size
                )
            )
        }
    }

    /* ────────────────────────────────
       저장
    ──────────────────────────────── */

    fun saveRoutine(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _editState.value

            // DB insert용 Step 목록 (id 초기화)
            val stepsForSave = state.steps.map {
                it.copy(id = 0L)
            }

            routineRepository.saveRoutineWithSteps(
                RoutineEntity(
                    id = state.routineId ?: 0L,
                    title = state.title
                ),
                stepsForSave
            )

            onFinished()
        }
    }

    /* ────────────────────────────────
       이동 시간 계산 (ODsay 연동)
    ──────────────────────────────── */

    fun calculateDuration(step: StepEntity) {
        viewModelScope.launch {
            if (!step.isTransport) return@launch

            val from = step.from ?: return@launch
            val to = step.to ?: return@launch

            val duration: Long =
                mapRepository.getExpectedDuration(from, to).toLong()

            updateStep(
                step.copy(
                    calculatedDuration = duration,
                    duration = duration
                )
            )
        }
    }

    /* ────────────────────────────────
       역 검색 (UI용)
    ──────────────────────────────── */

    private val _searchResults =
        MutableStateFlow<List<StationInfo>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    fun searchStation(query: String) {
        viewModelScope.launch {
            _searchResults.value =
                if (query.isBlank()) emptyList()
                else mapRepository.searchStationByName(query)
        }
    }

    fun clearSearchResults() {
        _searchResults.value = emptyList()
    }
}