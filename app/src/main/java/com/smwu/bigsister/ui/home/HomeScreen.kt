package com.smwu.bigsister.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.smwu.bigsister.ui.theme.GrayBg
import com.smwu.bigsister.ui.theme.MintConfirm
import com.smwu.bigsister.ui.theme.PurpleLight
import com.smwu.bigsister.ui.theme.PurplePrimary
import com.smwu.bigsister.ui.theme.TextGray
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HomeScreen(
    onNavigateToRoutineAdd: (String) -> Unit,
    onNavigateToRoutineList: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToLiveMode: (Int) -> Unit
) {
    var selectedDate by remember { mutableStateOf(LocalDate.now()) }
    var showCalendarDialog by remember { mutableStateOf(false) } // 달력 팝업 상태

    val hasRoutine = false

    Scaffold(
        containerColor = Color.White,
        topBar = {
            HomeTopBar(
                currentMonth = selectedDate.monthValue,
                onCalendarClick = { showCalendarDialog = true } // 아이콘 클릭 시 팝업 열기
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
        ) {
            Spacer(Modifier.height(16.dp))

            // 1. 주간 달력
            WeeklyCalendar(
                selectedDate = selectedDate,
                onDateSelected = { selectedDate = it }
            )

            Spacer(Modifier.height(32.dp))

            // 2. 메인 콘텐츠
            if (hasRoutine) {
                Text("여기에 오늘의 루틴 카드가 들어갑니다.")
            } else {
                EmptyRoutineState(
                    onMakeRoutineClick = { onNavigateToRoutineAdd(selectedDate.toString()) }
                )
            }

            Spacer(Modifier.weight(1f))

            // 3. 하단 예약 추가 버튼
            Button(
                onClick = { onNavigateToRoutineAdd(selectedDate.toString()) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MintConfirm,
                    contentColor = Color.White
                )
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(8.dp))
                Text("예약 추가", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }

            Spacer(Modifier.height(32.dp))
        }

        // ▼▼▼ 달력 팝업 (조건부 표시) ▼▼▼
        if (showCalendarDialog) {
            CalendarDialog(
                initialDate = selectedDate,
                onDismiss = { showCalendarDialog = false },
                onDateSelected = { date ->
                    selectedDate = date
                    // 선택 후 팝업을 닫고 싶으면 여기서 showCalendarDialog = false 추가
                },
                onAddScheduleClick = {
                    showCalendarDialog = false
                    onNavigateToRoutineAdd(selectedDate.toString())
                }
            )
        }
    }
}

// ================= UI 컴포넌트들 =================

@Composable
fun HomeTopBar(currentMonth: Int, onCalendarClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "${currentMonth}월",
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        IconButton(onClick = onCalendarClick) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "전체 달력",
                tint = Color.Black,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}

@Composable
fun WeeklyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    // 오늘 기준 앞뒤 날짜 생성
    val dates = remember(selectedDate) {
        // 선택된 날짜가 화면에 보이도록 범위를 동적으로 조정하거나, 고정 범위를 쓸 수 있습니다.
        // 여기서는 오늘 기준으로 보여줍니다.
        val today = LocalDate.now()
        (-3..10).map { today.plusDays(it.toLong()) }
    }

    LazyRow(
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(dates) { date ->
            DateCard(
                date = date,
                isSelected = date == selectedDate,
                isToday = date == LocalDate.now(),
                onClick = { onDateSelected(date) }
            )
        }
    }
}

@Composable
fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayOfWeek = date.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN))
    val dayNum = date.dayOfMonth.toString()

    val backgroundColor = if (isSelected) PurpleLight else Color.White
    val textColor = if (isSelected) PurplePrimary else if (isToday) PurplePrimary else Color.Black
    val borderColor = if (isSelected) PurplePrimary else Color.Transparent

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .border(1.dp, borderColor, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = dayOfWeek,
            fontSize = 14.sp,
            color = if (isSelected) PurplePrimary else TextGray,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = dayNum,
            fontSize = 18.sp,
            color = textColor,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun EmptyRoutineState(onMakeRoutineClick: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(200.dp)
            .drawBehind {
                val stroke = Stroke(
                    width = 4f,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(20f, 20f), 0f)
                )
                drawRoundRect(
                    color = Color(0xFFE0E0E0),
                    style = stroke,
                    cornerRadius = CornerRadius(16.dp.toPx())
                )
            }
            .clip(RoundedCornerShape(16.dp))
            .clickable { onMakeRoutineClick() },
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Box(
                modifier = Modifier
                    .size(56.dp)
                    .clip(CircleShape)
                    .background(GrayBg),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = null,
                    tint = TextGray
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "오늘 예약된 루틴이 없어요!",
                fontSize = 16.sp,
                color = TextGray,
                fontWeight = FontWeight.Medium
            )
            Spacer(Modifier.height(12.dp))
            Surface(
                shape = RoundedCornerShape(20.dp),
                color = PurpleLight,
                modifier = Modifier.height(36.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(horizontal = 16.dp)
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, tint = PurplePrimary, modifier = Modifier.size(16.dp))
                    Spacer(Modifier.width(4.dp))
                    Text("새 루틴 예약", color = PurplePrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

// 🎨 [추가] Figma 디자인을 그대로 옮긴 달력 팝업
@Composable
fun CalendarDialog(
    initialDate: LocalDate,
    onDismiss: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    onAddScheduleClick: () -> Unit
) {
    // 현재 보고 있는 달 (초기값: 선택된 날짜의 달)
    var currentMonth by remember { mutableStateOf(YearMonth.from(initialDate)) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false) // 너비 커스텀 가능하게
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth(0.9f) // 화면 너비의 90%
                .padding(vertical = 24.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(defaultElevation = 8.dp)
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // 1. 헤더 (제목 + 닫기 버튼)
                Box(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "${currentMonth.monthValue}월 달력",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.align(Alignment.Center)
                    )
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier.align(Alignment.CenterEnd).size(24.dp)
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "닫기", tint = TextGray)
                    }
                }

                Spacer(Modifier.height(24.dp))

                // 2. 월 이동 네비게이션
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)
                ) {
                    IconButton(onClick = { currentMonth = currentMonth.minusMonths(1) }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "이전 달", tint = Color.Black)
                    }
                    Text(
                        text = "${currentMonth.year}年 ${currentMonth.monthValue}月", // Figma 텍스트 포맷 확인 필요
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    IconButton(onClick = { currentMonth = currentMonth.plusMonths(1) }) {
                        Icon(Icons.Default.ArrowForward, contentDescription = "다음 달", tint = Color.Black)
                    }
                }

                Spacer(Modifier.height(16.dp))

                // 3. 요일 헤더
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                    listOf("일", "월", "화", "수", "목", "금", "토").forEach { day ->
                        Text(
                            text = day,
                            modifier = Modifier.weight(1f),
                            textAlign = TextAlign.Center,
                            fontSize = 14.sp,
                            color = TextGray
                        )
                    }
                }

                Spacer(Modifier.height(12.dp))

                // 4. 날짜 그리드
                val daysInMonth = currentMonth.lengthOfMonth()
                val firstDayOfWeek = currentMonth.atDay(1).dayOfWeek.value % 7 // 일요일=0, 월요일=1...
                val totalSlots = daysInMonth + firstDayOfWeek

                LazyVerticalGrid(
                    columns = GridCells.Fixed(7),
                    modifier = Modifier.height(280.dp), // 그리드 높이 고정
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp) // 가로 간격
                ) {
                    // 빈 칸 채우기
                    items(firstDayOfWeek) {
                        Spacer(Modifier.size(40.dp))
                    }
                    // 날짜 채우기
                    items(daysInMonth) { day ->
                        val date = currentMonth.atDay(day + 1)
                        val isSelected = date == initialDate
                        val isToday = date == LocalDate.now()

                        Box(
                            modifier = Modifier
                                .aspectRatio(1f)
                                .clip(if (isSelected) RoundedCornerShape(12.dp) else CircleShape) // 선택은 사각, 오늘은 원
                                .background(
                                    when {
                                        isSelected -> MintConfirm // 선택된 날짜 (초록)
                                        isToday -> PurpleLight    // 오늘 날짜 (보라)
                                        else -> Color.Transparent
                                    }
                                )
                                .clickable { onDateSelected(date) },
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "${day + 1}",
                                fontSize = 14.sp,
                                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) Color.White else if (isToday) PurplePrimary else Color.Black
                            )
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                // 5. 하단 검색 버튼 ("11월 24일에 일정 추가")
                Surface(
                    onClick = onAddScheduleClick,
                    shape = RoundedCornerShape(16.dp),
                    color = GrayBg,
                    modifier = Modifier.fillMaxWidth().height(52.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, tint = TextGray)
                        Spacer(Modifier.width(8.dp))
                        Text(
                            text = "${initialDate.monthValue}월 ${initialDate.dayOfMonth}일에 일정 추가",
                            fontSize = 16.sp,
                            color = TextGray
                        )
                    }
                }
            }
        }
    }
}