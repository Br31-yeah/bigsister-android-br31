package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
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
    onRoutineClick: (Long) -> Unit,
    onStartRoutineClick: (Long) -> Unit
) {
    val routineList by viewModel.routineListWithSteps.collectAsState(initial = emptyList())

    Scaffold(containerColor = Color.White) { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(top = 24.dp, bottom = 100.dp)
        ) {
            item {
                Text(
                    text = "내 루틴",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )
            }

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

            items(routineList) { routine ->
                RoutineCard(
                    data = routine,
                    onEditClick = { onRoutineClick(routine.routine.id) },
                    onDeleteClick = { viewModel.deleteRoutine(routine.routine) },
                    onStartClick = { onStartRoutineClick(routine.routine.id) }
                )
            }

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
                    Text("새 루틴 만들기", fontSize = 16.sp)
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
    val totalMinutes = data.steps.sumOf { it.duration }
    val timeText =
        if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
        else "${totalMinutes}분"

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(0.dp),
        border = BorderStroke(1.dp, Color(0xFFF2F2F7))
    ) {
        Column(Modifier.padding(20.dp)) {
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
                    Text(data.routine.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Spacer(Modifier.height(4.dp))
                    Text("🕒 $timeText • ${data.steps.size}단계", fontSize = 14.sp, color = TextGray)
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

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                data.steps.take(3).forEach { step ->
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Rounded.DirectionsCar, contentDescription = null, tint = TextGray)
                        Spacer(Modifier.width(8.dp))
                        Text("${step.name} · ${step.duration}분", fontSize = 14.sp)
                    }
                }
            }

            Spacer(Modifier.height(20.dp))

            Button(
                onClick = onStartClick,
                modifier = Modifier.width(120.dp).height(40.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MintConfirm)
            ) {
                Icon(Icons.Rounded.AccessTime, contentDescription = null)
                Spacer(Modifier.width(4.dp))
                Text("바로 시작", fontSize = 14.sp)
            }
        }
    }
}