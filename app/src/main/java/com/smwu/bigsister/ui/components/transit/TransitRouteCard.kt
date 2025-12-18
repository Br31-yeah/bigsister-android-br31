package com.smwu.bigsister.ui.components.transit

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.smwu.bigsister.data.model.transit.TransitRouteUiModel

@Composable
fun TransitRouteCard(
    route: TransitRouteUiModel,
    isRecommended: Boolean,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor =
        if (isSelected) Color(0xFF8B8FD9) else Color(0xFFE0E0E0)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(20.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(borderColor)
        ),
        colors = CardDefaults.cardColors(
            containerColor =
                if (isSelected) Color(0xFFF5F6FF) else Color.White
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {

            /* ───────── Header ───────── */
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "총 ${route.totalDurationText}",
                    style = MaterialTheme.typography.titleMedium
                )

                if (isRecommended) {
                    Text(
                        text = "추천",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2D6A5F)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            /* ───────── Steps ───────── */
            route.steps.forEachIndexed { index, step ->
                TransitStepItem(
                    step = step,
                    isLast = index == route.steps.lastIndex
                )
            }
        }
    }
}