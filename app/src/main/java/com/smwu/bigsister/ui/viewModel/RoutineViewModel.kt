package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.data.repository.MapRepository
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val stepRepository: StepRepository,
    private val mapRepository: MapRepository
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

    // ────────────────────────────
    // 편집 상태
    // ────────────────────────────
    private val _editState = MutableStateFlow(RoutineEditState())
    val editState = _editState.asStateFlow()

    fun initEditState(routineId: Long? = null) {
        if (routineId == null) {
            _editState.value = RoutineEditState()
        } else {
            viewModelScope.launch {
                val routineWithSteps = routineRepository.getRoutineWithSteps(routineId)
                if (routineWithSteps != null) {
                    _editState.value = RoutineEditState(
                        routineId = routineWithSteps.routine.id,
                        title = routineWithSteps.routine.title,
                        steps = routineWithSteps.steps
                    )
                }
            }
        }
    }

    fun updateTitle(newTitle: String) {
        _editState.value = _editState.value.copy(title = newTitle)
    }

    fun addStep() {
        val currentSteps = _editState.value.steps.toMutableList()
        val nextOrder = currentSteps.size

        val newStep = StepEntity(
            routineId = 0,
            name = "새 단계 ${nextOrder + 1}",
            duration = 10L,
            orderIndex = nextOrder
        )
        currentSteps.add(newStep)
        _editState.value = _editState.value.copy(steps = currentSteps)
    }

    fun updateStep(updatedStep: StepEntity) {
        val currentSteps = _editState.value.steps.toMutableList()
        val index = currentSteps.indexOfFirst { it.id == updatedStep.id || (it.id == 0L && it.orderIndex == updatedStep.orderIndex) }

        if (index != -1) {
            currentSteps[index] = updatedStep
            _editState.value = _editState.value.copy(steps = currentSteps)
        }
    }

    fun removeStep(step: StepEntity) {
        val currentSteps = _editState.value.steps.toMutableList()
        currentSteps.remove(step)
        val reordered = currentSteps.mapIndexed { index, s -> s.copy(orderIndex = index) }
        _editState.value = _editState.value.copy(steps = reordered)
    }

    // ────────────────────────────
    // 저장 및 삭제 (수정됨)
    // ────────────────────────────

    fun saveRoutine(userId: String, onComplete: () -> Unit) {
        val state = _editState.value
        if (state.title.isBlank()) return

        viewModelScope.launch {
            val totalDuration = state.steps.sumOf { it.duration }

            val routine = com.smwu.bigsister.data.local.RoutineEntity(
                id = state.routineId ?: 0L,
                userId = userId,
                title = state.title,
                totalDuration = totalDuration,
                isActive = true
            )

            routineRepository.saveRoutineWithSteps(userId, routine, state.steps)
            onComplete()
        }
    }

    // ✅ ID를 받아서 삭제 처리 (Repo의 deleteRoutineById 호출)
    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutineById(routineId)
        }
    }

    // ────────────────────────────
    // 지도 / 이동 시간 계산
    // ────────────────────────────
    fun calculateDuration(step: StepEntity) {
        val from = step.from ?: return
        val to = step.to ?: return
        val mode = step.transportMode ?: return

        val fromCoord = from.substringAfter("|").split(",")
        val toCoord = to.substringAfter("|").split(",")

        if (fromCoord.size < 2 || toCoord.size < 2) return

        val fromLatLng = "${fromCoord[1]},${fromCoord[0]}"
        val toLatLng = "${toCoord[1]},${toCoord[0]}"

        viewModelScope.launch {
            val duration = when (mode) {
                "transit" -> {
                    mapRepository.getExpectedDuration(
                        fromString = from.substringAfter("|"),
                        toString = to.substringAfter("|")
                    )
                }
                "walking", "driving" -> {
                    mapRepository.getWalkingOrDrivingDuration(
                        fromLatLng = fromLatLng,
                        toLatLng = toLatLng,
                        mode = mode
                    )
                }
                else -> 0L
            }

            updateStep(step.copy(calculatedDuration = duration, duration = duration))
        }
    }

    // ────────────────────────────
    // 역 검색
    // ────────────────────────────
    private val _searchResults = MutableStateFlow<List<StationInfo>>(emptyList())
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