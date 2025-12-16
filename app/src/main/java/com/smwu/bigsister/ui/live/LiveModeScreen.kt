package com.smwu.bigsister.ui.live

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import com.smwu.bigsister.ui.viewModel.LiveModeViewModel

/**
 * '루틴 실행 화면' (Live Mode) (PDF No. 7) [cite: 376]
 * ViewModel의 상태에 따라 실시간 타이머와 단계를 표시합니다.
 */
@Composable
fun LiveModeScreen(
    viewModel: LiveModeViewModel = hiltViewModel(),
    onFinishRoutine: () -> Unit // 루틴 완료 후 뒤로가기
) {
    val uiState by viewModel.uiState.collectAsState()

    // 시간 초과 시 배경 깜빡임 효과 (PDF 7-5) [cite: 403]
    val backgroundColor by animateColorAsState(
        targetValue = if (uiState.isOvertime) Color.Red.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = if (uiState.isOvertime) {
            infiniteRepeatable(tween(500), RepeatMode.Reverse)
        } else {
            tween(500)
        }
    )

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor) // 깜빡임 효과 적용
                .padding(paddingValues)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }
                uiState.isFinished -> {
                    // 루틴 완료 화면
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("루틴 완료!", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onFinishRoutine) { Text("홈으로 돌아가기") }
                    }
                }
                uiState.currentStep != null -> {
                    // 루틴 진행 중 화면
                    LiveModeContent(
                        routineTitle = uiState.routineTitle,
                        stepProgressText = "${uiState.currentStepIndex + 1}/${uiState.totalSteps} 단계", // (PDF 2번) [cite: 372, 400]
                        stepName = uiState.currentStep!!.name, // (PDF 1번) [cite: 380]
                        timeText = formatTimer(uiState.remainingTimeInMillis, uiState.overtimeInMillis), // (PDF 1번) [cite: 382, 384]
                        isOvertime = uiState.isOvertime,
                        naggingText = if (uiState.isOvertime) "아직도 안끝났어? 대단하네." else "", // (PDF 4번) [cite: 396]
                        onSkipClick = { viewModel.skipStep() }, // (PDF 3번) [cite: 401]
                        onCompleteClick = { viewModel.completeStep() } // (PDF 3번) [cite: 401]
                    )
                }
            }
        }
    }
}

@Composable
private fun LiveModeContent(
    routineTitle: String,
    stepProgressText: String,
    stepName: String,
    timeText: String,
    isOvertime: Boolean,
    naggingText: String,
    onSkipClick: () -> Unit,
    onCompleteClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier.fillMaxSize()
    ) {
        // 상단 루틴 제목
        Text(text = routineTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        // 단계 진행 (e.g., 4/4 단계) [cite: 372]
        Text(text = stepProgressText, fontSize = 16.sp, color = Color.Gray)

        Spacer(Modifier.weight(0.5f))

        // 단계 아이콘 (임시)
        Icon(
            imageVector = Icons.Default.Place, // TODO: Step 아이콘으로 변경
            contentDescription = stepName,
            modifier = Modifier.size(64.dp)
        )
        Spacer(Modifier.height(16.dp))

        // 단계 이름 [cite: 380]
        Text(
            text = stepName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        // 실시간 타이머 [cite: 382, 384]
        Text(
            text = timeText,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOvertime) Color.Red else MaterialTheme.colorScheme.onSurface
        )

        // TODO: 단계별 진행 바 (PDF 7-2) [cite: 400]
        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        // 잔소리 텍스트 (PDF 7-4) [cite: 396]
        Text(
            text = naggingText,
            fontSize = 16.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.height(24.dp) // 공간 확보
        )

        Spacer(Modifier.weight(1f)) // 버튼을 하단에 배치

        // 건너뛰기 / 완료 버튼 (PDF 7-3) [cite: 401]
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton(onClick = onSkipClick) {
                Text("▷ 건너뛰기", fontSize = 18.sp)
            }

            Button(
                onClick = onCompleteClick,
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text("✓ 완료", fontSize = 18.sp)
            }
        }
    }
}

/**
 * 유틸리티 함수 (TimeUtils.kt로 이동 권장)
 * 남은 시간 또는 초과 시간을 "39:58" 또는 "+0:04" 형태로 포맷합니다. [cite: 382, 384]
 */
@Composable
private fun formatTimer(remainingMillis: Long, overtimeMillis: Long): String {
    return if (overtimeMillis > 0) {
        // "+0:04"
        val seconds = (overtimeMillis / 1000) % 60
        val minutes = (overtimeMillis / (1000 * 60))
        "+${minutes}:${String.format("%02d", seconds)}"
    } else {
        // "39:58"
        val seconds = (remainingMillis / 1000) % 60
        val minutes = (remainingMillis / (1000 * 60)) % 60
        val hours = (remainingMillis / (1000 * 60 * 60))
        if (hours > 0) {
            "${hours}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
        } else {
            "${minutes}:${String.format("%02d", seconds)}"
        }
    }
}