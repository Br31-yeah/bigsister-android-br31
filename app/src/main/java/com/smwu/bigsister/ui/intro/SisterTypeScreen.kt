package com.smwu.bigsister.ui.intro

import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun SisterTypeScreen(
    onNextClick: (String) -> Unit // 다음 버튼 눌렀을 때
) {
    var selectedType by remember { mutableStateOf<String?>(null) }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color.White
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(40.dp))

            Text(
                text = "언니 타입 선택",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "가장 동기부여가 되는 성격을 선택하세요",
                fontSize = 14.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(32.dp))

            // 선택지 카드들
            TypeCard(
                title = "츤데레",
                description = "장난스럽고 약간 비꼬는 스타일\n\"아직도 준비 안 됐어? 대단하네.\"",
                isSelected = selectedType == "TSUNDERE",
                onClick = { selectedType = "TSUNDERE" }
            )
            Spacer(modifier = Modifier.height(16.dp))

            TypeCard(
                title = "현실적",
                description = "실용적이고 직설적인 스타일\n\"교통 혼잡해. 지금 출발해.\"",
                isSelected = selectedType == "REALISTIC",
                onClick = { selectedType = "REALISTIC" }
            )
            Spacer(modifier = Modifier.height(16.dp))

            TypeCard(
                title = "AI",
                description = "데이터 기반 분석형\n\"예상 지각 시간 +4분.\"",
                isSelected = selectedType == "AI",
                onClick = { selectedType = "AI" }
            )

            Spacer(modifier = Modifier.weight(1f))

            Button(
                onClick = { if (selectedType != null) onNextClick(selectedType!!) },
                modifier = Modifier.fillMaxWidth().height(56.dp),
                shape = RoundedCornerShape(12.dp),
                enabled = selectedType != null,
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFEBEBFA),
                    contentColor = Color.Black,
                    disabledContainerColor = Color(0xFFF0F0F0)
                )
            ) {
                Text(text = "계속하기", fontSize = 16.sp, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }
    }
}

@Composable
fun TypeCard(
    title: String,
    description: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF6A5ACD) else Color(0xFFE0E0E0)
    val borderWidth = if (isSelected) 2.dp else 1.dp

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .border(borderWidth, borderColor, RoundedCornerShape(12.dp)),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = title, fontWeight = FontWeight.Bold, fontSize = 16.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = description, fontSize = 14.sp, color = Color.Gray, lineHeight = 20.sp)
        }
    }
}