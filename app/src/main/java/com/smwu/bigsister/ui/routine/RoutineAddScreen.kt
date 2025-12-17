package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.ui.components.FigmaInput
import com.smwu.bigsister.ui.components.PrimaryButton
import com.smwu.bigsister.ui.components.RoutineSummaryCard
import com.smwu.bigsister.ui.components.StepCard
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.viewModel.LoginViewModel
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddScreen(
    routineId: Long? = null,
    onNavigateBack: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel(),
    loginViewModel: LoginViewModel = hiltViewModel() // ✅ 유저 ID 얻기 위해 추가
) {
    val state by viewModel.editState.collectAsState()

    // 현재 로그인된 유저 ID
    val currentUser by loginViewModel.currentUser.collectAsState()

    // 초기화
    LaunchedEffect(routineId) {
        viewModel.initEditState(routineId)
    }

    /* 장소 검색용 상태 */
    var activeSearchStepId by remember { mutableStateOf<Long?>(null) }
    var activeSearchType by remember { mutableStateOf<String?>(null) } // "FROM" or "TO"

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (routineId == null) "루틴 만들기" else "루틴 수정") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        },
        bottomBar = {
            Box(Modifier.padding(16.dp)) {
                PrimaryButton(
                    text = "저장하기",
                    onClick = {
                        // ✅ 저장 시 유저 ID 전달
                        val uid = currentUser?.uid ?: ""
                        viewModel.saveRoutine(uid) {
                            onNavigateBack()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(56.dp)
                )
            }
        },
        containerColor = Color.White
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
        ) {
            LazyColumn(
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                // 1. 루틴 제목 입력
                item {
                    FigmaInput(
                        value = state.title,
                        onValueChange = { viewModel.updateTitle(it) },
                        placeholder = "루틴 이름을 입력해주세요 (예: 학교 가기)"
                    )
                }

                // 2. 단계 리스트
                itemsIndexed(state.steps) { index, step ->
                    StepCard(
                        step = step,
                        viewModel = viewModel,
                        onDelete = { viewModel.removeStep(step) },
                        onSearch = { type ->
                            // 검색 창 띄우기
                            activeSearchStepId = step.id
                            activeSearchType = type
                        }
                    )
                }

                // 3. 단계 추가 버튼
                item {
                    Button(
                        onClick = { viewModel.addStep() },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFF3F3F5), contentColor = Color.Black)
                    ) {
                        Text("+ 단계 추가")
                    }
                }

                // 4. 요약 카드
                item {
                    RoutineSummaryCard(state)
                }
            }
        }

        // 역 검색 다이얼로그 (이전 코드와 동일)
        if (activeSearchStepId != null && activeSearchType != null) {
            Dialog(onDismissRequest = { activeSearchStepId = null; activeSearchType = null }) {
                StationSearchScreen(
                    onDismiss = { activeSearchStepId = null; activeSearchType = null },
                    onStationSelected = { station ->
                        val step = state.steps.firstOrNull { it.id == activeSearchStepId } ?: return@StationSearchScreen
                        val value = "${station.stationName}|${station.x},${station.y}"

                        val updated = if (activeSearchType == "FROM") step.copy(from = value) else step.copy(to = value)
                        viewModel.updateStep(updated)
                        if (updated.from != null && updated.to != null) viewModel.calculateDuration(updated)

                        activeSearchStepId = null
                        activeSearchType = null
                    }
                )
            }
        }
    }
}