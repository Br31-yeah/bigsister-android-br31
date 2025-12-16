package com.smwu.bigsister.ui.viewModel

import com.smwu.bigsister.data.local.StepEntity

/**
 * 루틴 생성 / 수정 화면에서 사용하는 UI 상태
 */
data class RoutineEditState(

    /** 수정 중인 루틴 ID (null = 신규 생성) */
    val routineId: Long? = null,

    /** 루틴 제목 */
    val title: String = "",

    /** 루틴 단계 목록 */
    val steps: List<StepEntity> = emptyList()
)