package com.smwu.bigsister.ui.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.maps.model.LatLng
import com.smwu.bigsister.data.repository.TransitRouteRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TransitRouteViewModel @Inject constructor(
    private val transitRouteRepository: TransitRouteRepository
) : ViewModel() {

    fun testFetchTransitRoutes() {
        viewModelScope.launch {

            val origin = LatLng(37.5665, 126.9780)      // 서울시청
            val destination = LatLng(37.4979, 127.0276) // 강남역

            val routes = transitRouteRepository.getTransitRoutes(
                origin = origin,
                destination = destination
            )

            Log.d("TransitRouteVM", "routes.size = ${routes.size}")

            routes.forEachIndexed { index, route ->
                Log.d("TransitRouteVM", "──── Route #$index ────")
                Log.d(
                    "TransitRouteVM",
                    "duration = ${route.localizedValues?.duration?.text}"
                )

                route.legs.firstOrNull()?.steps?.forEach { step ->
                    Log.d(
                        "TransitRouteVM",
                        "step mode = ${step.travelMode}, " +
                                "vehicle = ${step.transitDetails?.transitLine?.vehicle?.type}"
                    )
                }
            }
        }
    }
}