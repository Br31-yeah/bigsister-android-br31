package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.ui.theme.MintConfirm
import com.smwu.bigsister.ui.theme.PurpleLight
import com.smwu.bigsister.ui.theme.PurplePrimary
import com.smwu.bigsister.ui.theme.TextGray
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    onStartRoutineClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel() // 파라미터 순서 조정 및 기본값 유지
) {
    // 1번 브랜치의 collectAsState 초기값 로직 적용
    val routineList by viewModel.routineListWithSteps.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "내 루틴",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold
                    )
                },
                actions = {
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Default.Settings, contentDescription = "설정", tint = Color.Black)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 16.dp, bottom = 100.dp) // 하단 여백 유지
        ) {
            // 빈 상태 처리
            if (routineList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("저장된 루틴이 없어요 😢", color = TextGray)
                    }
                }
            }

            // 루틴 리스트 아이템
            items(items = routineList, key = { it.routine.id }) { routine ->
                RoutineCard(
                    data = routine,
                    onEditClick = { onRoutineClick(routine.routine.id) },
                    onDeleteClick = { viewModel.deleteRoutine(routine.routine.id) },
                    onStartClick = { onStartRoutineClick(routine.routine.id) }
                )
            }

            // 새 루틴 만들기 버튼
            item {
                Button(
                    onClick = onAddRoutineClick,
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleLight,
                        contentColor = PurplePrimary
                    )
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("새 루틴 만들기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

@Composable
fun RoutineCard(
    data: RoutineWithSteps,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStartClick: () -> Unit
) {
    // 시간 계산 로직 (시간/분 표시 통합)
    val totalMinutes = data.steps.sumOf { it.calculatedDuration ?: it.baseDuration }
    val timeText = if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분" else "${totalMinutes}분"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        border = BorderStroke(1.dp, Color(0xFFF2F2F7)),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(Modifier.padding(20.dp)) {
            /* 상단 영역: 제목 및 수정/삭제 */
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3E4FA)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("⏰", fontSize = 24.sp)
                }

                Spacer(Modifier.width(16.dp))

                Column(Modifier.weight(1f)) {
                    Text(
                        text = data.routine.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "🕒 $timeText • ${data.steps.size}단계",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }

                Row {
                    IconButton(onClick = onEditClick) {
                        Icon(Icons.Outlined.Edit, contentDescription = "수정", tint = TextGray)
                    }
                    IconButton(onClick = onDeleteClick) {
                        Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = TextGray)
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            /* 단계 미리보기 영역 (최대 3개) */
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.steps.take(3).forEach { step ->
                    val duration = step.calculatedDuration ?: step.baseDuration
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.DirectionsCar,
                            contentDescription = null,
                            tint = TextGray,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${step.name} · ${duration}분",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            /* 하단 시작 버튼 */
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintConfirm)
            ) {
                Icon(
                    imageVector = Icons.Rounded.AccessTime,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text("바로 시작", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}