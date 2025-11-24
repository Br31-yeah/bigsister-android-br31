package com.smwu.bigsister.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.data.repository.RoutineRepository
import com.smwu.bigsister.data.repository.StepRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import javax.inject.Inject

@HiltViewModel
class RoutineViewModel @Inject constructor(
    private val routineRepository: RoutineRepository,
    private val stepRepository: StepRepository
) : ViewModel() {

    /** 모든 루틴 목록 */
    val routines: StateFlow<List<RoutineEntity>> =
        routineRepository.getAllRoutines()
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    // routineId별 Step 리스트 Flow 캐싱
    private val stepFlows: MutableMap<Long, StateFlow<List<StepEntity>>> = mutableMapOf()

    /** 특정 루틴의 스텝 가져오기 (routineId 당 하나의 Flow만 유지) */
    fun getSteps(routineId: Long): StateFlow<List<StepEntity>> {
        return stepFlows.getOrPut(routineId) {
            stepRepository.getStepsByRoutineId(routineId)
                .stateIn(
                    scope = viewModelScope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }

    /** 루틴 저장 (생성/수정) */
    fun saveRoutine(routine: RoutineEntity, steps: List<StepEntity>) {
        viewModelScope.launch {
            routineRepository.saveRoutineWithSteps(routine, steps)
        }
    }

    /** 루틴 삭제 */
    fun deleteRoutine(routineId: Long) {
        viewModelScope.launch {
            routineRepository.deleteRoutine(routineId)
        }
    }

    /** 선택한 날짜 상태 관리 */
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    val selectedDate: StateFlow<LocalDate> = _selectedDate

    fun setSelectedDate(date: LocalDate) {
        _selectedDate.value = date
    }
}