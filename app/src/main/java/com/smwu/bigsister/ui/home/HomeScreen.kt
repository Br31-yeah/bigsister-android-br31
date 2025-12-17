package com.smwu.bigsister.ui.home

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    homeViewModel: HomeViewModel,
    reservationViewModel: ReservationViewModel = hiltViewModel(),
    onNavigateToReservationAdd: (String) -> Unit,
    onNavigateToRoutineList: () -> Unit,
    onNavigateToStats: () -> Unit
) {
    val selectedDate by homeViewModel.selectedDate.collectAsState()
    val todaySchedules by homeViewModel.todaySchedules.collectAsState()

    Scaffold(
        topBar = {
            HomeTopBar(
                currentMonth = selectedDate.monthValue,
                onCalendarClick = { /* TODO */ }
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

            if (todaySchedules.isEmpty()) {
                EmptyRoutineState {
                    onNavigateToReservationAdd(selectedDate.toString())
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.weight(1f)
                ) {
                    items(todaySchedules) { reservation ->
                        ReservationCard(
                            reservation = reservation,
                            onStart = { /* TODO */ },
                            onCancel = {
                                reservationViewModel.deleteReservation(reservation.id)
                            }
                        )
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

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
    }
}