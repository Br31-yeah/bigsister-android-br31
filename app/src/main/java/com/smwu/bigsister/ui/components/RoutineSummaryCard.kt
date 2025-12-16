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
    val totalMinutes = state.steps.sumOf { it.duration }

    val totalText =
        if (totalMinutes >= 60) {
            "${totalMinutes / 60}시간 ${totalMinutes % 60}분"
        } else {
            "${totalMinutes}분"
        }

    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFF8F8FC)
        )
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("총 소요시간", color = TextGray)
                Text(totalText, fontSize = 24.sp)
            }

            Column(horizontalAlignment = Alignment.End) {
                Text("단계 수", color = TextGray)
                Text("${state.steps.size}개", fontSize = 24.sp)
            }
        }
    }
}