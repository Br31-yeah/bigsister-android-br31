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

@Composable
fun LiveModeScreen(
    viewModel: LiveModeViewModel = hiltViewModel(),
    onFinishRoutine: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    val backgroundColor by animateColorAsState(
        targetValue = if (uiState.isOvertime) Color.Red.copy(alpha = 0.1f) else Color.Transparent,
        animationSpec = if (uiState.isOvertime) {
            infiniteRepeatable(
                animation = tween(500),
                repeatMode = RepeatMode.Reverse
            )
        } else {
            tween(500)
        },
        label = "overtime-bg"
    )

    Scaffold { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(backgroundColor)
                .padding(paddingValues)
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                uiState.isLoading -> {
                    CircularProgressIndicator()
                }

                uiState.isFinished -> {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text(
                            text = "루틴 완료!",
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(Modifier.height(16.dp))
                        Button(onClick = onFinishRoutine) {
                            Text("홈으로 돌아가기")
                        }
                    }
                }

                uiState.currentStep != null -> {
                    LiveModeContent(
                        routineTitle = uiState.routineTitle,
                        stepProgressText =
                            "${uiState.currentStepIndex + 1}/${uiState.totalSteps} 단계",
                        stepName = uiState.currentStep!!.name,
                        timeText = formatTimer(
                            uiState.remainingTimeInMillis,
                            uiState.overtimeInMillis
                        ),
                        isOvertime = uiState.isOvertime,
                        naggingText =
                            if (uiState.isOvertime) "아직도 안끝났어? 대단하네." else "",
                        onSkipClick = { viewModel.skipStep() },
                        onCompleteClick = { viewModel.completeStep() }
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
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(routineTitle, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Text(stepProgressText, fontSize = 16.sp, color = Color.Gray)

        Spacer(Modifier.weight(0.5f))

        Icon(
            imageVector = Icons.Default.Place,
            contentDescription = stepName,
            modifier = Modifier.size(64.dp)
        )

        Spacer(Modifier.height(16.dp))

        Text(
            text = stepName,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        Text(
            text = timeText,
            fontSize = 60.sp,
            fontWeight = FontWeight.Bold,
            color = if (isOvertime) Color.Red else MaterialTheme.colorScheme.onSurface
        )

        LinearProgressIndicator(modifier = Modifier.fillMaxWidth())

        Spacer(Modifier.height(24.dp))

        Text(
            text = naggingText,
            fontSize = 16.sp,
            color = Color.Red,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.height(24.dp)
        )

        Spacer(Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceAround
        ) {
            TextButton(onClick = onSkipClick) {
                Text("▷ 건너뛰기", fontSize = 18.sp)
            }
            Button(onClick = onCompleteClick) {
                Text("✓ 완료", fontSize = 18.sp)
            }
        }
    }
}

private fun formatTimer(
    remainingMillis: Long,
    overtimeMillis: Long
): String {
    return if (overtimeMillis > 0) {
        val seconds = (overtimeMillis / 1000) % 60
        val minutes = overtimeMillis / (1000 * 60)
        "+${minutes}:${String.format("%02d", seconds)}"
    } else {
        val seconds = (remainingMillis / 1000) % 60
        val minutes = (remainingMillis / (1000 * 60)) % 60
        val hours = remainingMillis / (1000 * 60 * 60)
        if (hours > 0) {
            "${hours}:${String.format("%02d", minutes)}:${String.format("%02d", seconds)}"
        } else {
            "${minutes}:${String.format("%02d", seconds)}"
        }
    }
}