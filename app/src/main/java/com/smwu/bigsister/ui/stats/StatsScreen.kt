package com.smwu.bigsister.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material.icons.filled.TrackChanges
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.smwu.bigsister.ui.viewModel.StatsUiState
import com.smwu.bigsister.ui.viewModel.StatsViewModel
import com.smwu.bigsister.ui.viewModel.WeeklyStat
import kotlin.collections.map
import kotlin.collections.mapIndexed

/**
 * Stats Screen with ViewModel
 */
@Composable
fun StatsScreen(
    viewModel: StatsViewModel = hiltViewModel()
) {
    val uiState by viewModel.stats.collectAsStateWithLifecycle()
    StatsScreenContent(uiState = uiState)
}

/**
 * UI Content
 */
@Composable
fun StatsScreenContent(
    uiState: StatsUiState,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {

        Text(
            text = "나의 진행상황",
            style = MaterialTheme.typography.headlineSmall.copy(
                color = Color(0xFF111827),
                fontWeight = FontWeight.SemiBold
            )
        )

        // --- 상단 3개 카드 ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatSmallCard(
                iconBackground = Color(0xFFA9E8D4),
                iconTint = Color(0xFF2D6A5F),
                icon = Icons.Default.TrackChanges,
                valueText = "${uiState.punctualityRate}%",
                labelText = "정시 도착",
                modifier = Modifier.weight(1f)
            )

            StatSmallCard(
                iconBackground = Color(0xFFFFD93D),
                iconTint = Color(0xFF8B7300),
                icon = Icons.Default.ShowChart,
                valueText = "${uiState.avgLateness}분",
                labelText = "평균 지각",
                modifier = Modifier.weight(1f)
            )

            StatSmallCard(
                iconBackground = Color(0xFFE3E4FA),
                iconTint = Color(0xFF8B8FD9),
                icon = Icons.Default.EmojiEvents,
                valueText = "${uiState.streakDays}일",
                labelText = "연속 기록",
                modifier = Modifier.weight(1f)
            )
        }

        // --- 주간 성과 ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            shape = MaterialTheme.shapes.extraLarge,
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                Text(
                    text = "주간 성과",
                    style = MaterialTheme.typography.titleMedium.copy(
                        color = Color(0xFF111827),
                        fontWeight = FontWeight.SemiBold
                    )
                )

                Spacer(Modifier.height(12.dp))

                WeeklyStatsBarChart(
                    weeklyData = uiState.weeklyData,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                )

                Spacer(Modifier.height(12.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ) {
                    LegendDot(color = Color(0xFFA9E8D4), label = "정시")
                    Spacer(Modifier.width(16.dp))
                    LegendDot(color = Color(0xFFFF6B6B), label = "지각")
                }
            }
        }

        // --- 언니의 한마디 ---
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = Color(0xFFE3E4FA).copy(alpha = 0.1f)
            ),
            shape = MaterialTheme.shapes.extraLarge,
            border = CardDefaults.outlinedCardBorder().copy(
                width = 2.dp,
                brush = CardDefaults.outlinedCardBorder().brush
                    ?: androidx.compose.ui.graphics.SolidColor(Color(0xFFE3E4FA))
            )
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Text("💭", style = MaterialTheme.typography.headlineMedium)

                Spacer(Modifier.width(12.dp))

                Column {
                    Text(
                        "언니의 한마디",
                        style = MaterialTheme.typography.labelMedium.copy(
                            color = Color(0xFF6B7280)
                        )
                    )
                    Spacer(Modifier.height(4.dp))
                    Text(
                        uiState.sisterComment,
                        style = MaterialTheme.typography.bodyMedium.copy(
                            color = Color(0xFF111827)
                        )
                    )
                }
            }
        }
    }
}

/**
 * 작은 카드 UI
 */
@Composable
fun StatSmallCard(
    iconBackground: Color,
    iconTint: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    valueText: String,
    labelText: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(containerColor = Color.White),
        shape = MaterialTheme.shapes.extraLarge,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
        border = CardDefaults.outlinedCardBorder().copy(
            width = 2.dp,
            brush = CardDefaults.outlinedCardBorder().brush
                ?: androidx.compose.ui.graphics.SolidColor(Color(0xFFF3F4F6))
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .background(iconBackground)
                    .padding(6.dp),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, null, tint = iconTint)
            }

            Spacer(Modifier.height(6.dp))

            Text(
                valueText,
                style = MaterialTheme.typography.bodyLarge.copy(
                    color = Color(0xFF111827),
                    fontWeight = FontWeight.SemiBold
                )
            )

            Spacer(Modifier.height(2.dp))

            Text(
                labelText,
                style = MaterialTheme.typography.labelSmall.copy(
                    color = Color(0xFF6B7280)
                )
            )
        }
    }
}

/**
 * 범례 점
 */
@Composable
fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(Modifier.width(4.dp))
        Text(
            label,
            style = MaterialTheme.typography.labelSmall.copy(
                color = Color(0xFF4B5563)
            )
        )
    }
}

/**
 * MPAndroidChart 주간 성과 그래프
 */
@Composable
fun WeeklyStatsBarChart(
    weeklyData: List<WeeklyStat>,
    modifier: Modifier
) {
    val onTimeColor = Color(0xFFA9E8D4)
    val lateColor = Color(0xFFFF6B6B)

    AndroidView(
        modifier = modifier,
        factory = { context ->
            BarChart(context).apply {
                description.isEnabled = false
                legend.isEnabled = false
                setDrawGridBackground(false)
                setScaleEnabled(false)
                setPinchZoom(false)

                axisRight.isEnabled = false
                axisLeft.apply {
                    axisMinimum = 0f
                    setDrawGridLines(false)
                    textColor = Color(0xFF9CA3AF).toArgb()
                    textSize = 10f
                }

                xAxis.apply {
                    position = XAxis.XAxisPosition.BOTTOM
                    setDrawGridLines(false)
                    granularity = 1f
                    textColor = Color(0xFF9CA3AF).toArgb()
                    textSize = 10f
                }
            }
        },
        update = { chart ->
            val entries = weeklyData.mapIndexed { idx, item ->
                BarEntry(
                    idx.toFloat(),
                    floatArrayOf(item.onTime.toFloat(), item.late.toFloat())
                )
            }

            val dataSet = BarDataSet(entries, "").apply {
                setDrawValues(false)
                colors = listOf(onTimeColor.toArgb(), lateColor.toArgb())
                stackLabels = arrayOf("정시", "지각")
            }

            chart.xAxis.valueFormatter =
                IndexAxisValueFormatter(weeklyData.map { it.day })

            chart.data = BarData(dataSet).apply { barWidth = 0.5f }
            chart.invalidate()
        }
    )
}