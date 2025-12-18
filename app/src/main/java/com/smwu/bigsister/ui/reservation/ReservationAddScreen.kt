package com.smwu.bigsister.ui.reservation

import android.widget.Toast
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
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
    onNavigateBack: () -> Unit,
    onNavigateToRoutineAdd: () -> Unit,
    routineViewModel: RoutineViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel()
) {
    // 1번 브랜치의 깔끔한 상태 수집 로직 적용
    val routineList by routineViewModel.routineListWithSteps.collectAsState(initial = emptyList())
    var searchQuery by remember { mutableStateOf("") }
    val context = LocalContext.current

    // 2번 브랜치의 시간 선택 및 시간대 로직
    val koreaZoneId = ZoneId.of("Asia/Seoul")
    val now = remember { LocalDateTime.now(koreaZoneId) }
    var selectedHour by remember { mutableStateOf(now.hour) }
    var selectedMinute by remember { mutableStateOf(now.minute) }
    var showTimePicker by remember { mutableStateOf(false) }

    // 상단 타이틀 날짜 포맷팅 (2번)
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
        },
        bottomBar = {
            // 하단에 루틴 생성 버튼 배치 (2번 UI)
            Button(
                onClick = onNavigateToRoutineAdd,
                modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                shape = MaterialTheme.shapes.medium
            ) {
                Text("+ 새 루틴 만들기", fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)
        ) {
            // 검색바 (아이콘 포함)
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { searchQuery = it },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("루틴 이름을 검색하세요") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = "검색") },
                shape = MaterialTheme.shapes.medium
            )

            Spacer(Modifier.height(16.dp))

            // 시작 시간 선택 버튼
            Button(
                onClick = { showTimePicker = true },
                modifier = Modifier.fillMaxWidth().height(52.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
            ) {
                Text(text = "🕒 시작 시간: %02d:%02d".format(selectedHour, selectedMinute), fontSize = 16.sp)
            }

            // TimePicker 다이얼로그 (2번 핵심 로직)
            if (showTimePicker) {
                Dialog(onDismissRequest = { showTimePicker = false }) {
                    Surface(shape = MaterialTheme.shapes.medium, tonalElevation = 4.dp) {
                        Column(modifier = Modifier.padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                            val timeState = rememberTimePickerState(initialHour = selectedHour, initialMinute = selectedMinute)
                            TimePicker(state = timeState)
                            Spacer(Modifier.height(16.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                TextButton(onClick = { showTimePicker = false }) { Text("취oc") }
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
                    Text("생성된 루틴이 없습니다.\n루틴을 먼저 만들어주세요.", color = Color.Gray)
                }
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    val filteredList = routineList.filter { it.routine.title.contains(searchQuery, ignoreCase = true) }

                    items(items = filteredList) { routineItem ->
                        ReservationRoutineCard(
                            routineWithSteps = routineItem,
                            onAddClick = {
                                // 유효성 검사 (2번)
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

                                // 과거 시간 체크 (오늘 날짜일 경우에만)
                                if (targetDateTime.isBefore(LocalDateTime.now(koreaZoneId))) {
                                    Toast.makeText(context, "이미 지나간 시간입니다.", Toast.LENGTH_SHORT).show()
                                    return@ReservationRoutineCard
                                }

                                // 저장 로직 수행
                                reservationViewModel.addReservation(
                                    routineId = routineItem.routine.id,
                                    routineTitle = routineItem.routine.title,
                                    date = targetDateStr,
                                    startTime = "%02d:%02d".format(selectedHour, selectedMinute),
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
    val totalMinutes = routineWithSteps.steps.sumOf { it.calculatedDuration ?: it.baseDuration }
    val totalTimeStr = if (totalMinutes >= 60) "${totalMinutes / 60}시간 ${totalMinutes % 60}분" else "${totalMinutes}분"

    Card(
        modifier = Modifier.fillMaxWidth().clickable { onAddClick() },
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF2F2F7))
    ) {
        Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 16.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(routineWithSteps.routine.title, fontSize = 17.sp, fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                Spacer(Modifier.height(4.dp))
                Text("🕒 $totalTimeStr · ${routineWithSteps.steps.size}단계", fontSize = 14.sp, color = Color.Gray)
            }
            Icon(Icons.Default.Add, contentDescription = "추가", tint = MaterialTheme.colorScheme.primary)
        }
    }
}