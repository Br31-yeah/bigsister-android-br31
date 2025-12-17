package com.smwu.bigsister.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smwu.bigsister.ui.theme.TextGray
import com.smwu.bigsister.ui.viewModel.RoutineEditState

@Composable
fun RoutineSummaryCard(
    state: RoutineEditState
) {
    // 기준 총 시간 (루틴 최초 생성/확정 시점)
    val baseTotal = state.steps.sumOf { it.baseDuration }

    // 현재 총 시간 (최근 교통 정보 반영)
    val currentTotal = state.steps.sumOf {
        it.calculatedDuration ?: it.baseDuration
    }

    val diff = currentTotal - baseTotal

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8FC)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {

            /* ───────── 왼쪽: 시간 요약 ───────── */
            Column {
                Text(
                    text = "총 소요시간",
                    color = TextGray,
                    fontSize = 14.sp
                )

                Text(
                    text = formatMinutes(currentTotal),
                    fontSize = 24.sp
                )

                // 기준 대비 변경이 있을 때만 표시
                if (diff != 0L) {
                    Text(
                        text = "기준 ${formatMinutes(baseTotal)} " +
                                "(${if (diff > 0) "+" else ""}${diff}분)",
                        fontSize = 14.sp,
                        color = Color.Red
                    )
                }
            }

            /* ───────── 오른쪽: 단계 수 ───────── */
            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "단계 수",
                    color = TextGray,
                    fontSize = 14.sp
                )
                Text(
                    text = "${state.steps.size}개",
                    fontSize = 24.sp
                )
            }
        }
    }
}

/* ───────── 공용 포맷 함수 ───────── */

private fun formatMinutes(minutes: Long): String =
    if (minutes >= 60) {
        "${minutes / 60}시간 ${minutes % 60}분"
    } else {
        "${minutes}분"
    }