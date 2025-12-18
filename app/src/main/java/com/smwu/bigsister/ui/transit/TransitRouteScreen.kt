package com.smwu.bigsister.ui.transit

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.android.gms.maps.model.LatLng
import com.smwu.bigsister.ui.transit.components.TransitRouteCard
import com.smwu.bigsister.ui.viewModel.transit.TransitRouteUiState
import com.smwu.bigsister.ui.viewModel.transit.TransitRouteViewModel
import com.smwu.bigsister.ui.viewModel.transit.TransitStepDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TransitRouteScreen(
    fromName: String,
    fromLatLng: String,
    toName: String,
    toLatLng: String,
    departureTime: String,
    onBack: () -> Unit,
    onRouteSelected: (TransitStepDraft) -> Unit,
    viewModel: TransitRouteViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedIndex by viewModel.selectedIndex.collectAsState()
    val selectedMode by viewModel.selectedMode.collectAsState()

    // ✅ remember 블록이 LatLng 객체를 정상적으로 반환하도록 수정
    val origin = remember(fromLatLng) {
        val lat = fromLatLng.substringBefore(",").toDoubleOrNull() ?: 0.0
        val lng = fromLatLng.substringAfter(",").toDoubleOrNull() ?: 0.0
        LatLng(lat, lng)
    }

    val destination = remember(toLatLng) {
        val lat = toLatLng.substringBefore(",").toDoubleOrNull() ?: 0.0
        val lng = toLatLng.substringAfter(",").toDoubleOrNull() ?: 0.0
        LatLng(lat, lng)
    }

    LaunchedEffect(origin, destination) {
        viewModel.fetchTransitRoutes(origin, destination)
    }

    Box(Modifier.fillMaxSize().background(Color(0xFFF8F9FA))) {
        Column(Modifier.fillMaxSize()) {
            // ✅ SmallTopAppBar 대신 TopAppBar 사용 (Material 3 표준)
            TopAppBar(
                title = { Text("경로 선택", fontWeight = FontWeight.Bold) },
                actions = {
                    Text(
                        text = "취소",
                        modifier = Modifier
                            .padding(end = 16.dp)
                            .clickable { onBack() },
                        color = Color.Gray
                    )
                }
            )

            // 교통수단 선택 탭
            Row(
                Modifier.fillMaxWidth().padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf("TRANSIT" to "대중교통", "DRIVE" to "자동차", "WALK" to "도보").forEach { (mode, label) ->
                    val isSelected = selectedMode == mode
                    Button(
                        onClick = { viewModel.updateMode(mode, origin, destination) },
                        modifier = Modifier.weight(1f),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = if (isSelected) Color(0xFF8B8FD9) else Color.White,
                            contentColor = if (isSelected) Color.White else Color.Black
                        ),
                        shape = RoundedCornerShape(12.dp),
                        elevation = ButtonDefaults.buttonElevation(2.dp)
                    ) {
                        Text(label, fontSize = 13.sp)
                    }
                }
            }

            when (val state = uiState) {
                TransitRouteUiState.Loading -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        CircularProgressIndicator(color = Color(0xFF8B8FD9))
                    }
                }
                is TransitRouteUiState.Error -> {
                    Box(Modifier.weight(1f).fillMaxWidth(), Alignment.Center) {
                        Text(state.message, color = Color.Red)
                    }
                }
                is TransitRouteUiState.Success -> {
                    LazyColumn(
                        Modifier.weight(1f).fillMaxWidth().padding(horizontal = 16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 100.dp)
                    ) {
                        itemsIndexed(state.routes) { index, route ->
                            TransitRouteCard(
                                route = route,
                                isSelected = selectedIndex == index,
                                onClick = { viewModel.selectRoute(index) }
                            )
                        }
                    }
                }
                else -> {}
            }
        }

        if (selectedIndex != null && uiState is TransitRouteUiState.Success) {
            Surface(
                Modifier.align(Alignment.BottomCenter).fillMaxWidth(),
                shadowElevation = 8.dp,
                color = Color.White
            ) {
                Button(
                    onClick = {
                        val draft = viewModel.getConfirmedDraft(fromName, fromLatLng, toName, toLatLng, departureTime)
                        if (draft != null) onRouteSelected(draft)
                    },
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(54.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2D2D2D))
                ) {
                    Text("이 경로로 단계 추가", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}