package com.smwu.bigsister.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smwu.bigsister.ui.theme.GrayBg
import com.smwu.bigsister.ui.theme.PurpleLight
import com.smwu.bigsister.ui.theme.PurplePrimary
import com.smwu.bigsister.ui.theme.TextGray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit
) {
    // UI 상태 (나중엔 DataStore/ViewModel과 연결 필요)
    var selectedType by remember { mutableStateOf("TSUNDERE") }
    var pushEnabled by remember { mutableStateOf(true) }
    var voiceEnabled by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Color.White,
        topBar = {
            TopAppBar(
                title = { Text("설정", fontWeight = FontWeight.Bold, fontSize = 20.sp) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "뒤로가기")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.White)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()), // 스크롤 가능하게
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            // 1. 언니 타입 섹션
            SectionContainer(title = "언니 타입") {
                TypeOption(
                    title = "츤데레",
                    desc = "장난스럽고 약간 비꼬는 스타일",
                    isSelected = selectedType == "TSUNDERE",
                    onClick = { selectedType = "TSUNDERE" }
                )
                Spacer(Modifier.height(12.dp))
                TypeOption(
                    title = "현실적",
                    desc = "실용적이고 직설적인 스타일",
                    isSelected = selectedType == "REALISTIC",
                    onClick = { selectedType = "REALISTIC" }
                )
                Spacer(Modifier.height(12.dp))
                TypeOption(
                    title = "AI",
                    desc = "데이터 기반 분석형",
                    isSelected = selectedType == "AI",
                    onClick = { selectedType = "AI" }
                )
            }

            // 2. 알림 섹션
            SectionContainer(title = "알림") {
                // 스위치 항목들
                SwitchRow(text = "푸시 알림", subText = "기기에서 알림 받기", checked = pushEnabled, onCheckedChange = { pushEnabled = it })
                Spacer(Modifier.height(24.dp))
                SwitchRow(text = "음성 알림", subText = "텍스트 음성 변환 리마인더", checked = voiceEnabled, onCheckedChange = { voiceEnabled = it })

                Spacer(Modifier.height(24.dp))

                // 드롭다운 항목들 (모양만 구현)
                DropdownRow(label = "강도", value = "보통 - 표준 알림")
                Spacer(Modifier.height(16.dp))
                DropdownRow(label = "타이밍", value = "마감 3분 전")
            }

            // 3. 정보 섹션
            SectionContainer(title = "정보") {
                InfoRow(label = "버전", value = "1.0.0")
                Spacer(Modifier.height(16.dp))
                InfoRow(label = "빌드", value = "2025.10.29")
            }

            Spacer(Modifier.height(40.dp)) // 하단 여백
        }
    }
}

// [컴포넌트] 섹션 컨테이너 (흰색 박스 + 제목 없음, 그냥 그룹핑용)
@Composable
fun SectionContainer(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White), // 배경 흰색
            border = androidx.compose.foundation.BorderStroke(1.dp, GrayBg), // 연한 테두리
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                content()
            }
        }
    }
}

// [컴포넌트] 언니 타입 선택 버튼
@Composable
fun TypeOption(title: String, desc: String, isSelected: Boolean, onClick: () -> Unit) {
    val bgColor = if (isSelected) PurpleLight else GrayBg
    val borderColor = if (isSelected) PurplePrimary else Color.Transparent

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable { onClick() }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(desc, fontSize = 14.sp, color = TextGray)
        }
    }
}

// [컴포넌트] 스위치 행
@Composable
fun SwitchRow(text: String, subText: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text, fontWeight = FontWeight.Medium, fontSize = 16.sp)
            Text(subText, fontSize = 13.sp, color = TextGray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = Color.Black, // Figma: 검정색 활성화
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

// [컴포넌트] 드롭다운 모양 행
@Composable
fun DropdownRow(label: String, value: String) {
    Column {
        Text(label, fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(GrayBg)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
        }
    }
}

// [컴포넌트] 정보 행 (버전, 빌드)
@Composable
fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, fontSize = 16.sp, fontWeight = FontWeight.Medium)
        Text(value, fontSize = 16.sp, color = TextGray)
    }
}