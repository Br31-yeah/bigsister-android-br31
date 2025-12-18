package com.smwu.bigsister.ui.reservation

import android.widget.Toast
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
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.hilt.navigation.compose.hiltViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.smwu.bigsister.data.local.ReservationEntity
import com.smwu.bigsister.data.local.RoutineWithSteps
import com.smwu.bigsister.ui.viewModel.ReservationViewModel
import com.smwu.bigsister.ui.viewModel.RoutineViewModel
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

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
    val context = LocalContext.current

    // ---------------------------
    // ⭐ 시간 선택 상태 (친구분 코드의 현재 시간 로직 반영)
    // ---------------------------
    val now = remember { LocalDateTime.now(ZoneId.of("Asia/Seoul")) }
    var selectedHour by remember { mutableStateOf(now.hour) }
    var selectedMinute by remember { mutableStateOf(now.minute) }
    var showTimePicker by remember { mutableStateOf(false) }

    val titleDate = remember(dateString) {
        try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.KOREA)
            val outputFormat = SimpleDateFormat("M월 d일", Locale.KOREA)
            val date = inputFormat.parse(dateString ?: "")
            date?.let { outputFormat.format(it) } ?: "오늘"
        } catch (e: Exception) { "오늘" }
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
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp)
            ) {
                Text("+ 새로 만들기", fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
        ) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("루틴 이름을 검색하세요") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") }
            )

            Spacer(Modifier.height(16.dp))

            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Text(text = "시작 시간 선택: %02d:%02d".format(selectedHour, selectedMinute), fontSize = 16.sp)
            }

            if (showTimePicker) {
                Dialog(onDismissRequest = { showTimePicker = false }) {
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val timeState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
                            TimePicker(state = timeState)
                            Spacer(Modifier.height(16.dp))
                            Row(horizontalArrangement = Arrangement.SpaceEvenly) {
                                TextButton(onClick = { showTimePicker = false }) { Text("취소") }
                                TextButton(onClick = {
                                    selectedHour = timeState.hour
                                    selectedMinute = timeState.minute
                                    showTimePicker = false
                                }) { Text("확인") }
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            if (routinesWithSteps.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("생성된 루틴이 없습니다.\n먼저 루틴을 만들어주세요.")
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    items(routinesWithSteps.filter { it.routine.title.contains(searchQuery, ignoreCase = true) }) { routineItem ->
                        ReservationRoutineCard(
                            routineWithSteps = routineItem,
                            onAddClick = {
                                // 🛑 [친구분 코드의 유효성 검사 로직]
                                val totalMinutes = routineItem.steps.sumOf { it.calculatedDuration ?: it.baseDuration }
                                if (totalMinutes <= 0L) {
                                    Toast.makeText(context, "소요 시간이 0분인 루틴은 예약할 수 없습니다.", Toast.LENGTH_SHORT).show()
                                    return@ReservationRoutineCard
                                }

                                val targetDateStr = dateString ?: LocalDate.now().toString()
                                val targetDateTime = LocalDateTime.of(
                                    LocalDate.parse(targetDateStr, DateTimeFormatter.ISO_DATE),
                                    LocalTime.of(selectedHour, selectedMinute)
                                )
                                if (targetDateTime.isBefore(LocalDateTime.now(ZoneId.of("Asia/Seoul")))) {
                                    Toast.makeText(context, "이미 지나간 시간입니다.", Toast.LENGTH_SHORT).show()
                                    return@ReservationRoutineCard
                                }

                                // ✅ [본인의 동기화 로직 유지]
                                reservationViewModel.addReservation(
                                    reservation = ReservationEntity(
                                        userId = Firebase.auth.currentUser?.uid ?: "",
                                        routineId = routineItem.routine.id,
                                        date = targetDateStr,
                                        startTime = "%02d:%02d".format(selectedHour, selectedMinute),
                                        routineTitle = routineItem.routine.title
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
fun ReservationRoutineCard(routineWithSteps: RoutineWithSteps, onAddClick: () -> Unit) {
    val routine = routineWithSteps.routine
    val steps = routineWithSteps.steps
    val totalMinutes = steps.sumOf { it.calculatedDuration ?: it.baseDuration }
    val totalTimeStr = if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분" else "${totalMinutes}분"

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(routine.title, fontSize = 16.sp)
                Text("$totalTimeStr • ${steps.size}단계", fontSize = 14.sp, color = Color.Gray)
            }
            IconButton(onClick = onAddClick) {
                Icon(Icons.Default.Add, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
            }
        }
    }
}