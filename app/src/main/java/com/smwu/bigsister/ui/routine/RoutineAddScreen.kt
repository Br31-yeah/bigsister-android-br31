package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.DragIndicator
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material.icons.rounded.Place
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.ui.map.StationSearchScreen
import com.smwu.bigsister.ui.viewmodel.RoutineEditState
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel

// 공통 색상
val GrayBg = Color(0xFFF2F2F7)
val PurplePrimary = Color(0xFF8B8FD9)
val MintConfirm = Color(0xFF8FD9B3)
val TextGray = Color.Gray

@Composable
fun RoutineAddScreen(
    routineId: Long?, // Long 기반 통일
    viewModel: RoutineViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    LaunchedEffect(routineId) {
        viewModel.loadRoutineForEdit(routineId)
    }

    val uiState by viewModel.editState.collectAsState()
    var activeSearchStepId by remember { mutableStateOf<Long?>(null) }
    var activeSearchType by remember { mutableStateOf<String?>(null) }

    Scaffold(
        topBar = {
            RoutineAddTopBar(
                title = if (routineId == null) "루틴 생성" else "루틴 수정",
                onBackClick = onNavigateBack
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
            // 루틴 이름
            item {
                Spacer(Modifier.height(12.dp))
                Text("루틴 이름", fontSize = 16.sp)
                Spacer(Modifier.height(12.dp))
                FigmaTextField(
                    value = uiState.title,
                    onValueChange = viewModel::updateTitle,
                    placeholder = "아침 루틴, 출근 준비…",
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // 제목
            item { Text("단계", fontSize = 16.sp) }

            // 단계 리스트
            itemsIndexed(uiState.steps, key = { _, step -> step.id }) { _, step ->
                StepCardFigma(
                    step = step,
                    viewModel = viewModel,
                    onStepChanged = viewModel::updateStep,
                    onDeleteClick = { viewModel.removeStep(step) },
                    onSearchClick = { type ->
                        activeSearchStepId = step.id
                        activeSearchType = type
                    }
                )
            }

            // 단계 추가 버튼
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    FigmaOutlineButton(
                        text = "단계 추가",
                        icon = Icons.Default.Add,
                        onClick = viewModel::addBlankStep,
                        modifier = Modifier.weight(1f)
                    )
                    FigmaOutlineButton(
                        text = "이동",
                        icon = Icons.Rounded.Place,
                        onClick = viewModel::addMovementStep,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 요약 카드
            item { SummaryCardFigma(state = uiState) }

            // 저장 버튼
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
                        text = if (routineId == null) "루틴 저장" else "수정 완료",
                        fontSize = 18.sp,
                        color = Color.White
                    )
                }
            }
        }

        // 장소 검색 팝업
        if (activeSearchStepId != null && activeSearchType != null) {
            StationSearchScreen(
                onDismiss = { activeSearchStepId = null; activeSearchType = null },
                onStationSelected = { station ->
                    val step = uiState.steps.find { it.id == activeSearchStepId }
                    if (step != null) {
                        val newLoc = "${station.stationName}|${station.x},${station.y}"

                        val updated = if (activeSearchType == "FROM") {
                            step.copy(from = newLoc)
                        } else step.copy(to = newLoc)

                        viewModel.updateStep(updated)
                        if (updated.from != null && updated.to != null) {
                            viewModel.calculateDuration(updated)
                        }
                    }
                    activeSearchStepId = null
                    activeSearchType = null
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineAddTopBar(title: String, onBackClick: () -> Unit) {
    TopAppBar(
        title = { Text(title, fontSize = 20.sp) },
        navigationIcon = {
            IconButton(onClick = onBackClick) {
                Icon(Icons.Default.ArrowBack, contentDescription = null)
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
    )
}

/* -------------------------- Step 카드 UI --------------------------- */

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
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, GrayBg),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(Modifier.padding(16.dp)) {

            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Outlined.DragIndicator, contentDescription = null, tint = TextGray)
                Spacer(Modifier.width(8.dp))

                // 아이콘 영역
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(GrayBg)
                        .padding(4.dp)
                ) {
                    Icon(
                        imageVector = if (step.isTransport) Icons.Rounded.Place else Icons.Default.Star,
                        contentDescription = null
                    )
                    Icon(Icons.Default.KeyboardArrowDown, null)
                }

                Spacer(Modifier.width(12.dp))

                // 이름 입력
                FigmaTextField(
                    value = step.name,
                    onValueChange = { onStepChanged(step.copy(name = it)) },
                    placeholder = "단계 이름",
                    modifier = Modifier.weight(1f),
                    height = 44.dp
                )

                Spacer(Modifier.width(8.dp))

                // duration
                FigmaTextField(
                    value = step.duration.toString(),
                    onValueChange = {
                        onStepChanged(step.copy(duration = it.toIntOrNull() ?: 0))
                    },
                    placeholder = "0",
                    modifier = Modifier.width(60.dp),
                    height = 44.dp,
                    readOnly = step.isTransport && step.calculatedDuration != null,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )

                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Outlined.Delete, null, tint = TextGray)
                }
            }

            // 이동 단계 전용
            if (step.isTransport) {
                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    val fromName = step.from?.split("|")?.firstOrNull() ?: "출발지"
                    val toName = step.to?.split("|")?.firstOrNull() ?: "도착지"

                    LocationSelectButton(fromName, Icons.Rounded.Place, { onSearchClick("FROM") }, Modifier.weight(1f))
                    LocationSelectButton(toName, Icons.Rounded.Place, { onSearchClick("TO") }, Modifier.weight(1f))
                }

                Spacer(Modifier.height(16.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TransportModeButton("자동차", Icons.Rounded.DirectionsCar, step.transportMode == "driving", {
                        onStepChanged(step.copy(transportMode = "driving"))
                        viewModel.calculateDuration(step)
                    }, Modifier.weight(1f))

                    TransportModeButton("대중교통", Icons.Rounded.DirectionsBus, step.transportMode == "transit", {
                        onStepChanged(step.copy(transportMode = "transit"))
                        viewModel.calculateDuration(step)
                    }, Modifier.weight(1f))

                    TransportModeButton("도보", Icons.Rounded.DirectionsWalk, step.transportMode == "walking", {
                        onStepChanged(step.copy(transportMode = "walking"))
                        viewModel.calculateDuration(step)
                    }, Modifier.weight(1f))
                }

                Spacer(Modifier.height(12.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Outlined.Info, null, tint = TextGray)
                    Spacer(Modifier.width(4.dp))
                    Text(
                        "예상 소요시간: ${
                            step.calculatedDuration?.let { "약 ${it}분" } ?: "계산 필요"
                        }",
                        fontSize = 14.sp,
                        color = TextGray
                    )
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

/* ---------------------- Figma 스타일 입력 필드 ---------------------- */

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
                    Text(placeholder, color = TextGray)
                }
                innerTextField()
            }
        }
    )
}

/* ---------------------- 선택 버튼: 출발/도착 ---------------------- */

@Composable
fun LocationSelectButton(text: String, icon: ImageVector, onClick: () -> Unit, modifier: Modifier) {
    Row(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(GrayBg)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(icon, contentDescription = null, tint = TextGray)
        Spacer(Modifier.width(8.dp))
        Text(text, color = TextGray)
    }
}

/* ---------------------- 선택 버튼: 이동 수단 ---------------------- */

@Composable
fun TransportModeButton(
    text: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier
) {
    Surface(
        modifier = modifier
            .height(40.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) Color.White else GrayBg,
        border = if (isSelected) BorderStroke(1.dp, PurplePrimary) else null
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) Color.Black else TextGray)
            Spacer(Modifier.width(4.dp))
            Text(text, color = if (isSelected) Color.Black else TextGray)
        }
    }
}

/* ---------------------- 요약 카드 ---------------------- */

@Composable
fun SummaryCardFigma(state: RoutineEditState) {
    val totalMinutes = state.steps.sumOf { it.duration }
    val totalStr = if (totalMinutes >= 60)
        "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
    else
        "${totalMinutes}분"

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFF8F8FC))
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("총 소요시간", color = TextGray)
                Text(totalStr, fontSize = 24.sp)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("단계 수", color = TextGray)
                Text("${state.steps.size}개", fontSize = 24.sp)
            }
        }
    }
}

/* ---------------------- FigmaOutlineButton 추가 ---------------------- */

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
        shape = RoundedCornerShape(26.dp),
        border = BorderStroke(1.dp, Color(0xFFE0E0E0)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Black)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(8.dp))
        Text(text, fontSize = 16.sp)
    }
}