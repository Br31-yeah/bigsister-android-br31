package com.smwu.bigsister.ui.reservation

import androidx.compose.foundation.clickable
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
import androidx.compose.material3.ButtonDefaults
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
import java.time.ZoneId
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReservationAddScreen(
    dateString: String?,
    onNavigateBack: () -> Unit,
    onNavigateToRoutineAdd: () -> Unit,
    routineViewModel: RoutineViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel()
) {
    val routineList by routineViewModel.routineListWithSteps.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    val koreaZoneId = ZoneId.of("Asia/Seoul")
    val now = remember { LocalDateTime.now(koreaZoneId) }
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
                title = { Text("$titleDate 예약 추가") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("루틴 이름을 검색하세요") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                shape = MaterialTheme.shapes.medium
            )
            Spacer(Modifier.height(16.dp))
            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text(text = "🕒 시작 시간: %02d:%02d".format(selectedHour, selectedMinute), fontSize = 16.sp)
            }

            if (showTimePicker) {
                Dialog(onDismissRequest = { showTimePicker = false }) {
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val timeState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
                            TimePicker(state = timeState)
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
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

            if (routineList.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("루틴이 없습니다.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val filteredList = routineList.filter { it.routine.title.contains(searchQuery, ignoreCase = true) }
                    items(items = filteredList) { routineItem ->
                        ReservationRoutineCard(
                            routineWithSteps = routineItem,
                            onAddClick = {
                                val targetDateStr = dateString ?: LocalDate.now().toString()
                                val currentUserId = Firebase.auth.currentUser?.uid ?: ""

                                // ✅ ReservationEntity 객체 생성 후 전달하여 에러 해결
                                val newReservation = ReservationEntity(
                                    userId = currentUserId,
                                    routineId = routineItem.routine.id,
                                    routineTitle = routineItem.routine.title,
                                    date = targetDateStr,
                                    startTime = "%02d:%02d".format(selectedHour, selectedMinute),
                                    endTime = null
                                )

                                reservationViewModel.addReservation(
                                    reservation = newReservation,
                                    onSuccess = { onNavigateBack() }
                                )
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
    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAddClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(routineWithSteps.routine.title, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Text("${routineWithSteps.steps.size}단계", fontSize = 14.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Add, contentDescription = "추가")
        }
    }
}