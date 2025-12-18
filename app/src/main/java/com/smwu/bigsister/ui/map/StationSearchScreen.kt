package com.smwu.bigsister.ui.map

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.ui.viewModel.RoutineViewModel

@Composable
fun StationSearchScreen(
    viewModel: RoutineViewModel,
    onDismiss: () -> Unit,
    onStationSelected: (StationInfo) -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    // ✅ getValue 임포트와 초기값 명시로 에러 해결
    val searchResults: List<StationInfo> by viewModel.searchResults.collectAsState(initial = emptyList())

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("지하철역 입력") },
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchStation(searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = null)
                        }
                    }
                )
                Spacer(Modifier.width(8.dp))
                Button(onClick = onDismiss) { Text("닫기") }
            }

            Spacer(Modifier.height(16.dp))

            if (searchQuery.isNotBlank() && searchResults.isEmpty()) {
                Box(Modifier.fillMaxSize(), Alignment.Center) { Text("결과가 없습니다.") }
            } else {
                LazyColumn {
                    // ✅ items 인자 모호성 해결
                    items(items = searchResults) { station ->
                        Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable {
                            onStationSelected(station)
                            viewModel.clearSearchResults()
                        }) {
                            Column(Modifier.padding(16.dp)) {
                                Text(station.stationName, style = MaterialTheme.typography.titleMedium)
                                Text(station.laneName, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                        }
                    }
                }
            }
        }
    }
}