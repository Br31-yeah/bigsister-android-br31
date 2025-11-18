package com.smwu.bigsister.ui.routine

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Place
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.RoutineEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel

@Composable
fun RoutineListScreen(
    viewModel: RoutineViewModel = hiltViewModel(),
    onAddRoutineClick: () -> Unit,
    onRoutineClick: (Int) -> Unit,
    onStartRoutineClick: (Int) -> Unit
) {
    val routinesWithSteps by viewModel.routineListWithSteps.collectAsState(initial = emptyList())

    var showDeleteDialog by remember { mutableStateOf(false) }
    var routineToDelete by remember { mutableStateOf<RoutineEntity?>(null) }

    Scaffold(
        topBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp) // ✅ 'page' -> '.height'로 수정
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.CenterStart
            ) {
                Text(
                    "내 루틴",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        },
        bottomBar = {
            Button(
                onClick = onAddRoutineClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp),
                shape = MaterialTheme.shapes.large
            ) {
                Text("+ 새 루틴 만들기", fontSize = 16.sp)
            }
        }
    ) { paddingValues ->

        if (routinesWithSteps.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    "저장된 루틴이 없습니다.\n새 루틴을 만들어보세요.",
                    textAlign = TextAlign.Center,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                item { Spacer(Modifier.height(4.dp)) }

                items(routinesWithSteps, key = { it.routine.id }) { routineWithSteps ->
                    RoutineListItem(
                        routineWithSteps = routineWithSteps,
                        onEditClick = { onRoutineClick(routineWithSteps.routine.id) },
                        onDeleteClick = {
                            routineToDelete = routineWithSteps.routine
                            showDeleteDialog = true
                        },
                        onStartClick = { onStartRoutineClick(routineWithSteps.routine.id) }
                    )
                }

                item { Spacer(Modifier.height(4.dp)) }
            }
        }

        if (showDeleteDialog) {
            DeleteConfirmationDialog(
                routineName = routineToDelete?.title ?: "",
                onConfirm = {
                    routineToDelete?.let { viewModel.deleteRoutine(it) }
                    showDeleteDialog = false
                    routineToDelete = null
                },
                onDismiss = {
                    showDeleteDialog = false
                    routineToDelete = null
                }
            )
        }
    }
}

@Composable
fun RoutineListItem(
    routineWithSteps: RoutineWithSteps,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onStartClick: () -> Unit
) {
    val routine = routineWithSteps.routine
    val steps = routineWithSteps.steps

    val totalMinutes = steps.sumOf { it.duration }
    val totalTimeStr = if (totalMinutes >= 60) {
        "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
    } else {
        "${totalMinutes}분"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // --- 1. 상단: 아이콘, 제목, 수정/삭제 ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(Color.Red.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Star,
                        contentDescription = "루틴 아이콘",
                        tint = Color.Red
                    )
                }

                Spacer(Modifier.size(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = routine.title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "$totalTimeStr • ${steps.size}단계",
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "수정")
                }
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "삭제")
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 2. 하위 단계 목록 (최대 3개) ---
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                steps.take(3).forEach { step ->
                    StepRow(step = step)
                }
                if (steps.size > 3) {
                    Text("... 그 외 ${steps.size - 3}개", fontSize = 14.sp, color = Color.Gray)
                }
            }

            Spacer(Modifier.height(16.dp))

            // --- 3. 바로 시작 버튼 ---
            Button(
                onClick = onStartClick,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                    contentColor = MaterialTheme.colorScheme.primary
                ),
                elevation = ButtonDefaults.buttonElevation(0.dp)
            ) {
                Text("바로 시작")
            }
        }
    }
}

@Composable
private fun StepRow(step: com.smwu.bigsister.data.local.StepEntity) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = when (step.icon) {
                "씻기" -> Icons.Default.Check
                "이동" -> Icons.Default.Place
                else -> Icons.Default.MoreVert
            },
            contentDescription = step.name,
            modifier = Modifier.size(18.dp),
            tint = Color.Gray
        )
        Spacer(Modifier.size(8.dp))
        Text(text = step.name, modifier = Modifier.weight(1f), fontSize = 14.sp)
        Text(text = "${step.duration}분", fontSize = 14.sp, color = Color.Gray)
    }
}

@Composable
fun DeleteConfirmationDialog(
    routineName: String,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("이 루틴을 삭제하시겠어요?") },
        text = { Text("'$routineName'을(를) 삭제하면 복구할 수 없습니다.") },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(contentColor = Color.Red)
            ) {
                Text("삭제")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("취소")
            }
        }
    )
}