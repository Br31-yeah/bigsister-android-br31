package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoutineListScreen(
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Long) -> Unit,
    onStartRoutineClick: (Long) -> Unit,
    onSettingsClick: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    // ✅ collectAsState 초기값 설정
    val routineList by viewModel.routineListWithSteps.collectAsState(initial = emptyList())

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("내 루틴", fontWeight = FontWeight.Bold) },
                actions = { IconButton(onClick = onSettingsClick) { Icon(Icons.Default.Settings, null) } }
            )
        }
    ) { padding ->
        LazyColumn(
            Modifier.fillMaxSize().padding(padding).padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            if (routineList.isEmpty()) {
                item { Box(Modifier.fillMaxWidth().height(200.dp), Alignment.Center) { Text("저장된 루틴이 없어요 😢", color = Color.Gray) } }
            }

            items(items = routineList) { item ->
                RoutineCard(
                    data = item,
                    onEditClick = { onRoutineClick(item.routine.id) },
                    onDeleteClick = { viewModel.deleteRoutine(item.routine.id) },
                    onStartClick = { onStartRoutineClick(item.routine.id) }
                )
            }

            item {
                Button(onClick = onAddRoutineClick, Modifier.fillMaxWidth().height(56.dp), shape = RoundedCornerShape(16.dp)) {
                    Icon(Icons.Default.Add, null)
                    Spacer(Modifier.width(8.dp))
                    Text("새 루틴 만들기")
                }
            }
        }
    }
}

@Composable
fun RoutineCard(data: RoutineWithSteps, onEditClick: () -> Unit, onDeleteClick: () -> Unit, onStartClick: () -> Unit) {
    val totalMinutes = data.steps.sumOf { it.calculatedDuration ?: it.baseDuration }
    Card(Modifier.fillMaxWidth(), shape = RoundedCornerShape(24.dp), border = BorderStroke(1.dp, Color(0xFFF2F2F7)), colors = CardDefaults.cardColors(containerColor = Color.White)) {
        Column(Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.Top) {
                Box(Modifier.size(48.dp).clip(CircleShape).background(Color(0xFFE3E4FA)), Alignment.Center) { Text("⏰", fontSize = 24.sp) }
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text(data.routine.title, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Text("🕒 ${totalMinutes}분 • ${data.steps.size}단계", fontSize = 14.sp, color = Color.Gray)
                }
                Row {
                    IconButton(onClick = onEditClick) { Icon(Icons.Outlined.Edit, null, tint = Color.Gray) }
                    IconButton(onClick = onDeleteClick) { Icon(Icons.Outlined.Delete, null, tint = Color.Gray) }
                }
            }
            Spacer(Modifier.height(20.dp))
            Button(onClick = onStartClick, Modifier.width(120.dp).height(40.dp), shape = RoundedCornerShape(20.dp), colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF80CBC4))) {
                Text("바로 시작", fontSize = 14.sp)
            }
        }
    }
}