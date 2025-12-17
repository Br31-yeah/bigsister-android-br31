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
       루틴 목록
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
       루틴 편집 상태
    ──────────────────────────────── */
    private val _editState = MutableStateFlow(RoutineEditState())
    val editState = _editState.asStateFlow()

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

    fun updateStep(step: StepEntity) {
        _editState.update {
            it.copy(steps = it.steps.map { s -> if (s.id == step.id) step else s })
        }
    }

    fun removeStep(step: StepEntity) {
        _editState.update {
            it.copy(steps = it.steps.filterNot { s -> s.id == step.id })
        }
    }

    fun addBlankStep() {
        _editState.update {
            it.copy(
                steps = it.steps + StepEntity(
                    id = tempStepId--,
                    routineId = it.routineId ?: 0L,
                    name = "",
                    baseDuration = 0L,
                    orderIndex = it.steps.size
                )
            )
        }
    }

    fun addMovementStep() {
        _editState.update {
            it.copy(
                steps = it.steps + StepEntity(
                    id = tempStepId--,
                    routineId = it.routineId ?: 0L,
                    name = "이동",
                    baseDuration = 0L,
                    isTransport = true,
                    orderIndex = it.steps.size
                )
            )
        }
    }

    /* ────────────────────────────────
       저장 (🔥 userId 통합 포인트)
    ──────────────────────────────── */
    fun saveRoutine(userId: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _editState.value

            val totalDuration =
                state.steps.sumOf { it.calculatedDuration ?: it.baseDuration }

            val routine = RoutineEntity(
                id = state.routineId ?: 0L,
                userId = userId,
                title = state.title,
                totalDuration = totalDuration,
                isActive = true
            )

            routineRepository.saveRoutineWithSteps(
                userId = userId,
                routine = routine,
                steps = state.steps.map { it.copy(id = 0L) }
            )

            onFinished()
        }
    }

    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutineById(routineId)
        }
    }

    /* ────────────────────────────────
       🚍 이동 시간 계산
    ──────────────────────────────── */
    fun calculateDuration(step: StepEntity) {
        if (!step.isTransport) return

        val from = step.from ?: return
        val to = step.to ?: return
        val mode = step.transportMode ?: return

        val fromCoord = from.substringAfter("|").split(",")
        val toCoord = to.substringAfter("|").split(",")

        if (fromCoord.size < 2 || toCoord.size < 2) return

        val fromLatLng = "${fromCoord[1]},${fromCoord[0]}"
        val toLatLng = "${toCoord[1]},${toCoord[0]}"

        viewModelScope.launch {
            val newDuration = when (mode) {
                "transit" ->
                    mapRepository.getExpectedDuration(
                        fromString = from.substringAfter("|"),
                        toString = to.substringAfter("|")
                    )
                "walking", "driving" ->
                    mapRepository.getWalkingOrDrivingDuration(
                        fromLatLng = fromLatLng,
                        toLatLng = toLatLng,
                        mode = mode
                    )
                else -> 0L
            }

            val updated =
                if (step.baseDuration == 0L) {
                    step.copy(
                        baseDuration = newDuration,
                        calculatedDuration = newDuration
                    )
                } else {
                    step.copy(calculatedDuration = newDuration)
                }

            updateStep(updated)
        }
    }

    /* ────────────────────────────────
       역 검색
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