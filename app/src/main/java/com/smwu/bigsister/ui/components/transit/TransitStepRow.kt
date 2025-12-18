package com.smwu.bigsister.ui.components.transit


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smwu.bigsister.data.model.transit.TransitStepUiModel
import com.smwu.bigsister.data.model.transit.backgroundColor
import com.smwu.bigsister.data.model.transit.icon

@Composable
fun TransitStepRow(
    step: TransitStepUiModel,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {

        /* ───────── 아이콘 ───────── */
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
                .background(step.mode.backgroundColor()),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = step.mode.icon(),
                fontSize = MaterialTheme.typography.bodyLarge.fontSize
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        /* ───────── 설명 ───────── */
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = step.description,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Medium,
                color = Color.Black
            )
        }

        /* ───────── 시간 ───────── */
        Text(
            text = step.durationText,
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray
        )
    }
}