package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smwu.bigsister.ui.components.FigmaInput
import com.smwu.bigsister.ui.components.SecondaryButton
import com.smwu.bigsister.ui.components.StepCard
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.viewModel.LoginViewModel
import com.smwu.bigsister.ui.viewModel.RoutineViewModel
import com.smwu.bigsister.ui.viewModel.transit.TransitStepDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddScreen(
    routineId: Long?,
    onNavigateBack: () -> Unit,
    // (fromName, fromLatLng, toName, toLatLng, departureTime, mode) -> Unit
    onNavigateToTransit: (String, String, String, String, String, String) -> Unit,
    navController: NavController,
    viewModel: RoutineViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    // 1. 기존 루틴 로드
    LaunchedEffect(routineId) {
        viewModel.loadRoutineForEdit(routineId)
    }

    // 2. 경로 선택 화면(TransitRouteScreen)에서 돌아온 데이터 감지 및 반영
    val navBackStackEntry = navController.currentBackStackEntry
    val transitResultState = navBackStackEntry?.savedStateHandle
        ?.getStateFlow<TransitStepDraft?>("selected_transit_draft", null)
        ?.collectAsState()

    LaunchedEffect(transitResultState?.value) {
        transitResultState?.value?.let { draft ->
            viewModel.addTransitStepFromDraft(draft) // 루틴 리스트에 추가 또는 업데이트
            // 데이터 소비 후 초기화하여 중복 추가 방지
            navBackStackEntry.savedStateHandle.set("selected_transit_draft", null)
        }
    }

    val state by viewModel.editState.collectAsState()
    val currentUser by loginViewModel.currentUser.collectAsState()

    var activeSearchStepIndex by remember { mutableStateOf<Int?>(null) }
    var activeSearchType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(if (routineId == null) "루틴 생성" else "루틴 수정", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(20.dp)
        ) {
            /* 루틴 제목 입력 */
            FigmaInput(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                placeholder = "루틴 이름을 입력하세요 (예: 출근 루틴)"
            )

            Spacer(Modifier.height(16.dp))

            /* 단계 리스트 */
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(state.steps) { index, step ->
                    StepCard(
                        step = step,
                        viewModel = viewModel,
                        onDelete = { viewModel.removeStep(step) },
                        onSearch = { type ->
                            activeSearchStepIndex = index
                            activeSearchType = type
                        },
                        onCurrentLocation = { type ->
                            // ViewModel에 구현된 현위치 반영 함수 호출
                            viewModel.updateStepWithCurrentLocation(index, type)
                        },
                        onSelectTransitRoute = { mode ->
                            val from = step.from ?: return@StepCard
                            val to = step.to ?: return@StepCard

                            // 경로 선택 화면으로 이동 (6개 인자 전달)
                            onNavigateToTransit(
                                from.substringBefore("|"), from.substringAfter("|"),
                                to.substringBefore("|"), to.substringAfter("|"),
                                step.baseDepartureTime ?: "09:00",
                                mode
                            )
                        }
                    )
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        SecondaryButton(
                            text = "+ 일반 단계",
                            onClick = viewModel::addBlankStep,
                            modifier = Modifier.weight(1f)
                        )
                        SecondaryButton(
                            text = "+ 이동 단계",
                            onClick = viewModel::addMovementStep,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            /* 저장 버튼 */
            Button(
                onClick = {
                    val uid = currentUser?.uid ?: ""
                    viewModel.saveRoutine(uid, onNavigateBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("루틴 저장하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
        }

        /* 출발지/도착지 장소 검색 다이얼로그 */
        if (activeSearchStepIndex != null && activeSearchType != null) {
            Dialog(onDismissRequest = {
                activeSearchStepIndex = null
                activeSearchType = null
            }) {
                StationSearchScreen(
                    viewModel = viewModel,
                    onDismiss = {
                        activeSearchStepIndex = null
                        activeSearchType = null
                    },
                    onStationSelected = { station ->
                        // "장소명|위도,경도" 포맷으로 저장
                        val value = "${station.stationName}|${station.y},${station.x}"
                        viewModel.updateStepLocation(activeSearchStepIndex!!, activeSearchType!!, value)
                        activeSearchStepIndex = null
                        activeSearchType = null
                    }
                )
            }
        }
    }
}