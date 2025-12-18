package com.smwu.bigsister.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.DirectionsWalk
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.rounded.DirectionsBus
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
    onSearch: (String) -> Unit,
    onCurrentLocation: (String) -> Unit,
    onSelectTransitRoute: ((String) -> Unit)? = null
) {
    val canSearchRoute = step.from != null && step.to != null

    FigmaCard {
        Row(verticalAlignment = Alignment.CenterVertically) {
            FigmaInput(
                value = step.name,
                onValueChange = { viewModel.updateStep(step.copy(name = it)) },
                placeholder = "단계 이름",
                modifier = Modifier.weight(1f)
            )
            if (!step.isTransport) {
                FigmaInput(
                    value = step.baseDuration.toString(),
                    onValueChange = { viewModel.updateStep(step.copy(baseDuration = it.toLongOrNull() ?: 0L)) },
                    placeholder = "분",
                    modifier = Modifier.width(80.dp)
                )
            }
            IconButton(onClick = onDelete) { Icon(Icons.Outlined.Delete, null) }
        }

        if (step.isTransport) {
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Column(Modifier.weight(1f)) {
                    SecondaryButton(text = step.from?.substringBefore("|") ?: "출발지", onClick = { onSearch("FROM") }, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = { onCurrentLocation("FROM") }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("📍 현위치", fontSize = 11.sp) }
                }
                Column(Modifier.weight(1f)) {
                    SecondaryButton(text = step.to?.substringBefore("|") ?: "도착지", onClick = { onSearch("TO") }, modifier = Modifier.fillMaxWidth())
                    TextButton(onClick = { onCurrentLocation("TO") }, modifier = Modifier.align(Alignment.CenterHorizontally)) { Text("📍 현위치", fontSize = 11.sp) }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                TransportButton(Icons.Rounded.DirectionsCar, "자동차", step.transportMode == "driving") {
                    viewModel.updateStep(step.copy(transportMode = "driving"))
                    if (canSearchRoute) onSelectTransitRoute?.invoke("DRIVE")
                }
                TransportButton(Icons.Rounded.DirectionsBus, "대중교통", step.transportMode == "transit") {
                    viewModel.updateStep(step.copy(transportMode = "transit"))
                    if (canSearchRoute) onSelectTransitRoute?.invoke("TRANSIT")
                }
                TransportButton(Icons.AutoMirrored.Rounded.DirectionsWalk, "도보", step.transportMode == "walking") {
                    viewModel.updateStep(step.copy(transportMode = "walking"))
                    if (canSearchRoute) onSelectTransitRoute?.invoke("WALK")
                }
            }
            Spacer(Modifier.height(12.dp))
            Text(text = "예상 소요시간 약 ${step.baseDuration}분", fontSize = 13.sp, color = MutedForeground)
        }
        FigmaInput(value = step.memo ?: "", onValueChange = { viewModel.updateStep(step.copy(memo = it)) }, placeholder = "메모 (선택)", minLines = 2)
    }
}

@Composable
private fun TransportButton(icon: ImageVector, label: String, selected: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick, modifier = Modifier.height(38.dp), shape = RoundedCornerShape(12.dp),
        colors = ButtonDefaults.buttonColors(containerColor = if (selected) Color(0xFF8B8FD9) else Color(0xFFF1F3FD), contentColor = if (selected) Color.White else Color(0xFF8B8FD9)),
        contentPadding = PaddingValues(horizontal = 12.dp)
    ) {
        Icon(icon, null, modifier = Modifier.size(16.dp))
        Spacer(Modifier.width(4.dp))
        Text(label, fontSize = 12.sp)
    }
}