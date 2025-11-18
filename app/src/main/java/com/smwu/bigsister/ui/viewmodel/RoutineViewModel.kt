package com.smwu.bigsister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.repository.MapRepository // ✅ [추가]
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
    val routineId: Int? = null,
    val title: String = "",
    val steps: List<StepEntity> = emptyList(),
    val isLoading: Boolean = false
)

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val mapRepository: MapRepository // ✅ [추가] MapRepository 주입
) : ViewModel() {

    // --- '루틴 탭' (No. 5) 용 ---
    val routineListWithSteps: Flow<List<RoutineWithSteps>> = routineRepository.routinesWithSteps

    fun deleteRoutine(routine: RoutineEntity) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routine)
        }
    }

    // --- '루틴 생성/수정 탭' (No. 6) 용 ---
    private val _editState = MutableStateFlow(RoutineEditState())
    val editState: StateFlow<RoutineEditState> = _editState.asStateFlow()

    fun loadRoutineForEdit(id: Int?) {
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

    // ✅ [수정] '일반 단계' 추가
    fun addBlankStep() {
        val newStep = StepEntity(
            id = System.currentTimeMillis().toInt(),
            routineId = 0,
            icon = "icon_default",
            name = "새 단계",
            duration = 10,
            stepOrder = _editState.value.steps.size + 1,
            memo = null,
            isTransport = false, // ✅ false
            from = null,
            to = null,
            transportMode = null,
            calculatedDuration = null
        )
        _editState.update { it.copy(steps = it.steps + newStep) }
    }

    // ✅ [신규] '이동 단계' 추가 (PDF 12페이지)
    fun addMovementStep() {
        val newStep = StepEntity(
            id = System.currentTimeMillis().toInt(),
            routineId = 0,
            icon = "icon_transport",
            name = "이동",
            duration = 20, // 임시
            stepOrder = _editState.value.steps.size + 1,
            memo = null,
            isTransport = true, // ✅ true
            from = "127.0276,37.4979", // 임시: 강남역
            to = "126.9780,37.5665", // 임시: 시청역
            transportMode = "driving", // 임시
            calculatedDuration = null
        )
        _editState.update { it.copy(steps = it.steps + newStep) }
    }

    fun removeStep(step: StepEntity) {
        _editState.update { it.copy(steps = it.steps - step) }
    }

    fun updateStep(step: StepEntity) {
        _editState.update { state ->
            val updatedSteps = state.steps.map {
                if (it.id == step.id) step else it
            }
            state.copy(steps = updatedSteps)
        }
    }

    // ✅ [신규] '예상 시간 계산' 기능 (PDF 12페이지 6-3)
    fun calculateDuration(step: StepEntity) {
        if (!step.isTransport || step.from == null || step.to == null) return

        viewModelScope.launch {
            // TODO: 'from'/'to'에 있는 장소 이름을 경도/위도로 변환하는 로직 필요
            // (지금은 경도/위도가 이미 입력되었다고 가정)
            val duration = mapRepository.getExpectedDuration(step.from!!, step.to!!)

            if (duration > 0) {
                // API 호출 성공 시, 'duration'과 'calculatedDuration'을 업데이트
                val updatedStep = step.copy(
                    duration = duration,
                    calculatedDuration = duration
                )
                updateStep(updatedStep)
            } else {
                // TODO: API 실패 시 에러 처리
            }
        }
    }

    fun saveRoutine(onFinished: () -> Unit) {
        viewModelScope.launch {
            val state = _editState.value
            val routine = RoutineEntity(
                id = state.routineId ?: 0,
                title = state.title,
                createdAt = System.currentTimeMillis()
            )
            routineRepository.saveRoutineWithSteps(routine, state.steps)
            onFinished()
        }
    }
}