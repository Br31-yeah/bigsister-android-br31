package com.smwu.bigsister.ui.viewModel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.data.repository.MapRepositoryImpl
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import com.smwu.bigsister.ui.viewModel.transit.TransitStepDraft
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
    private val mapRepository: MapRepositoryImpl
) : ViewModel() {

    private val _editState = MutableStateFlow(RoutineEditState())
    val editState = _editState.asStateFlow()

    private val _routineListWithSteps = MutableStateFlow<List<RoutineWithSteps>>(emptyList())
    val routineListWithSteps = _routineListWithSteps.asStateFlow()

    private val _searchResults = MutableStateFlow<List<StationInfo>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var tempStepId = -1L

    init {
        refreshRoutineList()
    }

    // 1. 리스트 조회 시 정렬 보장
    fun refreshRoutineList() {
        viewModelScope.launch {
            routineRepository.getRoutineListWithSteps().collect { list ->
                val sortedList = list.map { routineWithSteps ->
                    routineWithSteps.copy(
                        steps = routineWithSteps.steps.sortedBy { it.orderIndex }
                    )
                }
                _routineListWithSteps.value = sortedList
            }
        }
    }

    // 2. 저장 시 orderIndex 부여 및 임시 ID 처리
    fun saveRoutine(userId: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _editState.value
            if (state.title.isBlank()) return@launch

            val orderedSteps = state.steps.mapIndexed { index, step ->
                step.copy(
                    id = if (step.id < 0) 0L else step.id, // 임시 ID 제거
                    orderIndex = index,
                    routineId = state.routineId ?: 0L
                )
            }

            val totalSum = orderedSteps.sumOf { it.calculatedDuration ?: it.baseDuration }
            val routine = RoutineEntity(
                id = state.routineId ?: 0L,
                userId = userId,
                title = state.title,
                totalDuration = totalSum
            )

            routineRepository.saveRoutineWithSteps(userId, routine, orderedSteps)
            onFinished()
        }
    }

    // 3. 로드 시 리셋 방지 (핵심!)
    fun loadRoutineForEdit(id: Long?) {
        // ⭐ 중요: 이미 편집 중인 데이터가 있다면 (예: 제목이 있거나 스텝이 있으면) 다시 로드하지 않음
        // 이렇게 해야 경로 검색 후 돌아왔을 때 입력 중이던 일반 스텝이 사라지지 않습니다.
        if (_editState.value.routineId == id && (_editState.value.steps.isNotEmpty() || _editState.value.title.isNotEmpty())) {
            return
        }

        if (id == null) {
            _editState.value = RoutineEditState()
            return
        }

        viewModelScope.launch {
            val data = routineRepository.getRoutineWithSteps(id)
            data?.let {
                _editState.value = RoutineEditState(
                    routineId = it.routine.id,
                    title = it.routine.title,
                    steps = it.steps.sortedBy { it.orderIndex }
                )
            }
        }
    }

    // 4. 경로 검색 결과 반영 시 순서 유지
    fun addTransitStepFromDraft(draft: TransitStepDraft) {
        _editState.update { state ->
            val newStep = StepEntity(
                id = tempStepId--,
                routineId = state.routineId ?: 0L,
                name = "이동",
                baseDuration = draft.baseDuration,
                isTransport = true,
                from = "${draft.fromName}|${draft.fromLatLng}",
                to = "${draft.toName}|${draft.toLatLng}",
                transportMode = draft.transportMode,
                baseDepartureTime = draft.baseDepartureTime
            )

            val currentSteps = state.steps.toMutableList()
            // 가장 마지막으로 터치했던 이동 단계를 찾아서 교체하거나, 없으면 추가
            val idx = currentSteps.indexOfFirst { it.isTransport && it.from == null && it.to == null }
                .takeIf { it != -1 } ?: currentSteps.indexOfFirst { it.isTransport }

            if (idx != -1) {
                currentSteps[idx] = newStep
            } else {
                currentSteps.add(newStep)
            }
            state.copy(steps = currentSteps)
        }
    }

    fun updateTitle(t: String) { _editState.update { it.copy(title = t) } }
    fun addBlankStep() { _editState.update { it.copy(steps = it.steps + StepEntity(id = tempStepId--, routineId = 0, name = "", baseDuration = 0)) } }
    fun addMovementStep() { _editState.update { it.copy(steps = it.steps + StepEntity(id = tempStepId--, routineId = 0, name = "이동", baseDuration = 0, isTransport = true)) } }
    fun updateStep(s: StepEntity) { _editState.update { state -> state.copy(steps = state.steps.map { if (it.id == s.id) s else it }) } }
    fun removeStep(s: StepEntity) { _editState.update { state -> state.copy(steps = state.steps.filter { it.id != s.id }) } }
    fun updateStepLocation(idx: Int, type: String, v: String) { _editState.update { s -> val list = s.steps.toMutableList(); if (idx in list.indices) list[idx] = if (type == "FROM") list[idx].copy(from = v) else list[idx].copy(to = v); s.copy(steps = list) } }
    fun clearSearchResults() { _searchResults.value = emptyList() }
    fun deleteRoutine(id: Long) { viewModelScope.launch { routineRepository.deleteRoutineById(id) } }

    fun updateStepWithCurrentLocation(index: Int, type: String) {
        viewModelScope.launch {
            val lat = 37.5463; val lng = 126.9647
            val addressName = mapRepository.getAddressFromLatLng(lat, lng)
            updateStepLocation(index, type, "$addressName|$lat,$lng")
        }
    }

    fun searchStation(query: String) {
        viewModelScope.launch {
            _searchResults.value = if (query.isBlank()) emptyList() else mapRepository.searchPlacesByName(query)
        }
    }
}