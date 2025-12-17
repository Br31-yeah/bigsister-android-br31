package com.smwu.bigsister.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.DirectionsWalk
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.smwu.bigsister.data.local.StepEntity
import com.smwu.bigsister.ui.theme.MutedForeground
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@Composable
fun StepCard(
    step: StepEntity,
    viewModel: RoutineViewModel,
    onDelete: () -> Unit,
    onSearch: (String) -> Unit
) {
    FigmaCard {

        /* ───────────── 상단: 이름 + 시간 + 삭제 ───────────── */

        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            FigmaInput(
                value = step.name,
                onValueChange = { viewModel.updateStep(step.copy(name = it)) },
                placeholder = "단계 이름",
                modifier = Modifier.weight(1f)
            )

            Spacer(Modifier.width(12.dp))

            // ⏱ 일반 Step만 시간 직접 입력 가능
            if (!step.isTransport) {
                FigmaInput(
                    value = step.baseDuration.toString(),
                    onValueChange = {
                        viewModel.updateStep(
                            step.copy(baseDuration = it.toLongOrNull() ?: 0L)
                        )
                    },
                    placeholder = "분",
                    modifier = Modifier.width(80.dp),
                    singleLine = true
                )
            }

            Spacer(Modifier.width(4.dp))

            IconButton(onClick = onDelete) {
                Icon(Icons.Outlined.Delete, contentDescription = "삭제")
            }
        }

        /* ───────────── 이동 단계 전용 UI ───────────── */

        if (step.isTransport) {
            Spacer(Modifier.height(16.dp))

            // 출발 / 도착
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                SecondaryButton(
                    text = step.from?.substringBefore("|") ?: "출발지",
                    onClick = { onSearch("FROM") },
                    modifier = Modifier.weight(1f)
                )
                SecondaryButton(
                    text = step.to?.substringBefore("|") ?: "도착지",
                    onClick = { onSearch("TO") },
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))

            // 이동 수단
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransportButton(
                    icon = Icons.Rounded.DirectionsCar,
                    label = "자동차",
                    selected = step.transportMode == "driving"
                ) {
                    viewModel.updateStep(step.copy(transportMode = "driving"))
                    viewModel.calculateDuration(step)
                }

                TransportButton(
                    icon = Icons.Rounded.DirectionsBus,
                    label = "대중교통",
                    selected = step.transportMode == "transit"
                ) {
                    viewModel.updateStep(step.copy(transportMode = "transit"))
                    viewModel.calculateDuration(step)
                }

                TransportButton(
                    icon = Icons.Rounded.DirectionsWalk,
                    label = "도보",
                    selected = step.transportMode == "walking"
                ) {
                    viewModel.updateStep(step.copy(transportMode = "walking"))
                    viewModel.calculateDuration(step)
                }
            }

            Spacer(Modifier.height(12.dp))

            // ⏱ 기준 vs 변경 시간 표시
            val base = step.baseDuration
            val current = step.calculatedDuration ?: base
            val diff = current - base

            Text(
                text =
                    if (base == 0L) {
                        "시간 계산 필요"
                    } else if (diff == 0L) {
                        "예상 소요시간 약 ${base}분"
                    } else {
                        "예상 ${current}분 (기준 ${base}분, ${if (diff > 0) "+" else ""}${diff}분)"
                    },
                fontSize = 14.sp,
                color = if (diff != 0L) Color.Red else MutedForeground
            )
        }

        /* ───────────── 메모 ───────────── */

        Spacer(Modifier.height(16.dp))

        FigmaInput(
            value = step.memo ?: "",
            onValueChange = { viewModel.updateStep(step.copy(memo = it)) },
            placeholder = "메모 (선택)",
            singleLine = false,
            minLines = 2
        )
    }
}

/* ───────────── 이동 수단 버튼 ───────────── */

@Composable
private fun TransportButton(
    icon: ImageVector,
    label: String,
    selected: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    SecondaryButton(
        text = label,
        onClick = onClick,
        modifier = modifier
    )
}