package com.smwu.bigsister.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
    onSettingsClick: () -> Unit
) {
    val selectedDate by homeViewModel.selectedDate.collectAsState()
    val todaySchedules by homeViewModel.todaySchedules.collectAsState()

    var showDatePicker by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            HomeTopBar(
                currentMonth = selectedDate.monthValue,
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

            /* ---------- 오늘 일정 (핵심 영역) ---------- */
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
                                    // TODO: 즉시 시작 (LiveMode 연결 예정)
                                },
                                onCancel = {
                                    reservationViewModel.deleteReservation(reservation.id)
                                }
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            /* ---------- 예약 추가 버튼 ---------- */
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

        /* ---------- 날짜 선택 다이얼로그 ---------- */
        if (showDatePicker) {
            val datePickerState = rememberDatePickerState(
                initialSelectedDateMillis = selectedDate
                    .atStartOfDay(ZoneId.systemDefault())
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
                                    .atZone(ZoneId.systemDefault())
                                    .toLocalDate()
                                homeViewModel.setSelectedDate(newDate)
                            }
                            showDatePicker = false
                        }
                    ) {
                        Text("확인")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showDatePicker = false }) {
                        Text("취소")
                    }
                }
            ) {
                DatePicker(state = datePickerState)
            }
        }
    }
}