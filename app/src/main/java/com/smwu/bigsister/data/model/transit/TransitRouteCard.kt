package com.smwu.bigsister.data.model.transit

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smwu.bigsister.data.model.transit.TransitRouteUiModel
import com.smwu.bigsister.ui.components.transit.TransitStepRow

@Composable
fun TransitRouteCard(
    route: TransitRouteUiModel,
    isSelected: Boolean,
    isRecommended: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val borderColor =
        if (isSelected) Color(0xFF8B8FD9) else Color(0xFFE5E7EB)

    val backgroundColor =
        if (isSelected) Color(0x1A8B8FD9) else Color.White

    Column(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isSelected) 6.dp else 2.dp,
                shape = RoundedCornerShape(20.dp)
            )
            .background(backgroundColor, RoundedCornerShape(20.dp))
            .border(2.dp, borderColor, RoundedCornerShape(20.dp))
            .clickable { onClick() }
            .padding(20.dp)
    ) {

        /* ───────── Header ───────── */
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = "총 소요시간",
                style = MaterialTheme.typography.bodySmall,
                color = Color.Gray
            )

            Row {
                Text(
                    text = route.totalDurationText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold,
                    color = Color.Black
                )

                if (isRecommended) {
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "추천",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF2D6A5F),
                        modifier = Modifier
                            .background(
                                Color(0x3329C7A6),
                                RoundedCornerShape(12.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        /* ───────── Steps ───────── */
        Column {
            route.steps.forEach { step ->
                TransitStepRow(step = step)
            }
        }
    }
}