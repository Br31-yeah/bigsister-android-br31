package com.smwu.bigsister.ui.reservation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAddScreen(
    dateString: String?,
    onNavigateBack: () -> Unit,
    onNavigateToRoutineAdd: () -> Unit,
    viewModel: RoutineViewModel = hiltViewModel()
) {
    // ✅ 타입 추론 및 프로퍼티명 불일치 해결
    val routineList: List<RoutineWithSteps> by viewModel.routineListWithSteps.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("예약 추가 ($dateString)") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Outlined.ArrowBack, null) }
                }
            )
        }
    ) { padding ->
        Column(Modifier.padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                label = { Text("루틴 검색") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(16.dp))

            if (routineList.isEmpty()) {
                Text("등록된 루틴이 없습니다. 루틴을 먼저 생성해주세요.")
                Button(onClick = onNavigateToRoutineAdd) { Text("루틴 생성하러 가기") }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    // ✅ filter 로직에서 it 모호성 제거
                    val filteredList = routineList.filter { item ->
                        item.routine.title.contains(searchQuery, ignoreCase = true)
                    }
                    items(items = filteredList) { routineWithSteps ->
                        Card(
                            modifier = Modifier.fillMaxWidth().clickable { /* 예약 로직 */ },
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        ) {
                            Text(text = routineWithSteps.routine.title, modifier = Modifier.padding(16.dp))
                        }
                    }
                }
            }
        }
    }
}