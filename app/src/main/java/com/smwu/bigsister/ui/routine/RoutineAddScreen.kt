package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Search
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
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.ui.map.StationSearchScreen
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

    // ▼▼▼ [추가] 검색 팝업 상태 관리 ▼▼▼
    // 어떤 단계(Step)의 출발지/도착지를 수정 중인지 저장합니다.
    var activeSearchStepId by remember { mutableStateOf<Int?>(null) }
    var activeSearchType by remember { mutableStateOf<String?>(null) } // "FROM" or "TO"

    Scaffold(
        topBar = {
            RoutineAddTopBar(
                title = if (routineId == null) "루틴 생성" else "루틴 수정",
                onBackClick = onNavigateBack,
                onSaveClick = { viewModel.saveRoutine(onFinished = onNavigateBack) }
            )
        },
        bottomBar = { RoutineSummaryBar(state = uiState) }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 루틴 이름
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
                StepCard(
                    step = step,
                    viewModel = viewModel,
                    onStepChanged = { viewModel.updateStep(it) },
                    onDeleteClick = { viewModel.removeStep(step) },
                    // ▼▼▼ 검색 버튼 클릭 시 팝업 열기 ▼▼▼
                    onSearchClick = { type ->
                        activeSearchStepId = step.id
                        activeSearchType = type
                    }
                )
            }

            // 3. 버튼들
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
                        onClick = { viewModel.addMovementStep() },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Place, contentDescription = null)
                        Text("이동")
                    }
                }
                Spacer(Modifier.height(100.dp))
            }
        }

        // ▼▼▼ [추가] 검색 팝업 (조건부 표시) ▼▼▼
        if (activeSearchStepId != null && activeSearchType != null) {
            Dialog(
                onDismissRequest = {
                    activeSearchStepId = null
                    activeSearchType = null
                },
                properties = DialogProperties(usePlatformDefaultWidth = false) // 전체 화면 사용
            ) {
                // 아까 만든 검색 화면을 여기에 띄웁니다!
                StationSearchScreen(
                    onDismiss = {
                        activeSearchStepId = null
                        activeSearchType = null
                    },
                    onStationSelected = { station ->
                        // 선택된 역 정보를 뷰모델에 반영
                        val currentSteps = uiState.steps
                        val targetStep = currentSteps.find { it.id == activeSearchStepId }

                        if (targetStep != null) {
                            val newStep = if (activeSearchType == "FROM") {
                                targetStep.copy(from = "${station.x},${station.y}") // 이름도 저장하면 좋음
                            } else {
                                targetStep.copy(to = "${station.x},${station.y}")
                            }
                            viewModel.updateStep(newStep)

                            // 만약 출발/도착 둘 다 채워졌으면 자동 계산!
                            if (newStep.from != null && newStep.to != null) {
                                viewModel.calculateDuration(newStep)
                            }
                        }
                        // 팝업 닫기
                        activeSearchStepId = null
                        activeSearchType = null
                    }
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddTopBar(title: String, onBackClick: () -> Unit, onSaveClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold) },
        navigationIcon = {
            IconButton(onClick = onBackClick) { Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기") }
        },
        actions = {
            TextButton(onClick = onSaveClick) { Text("저장", fontWeight = FontWeight.Bold, fontSize = 16.sp) }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Transparent)
    )
}

@Composable
fun StepCard(
    step: StepEntity,
    viewModel: RoutineViewModel,
    onStepChanged: (StepEntity) -> Unit,
    onDeleteClick: () -> Unit,
    onSearchClick: (String) -> Unit // ✅ 검색 버튼 콜백 추가 ("FROM" or "TO")
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.3f))
    ) {
        Column(Modifier.padding(16.dp)) {
            // 1. 상단 (이름, 시간)
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (step.isTransport) Icons.Default.Place else Icons.Default.Star,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(16.dp))
                BasicTextField(
                    value = step.name,
                    onValueChange = { onStepChanged(step.copy(name = it)) },
                    modifier = Modifier.weight(1f),
                    textStyle = LocalTextStyle.current.copy(fontSize = 16.sp)
                )
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

            // 2. 이동 단계일 때 -> 검색 버튼 표시
            if (step.isTransport) {
                Spacer(Modifier.height(16.dp))

                // 출발지 검색 버튼
                OutlinedTextField(
                    value = step.from ?: "",
                    onValueChange = {}, // 읽기 전용
                    label = { Text("출발지 (검색)") },
                    modifier = Modifier.fillMaxWidth().clickable { onSearchClick("FROM") },
                    enabled = false, // 직접 입력 막고 클릭만 허용
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) }
                )

                Spacer(Modifier.height(8.dp))

                // 도착지 검색 버튼
                OutlinedTextField(
                    value = step.to ?: "",
                    onValueChange = {},
                    label = { Text("도착지 (검색)") },
                    modifier = Modifier.fillMaxWidth().clickable { onSearchClick("TO") },
                    enabled = false,
                    trailingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                    colors = OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )

                Spacer(Modifier.height(8.dp))

                // 시간 재계산 버튼
                Button(
                    onClick = { viewModel.calculateDuration(step) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text(if(step.calculatedDuration != null) "시간 재계산" else "예상 시간 계산")
                }
            }
        }
    }
}

@Composable
fun RoutineSummaryBar(state: RoutineEditState) {
    val totalMinutes = state.steps.sumOf { it.duration }
    val totalTimeStr = if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분" else "${totalMinutes}분"

    BottomAppBar(containerColor = MaterialTheme.colorScheme.surfaceVariant) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("총 소요시간", fontWeight = FontWeight.Bold)
            Text(totalTimeStr, fontWeight = FontWeight.Bold, fontSize = 18.sp)
            Text("단계 수", fontWeight = FontWeight.Bold)
            Text("${state.steps.size}개", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        }
    }
}