package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.ui.viewmodel.RoutineEditState
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel

@Composable
fun RoutineAddScreen(
    routineId: Int?,
    viewModel: RoutineViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(key1 = routineId) {
        viewModel.loadRoutineForEdit(routineId)
    }

    val uiState by viewModel.editState.collectAsState()

    Scaffold(
        topBar = {
            RoutineAddTopBar(
                title = if (routineId == null) "루틴 생성" else "루틴 수정",
                onBackClick = onNavigateBack,
                onSaveClick = { viewModel.saveRoutine(onFinished = onNavigateBack) }
            )
        },
        bottomBar = {
            RoutineSummaryBar(state = uiState)
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 루틴 이름 입력
            item {
                Spacer(Modifier.height(4.dp))
                Text("루틴 이름", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = { Text("학교 가기") },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 2. 단계 목록
            itemsIndexed(uiState.steps, key = { _, step -> step.id }) { index, step ->
                Text("단계 ${index + 1}", fontWeight = FontWeight.SemiBold, fontSize = 16.sp)
                Spacer(Modifier.height(8.dp))
                StepCard( // ✅ [수정] ViewModel을 전달하도록 변경
                    step = step,
                    viewModel = viewModel,
                    onStepChanged = { updatedStep -> viewModel.updateStep(updatedStep) },
                    onDeleteClick = { viewModel.removeStep(step) }
                )
            }

            // 3. 단계 추가 버튼
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = { viewModel.addBlankStep() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Text("단계 추가")
                    }
                    Button(
                        onClick = { viewModel.addMovementStep() }, // ✅ [수정] '이동' 단계 추가
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Text("이동")
                    }
                }
                Spacer(Modifier.height(100.dp)) // 하단 바 여백
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddTopBar(
    title: String,
    onBackClick: () -> Unit,
    onSaveClick: () -> Unit
) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        actions = {
            TextButton(onClick = onSaveClick) {
                Text("저장", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

/**
 * 단계 수정 카드 (PDF 1, 4번)
 * [수정됨] isTransport 플래그에 따라 '이동' UI를 분기 처리
 */
@Composable
fun StepCard(
    step: StepEntity,
    viewModel: RoutineViewModel, // ✅ [추가] 시간 계산 호출용
    onStepChanged: (StepEntity) -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            // --- 1. 상단 공통 (아이콘, 이름, 시간, 삭제) ---
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (step.isTransport) Icons.Default.Place else Icons.Default.Star,
                    contentDescription = "단계 아이콘",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                BasicTextField(
                    value = step.name,
                    onValueChange = { onStepChanged(step.copy(name = it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
                // ✅ '이동' 단계이고, API 계산값이 있으면 수동 변경 불가
                BasicTextField(
                    value = step.duration.toString(),
                    onValueChange = { onStepChanged(step.copy(duration = it.toIntOrNull() ?: 0)) },
                    modifier = Modifier.width(40.dp),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp),
                    enabled = !step.isTransport || step.calculatedDuration == null
                )
                Text("분", fontSize = 16.sp)
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제")
                }
            }

            // --- 2. '이동' 단계 전용 UI (PDF 4번) ---
            if (step.isTransport) {
                Spacer(Modifier.height(16.dp))
                // TODO: 실제로는 장소 검색(PDF 2번) 후 경도/위도(String)로 변환해야 함
                OutlinedTextField(
                    value = step.from ?: "",
                    onValueChange = { onStepChanged(step.copy(from = it)) },
                    label = { Text("출발지 (경도,위도)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                OutlinedTextField(
                    value = step.to ?: "",
                    onValueChange = { onStepChanged(step.copy(to = it)) },
                    label = { Text("도착지 (경도,위도)") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(8.dp))
                Button(
                    onClick = { viewModel.calculateDuration(step) }, // ✅ 시간 계산 호출
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = "시간 계산")
                    Text("예상 소요시간 계산")
                }
            }
        }
    }
}

@Composable
fun RoutineSummaryBar(state: RoutineEditState) {
    // (기존 코드와 동일)
    val totalMinutes = state.steps.sumOf { it.duration }
    val totalTimeStr = if (totalMinutes >= 60) {
        "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
    } else {
        "${totalMinutes}분"
    }

    BottomAppBar(
        containerColor = MaterialTheme.colorScheme.surfaceVariant,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("총 소요시간", fontWeight = FontWeight.Bold)
            Text("$totalTimeStr", fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("단계 수", fontWeight = FontWeight.Bold)
            Text("${state.steps.size}개", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}