package com.smwu.bigsister.ui.reservation

// --- ▼ Import 구문 전체 (수정됨) ▼ ---
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.ui.viewmodel.HomeViewModel // ✅ [추가] 'addReservation' 오류 해결
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.Date // ✅ [추가] 'Date()' 오류 해결
// --- ▲ Import 구문 전체 (수정됨) ▲ ---

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAddScreen(
    dateString: String?, // "YYYY-MM-DD"
    routineViewModel: RoutineViewModel = hiltViewModel(),
    homeViewModel: HomeViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit,
    onNavigateToRoutineAdd: () -> Unit
) {
    val routinesWithSteps by routineViewModel.routineListWithSteps.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }

    val titleDate = remember(dateString) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val outputFormat = SimpleDateFormat("M월 d일", Locale.KOREA)
            val date = inputFormat.parse(dateString ?: "")
            date?.let { outputFormat.format(it) } ?: "오늘"
        } catch (e: Exception) {
            "오늘" // 파싱 실패 시
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("$titleDate 루틴 추가") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        },
        bottomBar = {
            Button(
                onClick = onNavigateToRoutineAdd,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text("+ 새로 만들기", fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("Q 루틴 이름을 검색하세요") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") }
            )

            Spacer(Modifier.height(16.dp))

            if (routinesWithSteps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("생성된 루틴이 없습니다.\n먼저 루틴을 만들어주세요.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routinesWithSteps.filter { it.routine.title.contains(searchQuery, ignoreCase = true) }) { routineItem ->
                        ReservationRoutineCard(
                            routineWithSteps = routineItem,
                            onAddClick = {
                                // ✅ [수정] 'Date()' 오류 해결
                                val dateToAdd = dateString ?: SimpleDateFormat("yyyy-MM-dd", Locale.KOREA).format(Date())
                                homeViewModel.addReservation(
                                    routineId = routineItem.routine.id,
                                    date = dateToAdd,
                                    startTime = "09:00" // TODO: 시간 선택 기능
                                )
                                onNavigateBack()
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ReservationRoutineCard(
    routineWithSteps: RoutineWithSteps,
    onAddClick: () -> Unit
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
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(routine.title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text("$totalTimeStr • ${steps.size}단계", color = Color.Gray, fontSize = 14.sp)
            }
            IconButton(onClick = onAddClick) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = "추가",
                    modifier = Modifier.background(
                        MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
                        CircleShape
                    ),
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}