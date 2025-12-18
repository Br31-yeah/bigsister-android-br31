package com.smwu.bigsister.ui.routine

import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import com.smwu.bigsister.ui.components.*
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.theme.MintConfirm
import com.smwu.bigsister.ui.viewModel.LoginViewModel
import com.smwu.bigsister.ui.viewModel.RoutineViewModel
import com.smwu.bigsister.ui.viewModel.transit.TransitStepDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddScreen(
    routineId: Long?,
    onNavigateBack: () -> Unit,
    // ✅ 1번 브랜치: 구글 길찾기 파라미터 유지 (6개 인자)
    onNavigateToTransit: (String, String, String, String, String, String) -> Unit,
    navController: NavController,
    viewModel: RoutineViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    /* ---------------- 1. 초기 로딩 및 결과 수신 ---------------- */
    LaunchedEffect(routineId) {
        viewModel.loadRoutineForEdit(routineId)
    }

    // ✅ 1번 브랜치: TransitRouteScreen에서 돌아온 데이터 반영 로직
    val navBackStackEntry = navController.currentBackStackEntry
    val transitResultState = navBackStackEntry?.savedStateHandle
        ?.getStateFlow<TransitStepDraft?>("selected_transit_draft", null)
        ?.collectAsState()

    LaunchedEffect(transitResultState?.value) {
        transitResultState?.value?.let { draft ->
            viewModel.addTransitStepFromDraft(draft)
            navBackStackEntry.savedStateHandle.set("selected_transit_draft", null)
        }
    }

    val state by viewModel.editState.collectAsState()
    val currentUser by loginViewModel.currentUser.collectAsState()

    /* ---------------- 2. 장소 검색 상태 (인덱스 기반) ---------------- */
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
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            /* ---------- 루틴 이름 입력 (2번 UI 적용) ---------- */
            Text("루틴 이름", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))
            FigmaInput(
                value = state.title,
                onValueChange = viewModel::updateTitle,
                placeholder = "루틴 이름을 입력하세요 (예: 출근 루틴)"
            )

            Spacer(Modifier.height(24.dp))
            Text("단계 설정", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            Spacer(Modifier.height(8.dp))

            /* ---------- 단계 리스트 (1번 로직 + 2번 요약 카드) ---------- */
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 20.dp)
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
                            viewModel.updateStepWithCurrentLocation(index, type)
                        },
                        onSelectTransitRoute = { mode ->
                            val from = step.from ?: ""
                            val to = step.to ?: ""
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

                // ✅ 2번 브랜치: 전체 시간을 보여주는 요약 카드 추가
                item {
                    RoutineSummaryCard(state = state)
                }
            }

            /* ---------- 저장 버튼 (2번의 유효성 검사 적용) ---------- */
            Button(
                onClick = {
                    // 유효성 검사 로직
                    if (state.title.isBlank()) {
                        Toast.makeText(context, "루틴 이름을 입력해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    if (state.steps.isEmpty()) {
                        Toast.makeText(context, "최소 1개 이상의 단계를 추가해주세요.", Toast.LENGTH_SHORT).show()
                        return@Button
                    }

                    val uid = currentUser?.uid ?: ""
                    viewModel.saveRoutine(uid, onNavigateBack)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintConfirm)
            ) {
                Text("루틴 저장하기", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            Spacer(Modifier.height(20.dp))
        }

        /* ---------- 장소 검색 다이얼로그 (1번 인덱스 방식) ---------- */
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
                        // "장소명|위도,경도" 포맷으로 저장 (1번 브랜치 규격)
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