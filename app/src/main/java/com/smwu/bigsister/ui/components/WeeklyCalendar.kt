package com.smwu.bigsister.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smwu.bigsister.ui.theme.PurpleLight
import com.smwu.bigsister.ui.theme.PurplePrimary
import com.smwu.bigsister.ui.theme.TextGray
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

@Composable
fun WeeklyCalendar(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    // [기능 추가] 스크롤해서 월이 바뀔 때 부모(화면)에게 알려주는 기능
    onVisibleMonthChanged: (Int) -> Unit = {}
) {
    // 1. [수정] 한국 시간 기준 '오늘' 구하기 (에뮬레이터 시간 무시)
    val todayInKorea = remember { LocalDate.now(ZoneId.of("Asia/Seoul")) }

    // 2. 날짜 범위 설정 (과거 100일 ~ 미래 100일)
    val dates = remember {
        (-100..100).map { todayInKorea.plusDays(it.toLong()) }
    }

    val listState = rememberLazyListState()

    // 3. [기능 추가] 스크롤 감지하여 현재 보이는 월(Month) 계산하기
    LaunchedEffect(listState) {
        snapshotFlow { listState.layoutInfo }
            .collect { layoutInfo ->
                val visibleItems = layoutInfo.visibleItemsInfo
                if (visibleItems.isNotEmpty()) {
                    val viewportCenter = (layoutInfo.viewportStartOffset + layoutInfo.viewportEndOffset) / 2

                    // 화면 중앙에 가장 가까운 날짜 찾기
                    val centerItem = visibleItems.minByOrNull { item ->
                        abs((item.offset + item.size / 2) - viewportCenter)
                    }

                    centerItem?.let {
                        val index = it.index
                        if (index in dates.indices) {
                            // 중앙에 있는 날짜의 월 정보를 밖으로 보냄
                            onVisibleMonthChanged(dates[index].monthValue)
                        }
                    }
                }
            }
    }

    // 4. 화면 너비를 측정하기 위해 BoxWithConstraints 사용
    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
        val screenWidth = maxWidth
        val density = LocalDensity.current

        // 5. 화면이 그려진 후 '오늘' 날짜를 정확히 중앙으로 이동시킴
        LaunchedEffect(Unit) {
            val todayIndex = dates.indexOfFirst { it == todayInKorea }

            // [중앙 정렬 계산 로직]
            val centerOffset = with(density) {
                (screenWidth.toPx() - 56.dp.toPx()) / 2
            }

            // 음수(-) 오프셋으로 중앙 정렬
            if (todayIndex != -1) {
                listState.scrollToItem(todayIndex, -centerOffset.toInt())
            }
        }

        LazyRow(
            state = listState,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(dates) { date ->
                DateCard(
                    date = date,
                    isSelected = date == selectedDate,
                    isToday = date == todayInKorea, // [수정] 한국 시간 기준 비교
                    onClick = { onDateSelected(date) }
                )
            }
        }
    }
}

@Composable
private fun DateCard(
    date: LocalDate,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit
) {
    val dayOfWeek =
        date.format(DateTimeFormatter.ofPattern("E", Locale.KOREAN))
    val dayNum = date.dayOfMonth.toString()

    val background =
        if (isSelected) PurpleLight else Color.White
    val border =
        if (isSelected) PurplePrimary else Color.Transparent
    val textColor =
        if (isSelected || isToday) PurplePrimary else Color.Black

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .width(56.dp)
            .background(background, RoundedCornerShape(16.dp))
            .border(1.dp, border, RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    ) {
        Text(
            text = dayOfWeek,
            fontSize = 14.sp,
            color = if (isSelected) PurplePrimary else TextGray
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = dayNum,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = textColor
        )
    }
}