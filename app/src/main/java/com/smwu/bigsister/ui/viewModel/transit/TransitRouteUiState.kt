package com.smwu.bigsister.ui.viewModel.transit

import com.smwu.bigsister.data.model.transit.TransitRouteUiModel

sealed interface TransitRouteUiState {
    object Idle : TransitRouteUiState
    object Loading : TransitRouteUiState

    data class Success(
        val routes: List<TransitRouteUiModel>
    ) : TransitRouteUiState

    data class Error(
        val message: String
    ) : TransitRouteUiState
}