package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.ui.components.FigmaInput
import com.smwu.bigsister.ui.components.SecondaryButton
import com.smwu.bigsister.ui.components.StepCard
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.viewModel.RoutineEditState
import com.smwu.bigsister.ui.components.RoutineSummaryCard
import com.smwu.bigsister.ui.viewModel.RoutineViewModel
import com.smwu.bigsister.ui.theme.MintConfirm

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddScreen(
    routineId: Long?,
    onNavigateBack: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    // 루틴 로딩
    LaunchedEffect(routineId) {
        viewModel.loadRoutineForEdit(routineId)
    }

    val state by viewModel.editState.collectAsState()

    // 장소 검색용 상태
    var activeSearchStepId by remember { mutableStateOf<Long?>(null) }
    var activeSearchType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (routineId == null) "루틴 생성" else "루틴 수정",
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Outlined.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        containerColor = Color.White
    ) { paddingValues ->

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            contentPadding = PaddingValues(bottom = 32.dp)
        ) {

            /* ───────── 루틴 이름 ───────── */
            item {
                Text("루틴 이름", fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                FigmaInput(
                    value = state.title,
                    onValueChange = viewModel::updateTitle,
                    placeholder = "아침 루틴, 출근 준비…"
                )
            }

            /* ───────── 단계 ───────── */
            item {
                Text("단계", fontSize = 16.sp)
            }

            itemsIndexed(
                items = state.steps,
                key = { _, step -> step.id }
            ) { _, step ->
                StepCard(
                    step = step,
                    viewModel = viewModel,
                    onDelete = { viewModel.removeStep(step) },
                    onSearch = { type ->
                        activeSearchStepId = step.id
                        activeSearchType = type
                    }
                )
            }

            /* ───────── 단계 추가 ───────── */
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    SecondaryButton(
                        text = "단계 추가",
                        onClick = viewModel::addBlankStep,
                        modifier = Modifier.weight(1f)
                    )
                    SecondaryButton(
                        text = "이동",
                        onClick = viewModel::addMovementStep,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            /* ───────── 요약 ───────── */
            item {
                RoutineSummaryCard(state = state)
            }

            /* ───────── 저장 ───────── */
            item {
                Button(
                    onClick = { viewModel.saveRoutine(onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MintConfirm,
                        contentColor = Color.White
                    )
                ) {
                    Text(
                        text = if (routineId == null) "루틴 저장" else "수정 완료",
                        fontSize = 18.sp
                    )
                }
            }
        }

        /* ───────── 장소 검색 화면 ───────── */
        if (activeSearchStepId != null && activeSearchType != null) {
            StationSearchScreen(
                onDismiss = {
                    activeSearchStepId = null
                    activeSearchType = null
                },
                onStationSelected = { station ->
                    val step = state.steps.firstOrNull { it.id == activeSearchStepId }
                        ?: return@StationSearchScreen

                    val value = "${station.stationName}|${station.x},${station.y}"

                    val updatedStep =
                        if (activeSearchType == "FROM") {
                            step.copy(from = value)
                        } else {
                            step.copy(to = value)
                        }

                    viewModel.updateStep(updatedStep)

                    if (updatedStep.from != null && updatedStep.to != null) {
                        viewModel.calculateDuration(updatedStep)
                    }

                    activeSearchStepId = null
                    activeSearchType = null
                }
            )
        }
    }
}