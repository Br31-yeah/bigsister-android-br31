package com.smwu.bigsister.ui.reservation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel
import com.smwu.bigsister.ui.viewmodel.ReservationViewModel
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAddScreen(
    dateString: String?,
    routineViewModel: RoutineViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel(),
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
            "오늘"
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
                placeholder = { Text("루틴 이름을 검색하세요") },
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
                    items(
                        routinesWithSteps.filter {
                            it.routine.title.contains(searchQuery, ignoreCase = true)
                        }
                    ) { routineItem ->
                        ReservationRoutineCard(
                            routineWithSteps = routineItem,
                            onAddClick = {
                                val dateToAdd = dateString
                                    ?: SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
                                        .format(Date())

                                reservationViewModel.addReservation(
                                    reservation = ReservationEntity(
                                        routineId = routineItem.routine.id,
                                        date = dateToAdd,
                                        startTime = "09:00"
                                    )
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
    val totalTimeStr =
        if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
        else "${totalMinutes}분"

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
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}