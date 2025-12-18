package com.smwu.bigsister.ui.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun HomeTopBar(
    currentMonth: Int,
    userName: String, // 추가된 파라미터
    onCalendarClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = "${currentMonth}월",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.Black
            )
            Text(
                text = "${userName}님의 루틴", // "지설님의 루틴" 으로 표시됨
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = Color.DarkGray
            )
        }

        IconButton(onClick = onCalendarClick) {
            Icon(
                imageVector = Icons.Outlined.CalendarMonth,
                contentDescription = "전체 달력",
                tint = Color.Black
            )
        }
    }
}