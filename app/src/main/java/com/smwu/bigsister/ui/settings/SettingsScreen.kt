package com.smwu.bigsister.ui.settings

import android.widget.Toast
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
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.PersonRemove
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.model.VoiceType
import com.smwu.bigsister.ui.theme.GrayBg
import com.smwu.bigsister.ui.theme.PurpleLight
import com.smwu.bigsister.ui.theme.PurplePrimary
import com.smwu.bigsister.ui.theme.TextGray
import com.smwu.bigsister.ui.viewModel.SettingsViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onNavigateBack: () -> Unit,
    onNavigateToLogin: () -> Unit,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    val context = LocalContext.current

    // ✅ 모든 상태를 ViewModel로부터 구독 (remember 제거)
    val sisterType by viewModel.sisterType.collectAsState()
    val pushEnabled by viewModel.pushAlarm.collectAsState()
    val voiceEnabled by viewModel.voiceAlarm.collectAsState()
    val selectedIntensity by viewModel.intensity.collectAsState()
    val selectedTiming by viewModel.timing.collectAsState()

    var showDeleteDialog by remember { mutableStateOf(false) }

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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            Spacer(Modifier.height(8.dp))

            SectionContainer(title = "언니 타입") {
                TypeOption(
                    title = "츤데레",
                    desc = "장난스럽고 약간 비꼬는 스타일",
                    isSelected = sisterType == "TSUNDERE",
                    onClick = { viewModel.setSisterType("TSUNDERE") },
                    onPreview = { viewModel.previewVoice(VoiceType.TSUNDERE) }
                )
                Spacer(Modifier.height(12.dp))
                TypeOption(
                    title = "현실적",
                    desc = "실용적이고 직설적인 스타일",
                    isSelected = sisterType == "REALISTIC",
                    onClick = { viewModel.setSisterType("REALISTIC") },
                    onPreview = { viewModel.previewVoice(VoiceType.REALISTIC) }
                )
                Spacer(Modifier.height(12.dp))
                TypeOption(
                    title = "AI",
                    desc = "데이터 기반 분석형",
                    isSelected = sisterType == "AI",
                    onClick = { viewModel.setSisterType("AI") },
                    onPreview = { viewModel.previewVoice(VoiceType.AI) }
                )
            }

            SectionContainer(title = "알림") {
                SwitchRow(text = "푸시 알림", subText = "기기에서 알림 받기", checked = pushEnabled, onCheckedChange = { viewModel.setPushAlarm(it) })
                Spacer(Modifier.height(24.dp))
                SwitchRow(text = "음성 알림", subText = "텍스트 음성 변환 리마인더", checked = voiceEnabled, onCheckedChange = { viewModel.setVoiceAlarm(it) })
                Spacer(Modifier.height(24.dp))

                DropdownRow(
                    label = "강도",
                    value = selectedIntensity,
                    options = listOf("약하게 - 조용한 가이드", "보통 - 표준 알림", "강하게 - 반복 독촉"),
                    onOptionSelected = { viewModel.setIntensity(it) } // ✅ 즉시 저장
                )

                Spacer(Modifier.height(16.dp))

                DropdownRow(
                    label = "타이밍",
                    value = selectedTiming,
                    options = listOf("마감 1분 전", "마감 3분 전", "마감 5분 전"),
                    onOptionSelected = { viewModel.setTiming(it) } // ✅ 즉시 저장
                )
            }

            SectionContainer(title = "정보") {
                InfoRow(label = "버전", value = "1.0.0")
                Spacer(Modifier.height(16.dp))
                InfoRow(label = "빌드", value = "2025.10.29")
            }

            SectionContainer(title = "계정") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {
                            viewModel.logout {
                                Toast.makeText(context, "로그아웃 되었습니다.", Toast.LENGTH_SHORT).show()
                                onNavigateToLogin()
                            }
                        }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("로그아웃", fontSize = 16.sp, fontWeight = FontWeight.Medium)
                    Icon(Icons.AutoMirrored.Filled.Logout, contentDescription = null, tint = TextGray, modifier = Modifier.size(20.dp))
                }

                Spacer(Modifier.height(8.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { showDeleteDialog = true }
                        .padding(vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("회원탈퇴", fontSize = 16.sp, fontWeight = FontWeight.Medium, color = Color.Red)
                    Icon(Icons.Default.PersonRemove, contentDescription = null, tint = Color.Red, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(Modifier.height(40.dp))
        }
    }

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("정말 탈퇴하시겠습니까?", fontWeight = FontWeight.Bold) },
            text = { Text("계정을 삭제하면 저장된 모든 루틴 데이터가 영구적으로 삭제됩니다.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        viewModel.deleteAccount(
                            onSuccess = {
                                Toast.makeText(context, "탈퇴가 완료되었습니다.", Toast.LENGTH_SHORT).show()
                                onNavigateToLogin()
                            },
                            onError = { errorMsg ->
                                Toast.makeText(context, errorMsg, Toast.LENGTH_LONG).show()
                            }
                        )
                    }
                ) {
                    Text("탈퇴하기", color = Color.Red, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("취소")
                }
            },
            containerColor = Color.White
        )
    }
}

@Composable
fun SectionContainer(title: String, content: @Composable () -> Unit) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
        Spacer(Modifier.height(12.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(1.dp, GrayBg),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) { content() }
        }
    }
}

@Composable
fun TypeOption(
    title: String,
    desc: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    onPreview: () -> Unit
) {
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
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.Black)
            Spacer(Modifier.height(4.dp))
            Text(desc, fontSize = 14.sp, color = TextGray)
        }

        IconButton(
            onClick = { onPreview() },
            modifier = Modifier.size(28.dp)
        ) {
            Icon(
                imageVector = Icons.Default.VolumeUp,
                contentDescription = "미리듣기",
                tint = if (isSelected) PurplePrimary else TextGray
            )
        }
    }
}

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
                checkedTrackColor = Color.Black,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = Color.LightGray
            )
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DropdownRow(
    label: String,
    value: String,
    options: List<String>,
    onOptionSelected: (String) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Column {
        Text(label, fontSize = 14.sp, color = TextGray, modifier = Modifier.padding(bottom = 8.dp))

        ExposedDropdownMenuBox(
            expanded = expanded,
            onExpandedChange = { expanded = !expanded }
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .menuAnchor()
                    .clip(RoundedCornerShape(8.dp))
                    .background(GrayBg)
                    .clickable { expanded = true }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(value, fontSize = 15.sp, fontWeight = FontWeight.Medium)
                Icon(
                    Icons.Default.KeyboardArrowDown,
                    contentDescription = null,
                    tint = TextGray,
                    modifier = Modifier.size(20.dp)
                )
            }

            ExposedDropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.background(Color.White)
            ) {
                options.forEach { option ->
                    DropdownMenuItem(
                        text = { Text(option) },
                        onClick = {
                            onOptionSelected(option)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

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