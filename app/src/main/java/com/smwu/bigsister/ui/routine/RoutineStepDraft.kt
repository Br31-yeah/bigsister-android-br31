package com.smwu.bigsister.ui.routine

import com.smwu.bigsister.ui.viewModel.transit.TransitStepDraft

sealed interface RoutineStepDraft {

    data class Action(
        val name: String,
        val baseDuration: Long,
        val memo: String? = null
    ) : RoutineStepDraft

    data class Transit(
        val draft: TransitStepDraft
    ) : RoutineStepDraft
}