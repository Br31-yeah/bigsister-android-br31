package com.smwu.bigsister.ui.intro

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smwu.bigsister.ui.theme.PurpleLight
import com.smwu.bigsister.ui.theme.PurplePrimary

// 온보딩 전체 흐름을 관리하는 화면
// com/smwu/bigsister/ui/intro/OnboardingFlow.kt

@Composable
fun OnboardingFlow(
    onComplete: () -> Unit // RootNavigation에서 전달된 람다
) {
    var currentStep by remember { mutableStateOf(0) }
    var selectedType by remember { mutableStateOf("TSUNDERE") }

    when (currentStep) {
        0 -> WelcomeScreen(onNext = { currentStep = 1 })
        1 -> SisterTypeScreen(
            onNextClick = { type ->
                selectedType = type
                currentStep = 2
            }
        )
        2 -> NotificationScreen(onNext = onComplete) // ✅ 여기서 onComplete 실행
    }
}

// 1. 시작하기 화면 (Big Sister 로고)
@Composable
fun WelcomeScreen(onNext: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            // 로고 (텍스트 또는 이미지)
            Text("Big Sister", fontSize = 32.sp, fontWeight = FontWeight.Bold, color = PurplePrimary)
            Spacer(Modifier.height(16.dp))
            Text(
                "빅 시스터 코치와 함께 지각 없는 하루를 만들어보세요!",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 24.sp
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleLight, contentColor = PurplePrimary)
            ) {
                Text("시작하기", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}

// 3. 알림 설정 화면
@Composable
fun NotificationScreen(onNext: () -> Unit) {
    Surface(modifier = Modifier.fillMaxSize(), color = Color.White) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Spacer(Modifier.weight(1f))

            Text("알림 설정", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.Black)
            Spacer(Modifier.height(16.dp))
            Text(
                "제때 리마인더와 부드러운 알림으로 일정을 지켜보세요",
                textAlign = TextAlign.Center,
                color = Color.Gray,
                lineHeight = 24.sp
            )

            Spacer(Modifier.weight(1f))

            Button(
                onClick = onNext, // 실제 권한 요청 로직은 나중에 추가
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = PurpleLight, contentColor = PurplePrimary)
            ) {
                Text("알림 허용하고 시작", fontSize = 16.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(16.dp))

            TextButton(onClick = onNext) {
                Text("나중에 하기", color = Color.Gray)
            }
            Spacer(Modifier.height(32.dp))
        }
    }
}