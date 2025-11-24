package com.smwu.bigsister.ui.routine

// ▼▼▼ 이제 Color.kt에 등록했으니 잘 불러와질 겁니다 ▼▼▼
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.rounded.AccessTime
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel

@Composable
fun RoutineListScreen(
    viewModel: RoutineViewModel = hiltViewModel(),
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Int) -> Unit,
    onStartRoutineClick: (Int) -> Unit
) {
    val routineList by viewModel.routineListWithSteps.collectAsState(initial = emptyList())

    Scaffold(
        containerColor = Color.White,
    ) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            // 1. 타이틀
            item {
                Text(
                    text = "내 루틴",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }

            // 2. 리스트가 비어있을 때 안내 문구 (선택사항)
            if (routineList.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("저장된 루틴이 없어요 😢", color = TextGray)
                    }
                }
            }

            // 3. 루틴 리스트
            items(routineList) { routineWithSteps ->
                RoutineFigmaCard(
                    data = routineWithSteps,
                    onEditClick = { onRoutineClick(routineWithSteps.routine.id) },
                    onDeleteClick = { viewModel.deleteRoutine(routineWithSteps.routine) },
                    onStartClick = { onStartRoutineClick(routineWithSteps.routine.id) }
                )
            }

            // 4. 새 루틴 만들기 버튼
            item {
                Button(
                    onClick = onAddRoutineClick,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = PurpleLight,
                        contentColor = PurplePrimary
                    ),
                    elevation = ButtonDefaults.buttonElevation(0.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text("새 루틴 만들기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
fun RoutineFigmaCard(
    data: RoutineWithSteps,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStartClick: () -> Unit
) {
    val totalMinutes = data.steps.sumOf { it.duration }
    val timeText = if (totalMinutes >= 60)
        "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
    else
        "${totalMinutes}분"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2F2F7))
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            // 상단 영역
            Row(verticalAlignment = Alignment.Top) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFE3E4FA)),
                    contentAlignment = Alignment.Center
                ) {
                    // ⏰ 아이콘 대신 PlayArrow나 다른 아이콘을 써도 됩니다.
                    Text(text = "⏰", fontSize = 24.sp)
                }

                Spacer(Modifier.width(16.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = data.routine.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.Black
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = "🕒 $timeText  •  ${data.steps.size}단계",
                        fontSize = 14.sp,
                        color = TextGray
                    )
                }

                Row {
                    IconButton(onClick = onEditClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Edit, contentDescription = "수정", tint = TextGray, modifier = Modifier.size(20.dp))
                    }
                    IconButton(onClick = onDeleteClick, modifier = Modifier.size(32.dp)) {
                        Icon(Icons.Outlined.Delete, contentDescription = "삭제", tint = TextGray, modifier = Modifier.size(20.dp))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // 중간 단계 미리보기
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.steps.take(3).forEach { step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        val icon = if (step.isTransport) Icons.Rounded.DirectionsCar else Icons.Rounded.AccessTime
                        Icon(icon, contentDescription = null, tint = TextGray, modifier = Modifier.size(16.dp))
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${step.name} · ${step.duration}분",
                            fontSize = 14.sp,
                            color = Color.DarkGray
                        )
                    }
                }
                if (data.steps.size > 3) {
                    Text("+ ${data.steps.size - 3}개 더보기", fontSize = 12.sp, color = TextGray, modifier = Modifier.padding(start = 24.dp))
                }
            }

            Spacer(Modifier.height(20.dp))

            // 하단 시작 버튼
            Button(
                onClick = onStartClick,
                modifier = Modifier
                    .width(120.dp)
                    .height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintConfirm,
                    contentColor = Color.White
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                // 아이콘 추가 (선택사항)
                Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(16.dp)) // 👈 여기도 PlayArrow로 수정됨
                Spacer(Modifier.width(4.dp))
                Text("바로 시작", fontSize = 14.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}