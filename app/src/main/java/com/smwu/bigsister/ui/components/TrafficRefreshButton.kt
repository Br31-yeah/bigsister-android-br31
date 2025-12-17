package com.smwu.bigsister.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Refresh
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch

@Composable
fun TrafficRefreshButton(
    modifier: Modifier = Modifier,
    onRefresh: suspend () -> Unit
) {
    var isLoading by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Surface(
        shape = RoundedCornerShape(24.dp),
        color = Color(0xFFEEF0FB),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        modifier = modifier
            .clickable(enabled = !isLoading) {
                scope.launch {
                    isLoading = true
                    onRefresh()
                    isLoading = false
                }
            }
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Icon(
                imageVector = Icons.Rounded.Refresh,
                contentDescription = "교통 정보 새로고침",
                tint = Color(0xFF8B8FD9),
                modifier = Modifier
                    .size(16.dp)
                    .rotate(if (isLoading) 360f else 0f)
            )

            Text(
                text = if (isLoading) "업데이트 중…" else "교통 정보 새로고침",
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xFF8B8FD9)
            )
        }
    }
}