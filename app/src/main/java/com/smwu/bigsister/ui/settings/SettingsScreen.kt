package com.smwu.bigsister.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.ui.viewmodel.SettingsViewModel

/**
 * 설정 탭 화면 (PDF No. 9)
 */
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel = hiltViewModel(),
    onNavigateBack: () -> Unit
) {
    // ViewModel로부터 현재 설정 상태를 구독
    val sisterType by viewModel.sisterType.collectAsState()
    val pushAlarm by viewModel.pushAlarm.collectAsState()
    val voiceAlarm by viewModel.voiceAlarm.collectAsState()

    Scaffold(
        topBar = {
            // TODO: AppTopBar 구현 시 교체
            Text(
                "설정",
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            // --- 1. 언니 타입 선택 (PDF 9-1) [cite: 444-450] ---
            Text("언니 타입", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            SisterTypeSelector(
                selectedType = sisterType,
                onTypeSelected = { viewModel.setSisterType(it) }
            )

            Divider(modifier = Modifier.padding(vertical = 16.dp))

            // --- 2. 알림 설정 (PDF 9-2) [cite: 451-455] ---
            Text("알림", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            Spacer(Modifier.height(8.dp))
            SwitchSettingItem(
                title = "푸시 알림",
                subtitle = "기기에서 알림 받기",
                checked = pushAlarm,
                onCheckedChange = { viewModel.setPushAlarm(it) }
            )
            SwitchSettingItem(
                title = "음성 알림",
                subtitle = "텍스트 음성 변환 리마인더",
                checked = voiceAlarm,
                onCheckedChange = { viewModel.setVoiceAlarm(it) }
            )

            // TODO: 알림 강도, 타이밍 설정 [cite: 462-465]

            Spacer(Modifier.weight(1f))

            // --- 3. 정보 (PDF 9) [cite: 466-470] ---
            Text("정보", fontWeight = FontWeight.SemiBold, fontSize = 18.sp)
            InfoItem(title = "버전", value = "1.0.0") // [cite: 468]
            InfoItem(title = "빌드", value = "20251114") // [cite: 470]
            Spacer(Modifier.height(16.dp))
        }
    }
}

/**
 * '언니 타입' 라디오 버튼 그룹
 */
@Composable
private fun SisterTypeSelector(
    selectedType: String,
    onTypeSelected: (String) -> Unit
) {
    val types = listOf("츤데레", "현실적", "AI") // [cite: 445-450]
    Column(Modifier.selectableGroup()) {
        types.forEach { type ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .selectable(
                        selected = (type == selectedType),
                        onClick = { onTypeSelected(type) },
                        role = Role.RadioButton
                    )
                    .padding(vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                RadioButton(
                    selected = (type == selectedType),
                    onClick = null // Row의 onClick 사용
                )
                Text(
                    text = type,
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.padding(start = 16.dp)
                )
            }
        }
    }
}

/**
 * (예: 푸시 알림) 스위치 아이템
 */
@Composable
private fun SwitchSettingItem(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontSize = 16.sp)
            Text(subtitle, fontSize = 14.sp, color = Color.Gray)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

/**
 * (예: 버전) 정보 아이템
 */
@Composable
private fun InfoItem(title: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(title, fontSize = 16.sp)
        Text(value, fontSize = 16.sp, color = Color.Gray)
    }
}