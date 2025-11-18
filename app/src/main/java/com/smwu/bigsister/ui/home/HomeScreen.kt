package com.smwu.bigsister.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.repository.ReservationRepository
import com.smwu.bigsister.ui.viewmodel.HomeViewModel

@Composable
fun HomeScreen(
    viewModel: HomeViewModel = hiltViewModel(),
    onNavigateToRoutineAdd: (String) -> Unit,
    onNavigateToRoutineList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLiveMode: (Int) -> Unit // ✅ [추가]
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        bottomBar = {
            Button(
                onClick = { onNavigateToRoutineAdd(uiState.selectedDate) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(56.dp)
            ) {
                Text("+ 예약 추가", fontSize = 16.sp)
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Text(
                "${uiState.selectedDate} 루틴",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )

            if (uiState.isLoading) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (uiState.scheduleList.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        "오늘 예약된 루틴이 없어요!",
                        textAlign = TextAlign.Center,
                        color = Color.Gray
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(uiState.scheduleList) { scheduledRoutine ->
                        ScheduledRoutineCard(
                            routine = scheduledRoutine,
                            // ✅ [수정] '바로 시작' 버튼에 routineId 전달
                            onStartClick = { onNavigateToLiveMode(scheduledRoutine.routineId) }
                        )
                    }
                }
            }

            // 임시 버튼들
            Spacer(Modifier.height(16.dp))
            Button(onClick = onNavigateToRoutineList, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("내 루틴 목록 보기 (임시)")
            }

            Button(onClick = onNavigateToSettings, modifier = Modifier.padding(horizontal = 16.dp)) {
                Text("설정 탭 가기 (임시)")
            }
        }
    }
}

@Composable
fun ScheduledRoutineCard(
    routine: ReservationRepository.ScheduledRoutineInfo,
    onStartClick: () -> Unit // ✅ [수정]
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(Modifier.padding(16.dp)) {
            Text(routine.routineTitle, fontWeight = FontWeight.Bold)
            Text("시작 시간: ${routine.startTime}")
            Spacer(Modifier.height(8.dp))
            // ✅ [수정] 'onClick' 연결
            Button(onClick = onStartClick) {
                Text("바로 시작")
            }
        }
    }
}