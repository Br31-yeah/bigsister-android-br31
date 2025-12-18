package com.smwu.bigsister.ui.viewModel.transit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.smwu.bigsister.data.repository.TransitRouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransitRouteViewModel @Inject constructor(
    private val transitRouteRepository: TransitRouteRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<TransitRouteUiState>(TransitRouteUiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _selectedIndex = MutableStateFlow<Int?>(null)
    val selectedIndex = _selectedIndex.asStateFlow()

    private val _selectedMode = MutableStateFlow("TRANSIT")
    val selectedMode = _selectedMode.asStateFlow()

    fun updateMode(mode: String, origin: LatLng, destination: LatLng) {
        _selectedMode.value = mode
        fetchTransitRoutes(origin, destination, mode)
    }

    fun fetchTransitRoutes(origin: LatLng, destination: LatLng, mode: String = _selectedMode.value) {
        viewModelScope.launch {
            _uiState.value = TransitRouteUiState.Loading
            _selectedIndex.value = null

            try {
                val routes = transitRouteRepository.getTransitRoutes(origin, destination, mode)
                // ✅ 최적 순 정렬: 소요 시간 짧은 순
                val sortedRoutes = routes.sortedBy { it.totalDurationMinutes }
                _uiState.value = TransitRouteUiState.Success(sortedRoutes)
                if (sortedRoutes.isNotEmpty()) _selectedIndex.value = 0
            } catch (e: Exception) {
                _uiState.value = TransitRouteUiState.Error("경로를 불러올 수 없습니다.")
            }
        }
    }

    fun selectRoute(index: Int) { _selectedIndex.value = index }

    fun getConfirmedDraft(fN: String, fL: String, tN: String, tL: String, time: String): TransitStepDraft? {
        val state = _uiState.value
        val index = _selectedIndex.value
        if (state !is TransitRouteUiState.Success || index == null) return null

        val route = state.routes[index]
        return TransitStepDraft(
            name = "이동", fromName = fN, fromLatLng = fL, toName = tN, toLatLng = tL,
            transportMode = _selectedMode.value.lowercase(),
            baseDuration = route.totalDurationMinutes, // ✅ 정확한 분 단위 전달
            baseDepartureTime = time
        )
    }
}