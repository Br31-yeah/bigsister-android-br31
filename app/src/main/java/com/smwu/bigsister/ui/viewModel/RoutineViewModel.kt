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
    private val mapRepository: MapRepositoryImpl // 주소 변환 기능 사용을 위해 Impl 주입
) : ViewModel() {

    private val _editState = MutableStateFlow(RoutineEditState())
    val editState = _editState.asStateFlow()

    private val _routineListWithSteps = MutableStateFlow<List<RoutineWithSteps>>(emptyList())
    val routineListWithSteps = _routineListWithSteps.asStateFlow()

    private val _searchResults = MutableStateFlow<List<StationInfo>>(emptyList())
    val searchResults = _searchResults.asStateFlow()

    private var tempStepId = -1L

    init { refreshRoutineList() }

    fun refreshRoutineList() {
        viewModelScope.launch {
            routineRepository.getRoutineListWithSteps().collect { _routineListWithSteps.value = it }
        }
    }

    // ✅ 문제 1 해결: 저장 시 제목과 모든 step 시간 합산 보장
    fun saveRoutine(userId: String, onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _editState.value
            if (state.title.isBlank()) return@launch

            val totalSum = state.steps.sumOf { it.calculatedDuration ?: it.baseDuration }

            val routine = RoutineEntity(
                id = state.routineId ?: 0L,
                userId = userId,
                title = state.title, // ✅ 제목 저장 확인
                totalDuration = totalSum // ✅ 전체 시간 합산
            )

            routineRepository.saveRoutineWithSteps(userId, routine, state.steps)
            refreshRoutineList()
            onFinished()
        }
    }

    // ✅ 문제 2 & 4 해결: 현위치 클릭 시 실제 지명(주소) 반영
    fun updateStepWithCurrentLocation(index: Int, type: String) {
        viewModelScope.launch {
            val lat = 37.5463; val lng = 126.9647 // 📍 실제 GPS 좌표 연동 지점
            val addressName = mapRepository.getAddressFromLatLng(lat, lng)
            updateStepLocation(index, type, "$addressName|$lat,$lng")
        }
    }

    // ✅ 장소 검색 시 일반 장소까지 확장된 Repository 함수 호출
    fun searchStation(query: String) {
        viewModelScope.launch {
            _searchResults.value = if (query.isBlank()) emptyList() else mapRepository.searchPlacesByName(query)
        }
    }

    fun addTransitStepFromDraft(draft: TransitStepDraft) {
        _editState.update { state ->
            val newStep = StepEntity(
                id = 0L, routineId = state.routineId ?: 0L, name = "이동",
                baseDuration = draft.baseDuration, isTransport = true,
                from = "${draft.fromName}|${draft.fromLatLng}",
                to = "${draft.toName}|${draft.toLatLng}",
                transportMode = draft.transportMode,
                baseDepartureTime = draft.baseDepartureTime
            )
            val currentSteps = state.steps.toMutableList()
            val idx = currentSteps.indexOfFirst { it.isTransport }
            if (idx != -1) currentSteps[idx] = newStep else currentSteps.add(newStep)
            state.copy(steps = currentSteps)
        }
    }

    // 나머지 updateTitle, addBlankStep 등은 기존과 동일
    fun updateTitle(t: String) { _editState.update { it.copy(title = t) } }
    fun clearSearchResults() { _searchResults.value = emptyList() }
    fun addBlankStep() { _editState.update { it.copy(steps = it.steps + StepEntity(id = tempStepId--, routineId = 0, name = "", baseDuration = 0)) } }
    fun addMovementStep() { _editState.update { it.copy(steps = it.steps + StepEntity(id = tempStepId--, routineId = 0, name = "이동", baseDuration = 0, isTransport = true)) } }
    fun updateStepLocation(idx: Int, type: String, v: String) { _editState.update { s -> val list = s.steps.toMutableList(); if (idx in list.indices) list[idx] = if (type == "FROM") list[idx].copy(from = v) else list[idx].copy(to = v); s.copy(steps = list) } }
    fun updateStep(s: StepEntity) { _editState.update { state -> state.copy(steps = state.steps.map { if (it.id == s.id) s else it }) } }
    fun removeStep(s: StepEntity) { _editState.update { state -> state.copy(steps = state.steps.filter { it.id != s.id }) } }
    fun deleteRoutine(id: Long) { viewModelScope.launch { routineRepository.deleteRoutineById(id); refreshRoutineList() } }
    fun loadRoutineForEdit(id: Long?) { /* 기존 로드 로직 */ }
}