package com.smwu.bigsister.ui.routine

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@Composable
fun RoutineAddScreen(
    routineId: Long? = null,
    onFinished: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    val editState by viewModel.editState.collectAsState()

    LaunchedEffect(routineId) {
        viewModel.loadRoutineForEdit(routineId)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        OutlinedTextField(
            value = editState.title,
            onValueChange = viewModel::updateTitle,
            label = { Text("루틴 이름") },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(editState.steps.size) { index ->
                val step = editState.steps[index]

                StepEditor(
                    step = step,
                    onChange = viewModel::updateStep,
                    onDelete = { viewModel.removeStep(step) },
                    onCalculate = { viewModel.calculateDuration(step) }
                )
            }
        }

        Spacer(Modifier.height(16.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(onClick = viewModel::addBlankStep) {
                Text("단계 추가")
            }
            Button(onClick = viewModel::addMovementStep) {
                Text("이동 추가")
            }
        }

        Spacer(Modifier.height(24.dp))

        Button(
            onClick = { viewModel.saveRoutine(onFinished) },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("저장")
        }
    }
}

@Composable
private fun StepEditor(
    step: StepEntity,
    onChange: (StepEntity) -> Unit,
    onDelete: () -> Unit,
    onCalculate: () -> Unit
) {
    Card {
        Column(Modifier.padding(12.dp)) {

            OutlinedTextField(
                value = step.name,
                onValueChange = { onChange(step.copy(name = it)) },
                label = { Text("단계 이름") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            OutlinedTextField(
                value = step.duration.toString(),
                onValueChange = {
                    onChange(step.copy(duration = it.toIntOrNull() ?: 0))
                },
                label = { Text("소요 시간 (분)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(8.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TextButton(onClick = onCalculate) {
                    Text("시간 계산")
                }
                TextButton(onClick = onDelete) {
                    Text("삭제")
                }
            }
        }
    }
}