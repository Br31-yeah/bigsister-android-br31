package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.viewmodel.RoutineEditState
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel

// Figma 색상 정의
val GrayBg = Color(0xFFF2F2F7) // 입력 필드 등 회색 배경
val PurplePrimary = Color(0xFF8B8FD9) // 포인트 보라색
val MintConfirm = Color(0xFF8FD9B3) // 저장 버튼 민트색
val TextGray = Color.Gray

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
    var activeSearchStepId by remember { mutableStateOf<Int?>(null) }
    var activeSearchType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            RoutineAddTopBar(
                title = if (routineId == null) "루틴 생성" else "루틴 수정",
                onBackClick = onNavigateBack
            )
        },
        // bottomBar 제거 -> LazyColumn 맨 아래로 이동
        containerColor = Color.White // 전체 배경 흰색
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp), // 좌우 패딩 20dp
            verticalArrangement = Arrangement.spacedBy(24.dp), // 아이템 간 간격 넓힘
            contentPadding = PaddingValues(bottom = 32.dp) // 하단 여백 추가
        ) {
            // 1. 루틴 이름 입력
            item {
                Spacer(Modifier.height(12.dp))
                Text("루틴 이름", fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                FigmaTextField(
                    value = uiState.title,
                    onValueChange = { viewModel.updateTitle(it) },
                    placeholder = "아침 루틴, 출근 준비...",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            item {
                Text("단계", fontWeight = FontWeight.Bold, fontSize = 16.sp)
            }

            // 2. 단계 목록
            itemsIndexed(uiState.steps, key = { _, step -> step.id }) { index, step ->
                StepCardFigma(
                    step = step,
                    viewModel = viewModel,
                    onStepChanged = { viewModel.updateStep(it) },
                    onDeleteClick = { viewModel.removeStep(step) },
                    onSearchClick = { type ->
                        activeSearchStepId = step.id
                        activeSearchType = type
                    }
                )
            }

            // 3. 단계 추가 버튼 그룹
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FigmaOutlineButton(
                        text = "단계 추가",
                        icon = Icons.Default.Add,
                        onClick = { viewModel.addBlankStep() },
                        modifier = Modifier.weight(1f)
                    )
                    FigmaOutlineButton(
                        text = "이동",
                        icon = Icons.Rounded.Place, // 둥근 마커 아이콘
                        onClick = { viewModel.addMovementStep() },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 4. 요약 정보 카드 (BottomBar 대체)
            item {
                SummaryCardFigma(state = uiState)
            }

            // 5. 저장 버튼 (BottomBar 대체)
            item {
                Button(
                    onClick = { viewModel.saveRoutine(onFinished = onNavigateBack) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MintConfirm)
                ) {
                    Text(
                        if (routineId == null) "루틴 저장" else "수정 완료",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }

        // 검색 팝업 (로직 유지)
        if (activeSearchStepId != null && activeSearchType != null) {
            Dialog(
                onDismissRequest = { activeSearchStepId = null; activeSearchType = null },
                properties = DialogProperties(usePlatformDefaultWidth = false)
            ) {
                StationSearchScreen(
                    onDismiss = { activeSearchStepId = null; activeSearchType = null },
                    onStationSelected = { station ->
                        val targetStep = uiState.steps.find { it.id == activeSearchStepId }
                        if (targetStep != null) {
                            // 좌표와 역 이름을 함께 저장 (예: "강남역|127.0276,37.4979")
                            val locationData = "${station.stationName}|${station.x},${station.y}"
                            val newStep = if (activeSearchType == "FROM") {
                                targetStep.copy(from = locationData)
                            } else {
                                targetStep.copy(to = locationData)
                            }
                            viewModel.updateStep(newStep)
                            if (newStep.from != null && newStep.to != null) {
                                viewModel.calculateDuration(newStep)
                            }
                        }
                        activeSearchStepId = null
                        activeSearchType = null
                    }
                )
            }
        }
    }
}

// 상단 바 (저장 버튼 제거 - 하단으로 이동)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontWeight = FontWeight.Bold, fontSize = 20.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

// Figma 스타일의 단계 카드
@Composable
fun StepCardFigma(
    step: StepEntity,
    viewModel: RoutineViewModel,
    onStepChanged: (StepEntity) -> Unit,
    onDeleteClick: () -> Unit,
    onSearchClick: (String) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp), // 더 둥글게
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = BorderStroke(1.dp, GrayBg) // 연한 테두리
    ) {
        Column(Modifier.padding(16.dp)) {
            // [상단] 드래그, 아이콘, 이름, 시간, 삭제
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Outlined.DragIndicator,
                    contentDescription = "드래그",
                    tint = TextGray,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.width(8.dp))
                // 아이콘 선택 (현재는 고정, 추후 드롭다운 구현 필요)
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrayBg)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (step.isTransport) Icons.Rounded.Place else Icons.Default.Star, // 아이콘 변경
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = Color.Unspecified // 원래 색상 유지 시도 (안되면 tint 적용)
                    )
                    Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                }
                Spacer(Modifier.width(12.dp))

                // 단계 이름 입력 (회색 박스)
                FigmaTextField(
                    value = step.name,
                    onValueChange = { onStepChanged(step.copy(name = it)) },
                    placeholder = "단계 이름",
                    modifier = Modifier.weight(1f),
                    height = 44.dp
                )
                Spacer(Modifier.width(8.dp))

                // 시간 입력 (회색 박스, 오른쪽 배치)
                FigmaTextField(
                    value = step.duration.toString(),
                    onValueChange = { onStepChanged(step.copy(duration = it.toIntOrNull() ?: 0)) },
                    placeholder = "0",
                    modifier = Modifier.width(60.dp),
                    height = 44.dp,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    readOnly = step.isTransport && step.calculatedDuration != null, // 계산된 경우 수정 불가
                    textStyle = TextStyle(textAlign = androidx.compose.ui.text.style.TextAlign.Center, fontSize = 16.sp)
                )
                Spacer(Modifier.width(8.dp))
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = TextGray)
                }
            }

            // [이동 단계 전용 UI]
            if (step.isTransport) {
                Spacer(Modifier.height(16.dp))
                // 출발지 / 도착지 버튼
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    // 역 이름만 추출해서 보여줌 (예: "강남역|127..." -> "강남역")
                    val fromName = step.from?.split("|")?.firstOrNull() ?: ""
                    val toName = step.to?.split("|")?.firstOrNull() ?: ""

                    LocationSelectButton(
                        text = fromName.ifEmpty { "출발지" },
                        icon = Icons.Rounded.Place,
                        onClick = { onSearchClick("FROM") },
                        modifier = Modifier.weight(1f)
                    )
                    LocationSelectButton(
                        text = toName.ifEmpty { "도착지" },
                        icon = Icons.Rounded.Place, // 도착지 아이콘 다르게 할 수도 있음
                        onClick = { onSearchClick("TO") },
                        modifier = Modifier.weight(1f)
                    )
                }
                Spacer(Modifier.height(16.dp))

                // 이동 수단 선택 (라디오 버튼 그룹 형태)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransportModeButton(
                        text = "자동차",
                        icon = Icons.Rounded.DirectionsCar,
                        isSelected = step.transportMode == "driving",
                        onClick = { onStepChanged(step.copy(transportMode = "driving")); viewModel.calculateDuration(step) },
                        modifier = Modifier.weight(1f)
                    )
                    TransportModeButton(
                        text = "대중교통",
                        icon = Icons.Rounded.DirectionsBus,
                        isSelected = step.transportMode == "transit",
                        onClick = { onStepChanged(step.copy(transportMode = "transit")); viewModel.calculateDuration(step) },
                        modifier = Modifier.weight(1f)
                    )
                    TransportModeButton(
                        text = "도보",
                        icon = Icons.Rounded.DirectionsWalk,
                        isSelected = step.transportMode == "walking",
                        onClick = { onStepChanged(step.copy(transportMode = "walking")); viewModel.calculateDuration(step) },
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(Modifier.height(12.dp))
                // 예상 소요시간 표시
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    val durationText = if (step.calculatedDuration != null) "약 ${step.calculatedDuration}분" else "계산 필요"
                    Text("예상 소요시간: $durationText", color = TextGray, fontSize = 14.sp)
                }
            }

            Spacer(Modifier.height(12.dp))
            // 메모 입력
            FigmaTextField(
                value = step.memo ?: "",
                onValueChange = { onStepChanged(step.copy(memo = it)) },
                placeholder = "메모 (선택사항)",
                modifier = Modifier.fillMaxWidth(),
                height = 50.dp,
                singleLine = false
            )
        }
    }
}

// [커스텀 컴포넌트] Figma 스타일 입력 필드
@Composable
fun FigmaTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    height: Dp = 56.dp,
    readOnly: Boolean = false,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    textStyle: TextStyle = TextStyle(fontSize = 16.sp)
) {
    BasicTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(12.dp))
            .background(GrayBg)
            .padding(horizontal = 16.dp),
        readOnly = readOnly,
        singleLine = singleLine,
        keyboardOptions = keyboardOptions,
        textStyle = textStyle,
        decorationBox = { innerTextField ->
            Box(contentAlignment = Alignment.CenterStart) {
                if (value.isEmpty()) {
                    Text(placeholder, color = TextGray, fontSize = 16.sp)
                }
                innerTextField()
            }
        }
    )
}

// [커스텀 컴포넌트] 테두리 버튼 (+ 단계 추가, 이동)
@Composable
fun FigmaOutlineButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = RoundedCornerShape(26.dp), // 완전히 둥글게
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
    }
}

// [커스텀 컴포넌트] 출발지/도착지 선택 버튼
@Composable
fun LocationSelectButton(
    text: String,
    icon: ImageVector,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GrayBg)
            .clickable { onClick() }
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextGray, fontSize = 14.sp, maxLines = 1)
    }
}

// [커스텀 컴포넌트] 이동 수단 선택 버튼
@Composable
fun TransportModeButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val backgroundColor = if (isSelected) Color.White else GrayBg
    val contentColor = if (isSelected) Color.Black else TextGray
    val border = if (isSelected) BorderStroke(1.dp, PurplePrimary) else null

    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = backgroundColor,
        border = border
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center,
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Icon(icon, contentDescription = null, tint = contentColor, modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(4.dp))
            Text(text, color = contentColor, fontSize = 14.sp, fontWeight = FontWeight.SemiBold)
        }
    }
}

// [커스텀 컴포넌트] 요약 정보 카드
@Composable
fun SummaryCardFigma(state: RoutineEditState) {
    val totalMinutes = state.steps.sumOf { it.duration }
    val totalTimeStr = if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분" else "${totalMinutes}분"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8FC)), // 아주 연한 보라빛 회색
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(20.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text("총 소요시간", fontSize = 14.sp, color = TextGray)
                Spacer(Modifier.height(4.dp))
                Text(totalTimeStr, fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("단계 수", fontSize = 14.sp, color = TextGray)
                Spacer(Modifier.height(4.dp))
                Text("${state.steps.size}개", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}