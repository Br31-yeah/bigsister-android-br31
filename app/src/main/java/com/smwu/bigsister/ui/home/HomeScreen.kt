package com.smwu.bigsister.ui.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.ui.component.EmptyRoutineState
import com.smwu.bigsister.ui.component.HomeTopBar
import com.smwu.bigsister.ui.component.ReservationCard
import com.smwu.bigsister.ui.component.WeeklyCalendar
import com.smwu.bigsister.ui.theme.MintConfirm
import com.smwu.bigsister.ui.viewModel.HomeViewModel
import com.smwu.bigsister.ui.viewModel.ReservationViewModel
import java.time.Instant
import java.time.ZoneId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel = hiltViewModel(),
    reservationViewModel: ReservationViewModel = hiltViewModel(),
    onNavigateToReservationAdd: (String) -> Unit,
    onNavigateToRoutineList: () -> Unit,
    onNavigateToStats: () -> Unit,
    onSettingsClick: () -> Unit,
    onNavigateToLiveMode: (Long) -> Unit // ✅ 라이브 모드 이동 콜백 유지
) {
    // 1번+2번 상태 통합
    val selectedDate by homeViewModel.selectedDate.collectAsState()
    val todaySchedules by homeViewModel.todaySchedules.collectAsState()
    val userName by homeViewModel.userName.collectAsState() // ✅ 2번 브랜치: 유저 이름 추가

    var showDatePicker by remember { mutableStateOf(false) }
    val koreaZoneId = ZoneId.of("Asia/Seoul") // ✅ 2번 브랜치: 한국 시간대 고정

    Scaffold(
        topBar = {
            HomeTopBar(
                currentMonth = selectedDate.monthValue,
                userName = userName, // ✅ 2번 브랜치: 이름 전달 로직 유지
                onCalendarClick = { showDatePicker = true }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 20.dp)
        ) {
            WeeklyCalendar(
                selectedDate = selectedDate,
                onDateSelected = homeViewModel::setSelectedDate
            )

            Spacer(Modifier.height(24.dp))

            /* ---------- 오늘 일정 리스트 ---------- */
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (todaySchedules.isEmpty()) {
                    EmptyRoutineState {
                        onNavigateToReservationAdd(selectedDate.toString())
                    }
                } else {
                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(todaySchedules) { reservation ->
                            ReservationCard(
                                reservation = reservation,
                                onStart = {
                                    // ✅ 1번 브랜치: 루틴 ID를 넘겨주며 라이브 모드로 이동
                                    onNavigateToLiveMode(reservation.routineId)
                                },
                                onCancel = {
                                    // ✅ 1번 브랜치: 삭제 로직 유지
                                    reservationViewModel.deleteReservation(reservation.id)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            /* ---------- 하단 예약 추가 버튼 ---------- */
            Button(
                onClick = {
                    onNavigateToReservationAdd(selectedDate.toString())
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintConfirm,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = "예약 추가",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(Modifier.height(24.dp))
        }

        /* ---------- 날짜 선택 다이얼로그 (한국 시간대 적용) ---------- */
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay(koreaZoneId)
                    .toInstant()
                    .toEpochMilli()
            )

            DatePickerDialog(
                onDismissRequest = { showDatePicker = false },
                confirmButton = {
                    TextButton(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val newDate = Instant.ofEpochMilli(millis)
                                    .atZone(koreaZoneId)
                                    .toLocalDate()
                                homeViewModel.setSelectedDate(newDate)
                            }
                            showDatePicker = false
                        }
                    ) { Text("확인") }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) { Text("취소") }
                }
            ) { DatePicker(state = datePickerState) }
        }
    }
}