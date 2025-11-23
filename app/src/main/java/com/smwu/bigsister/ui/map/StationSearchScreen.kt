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
import androidx.compose.material3.CardDefaults
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
import androidx.hilt.navigation.compose.hiltViewModel
import com.smwu.bigsister.data.network.StationInfo
import com.smwu.bigsister.ui.viewmodel.RoutineViewModel

@Composable
fun StationSearchScreen(
    viewModel: RoutineViewModel = hiltViewModel(),
    onDismiss: () -> Unit,           // '닫기' 눌렀을 때
    onStationSelected: (StationInfo) -> Unit // '역 선택' 했을 때
) {
    // 검색어 상태
    var searchQuery by remember { mutableStateOf("") }
    // 뷰모델에서 검색 결과 구독
    val searchResults by viewModel.searchResults.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(modifier = Modifier.padding(16.dp)) {

            // 1. 상단: 검색창과 닫기 버튼
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    modifier = Modifier.weight(1f),
                    placeholder = { Text("지하철역 입력 (예: 강남)") },
                    singleLine = true,
                    trailingIcon = {
                        IconButton(onClick = { viewModel.searchStation(searchQuery) }) {
                            Icon(Icons.Default.Search, contentDescription = "검색")
                        }
                    }
                )
                Spacer(modifier = Modifier.width(8.dp))
                Button(onClick = onDismiss) {
                    Text("닫기")
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. 중앙: 검색 결과 리스트
            if (searchResults.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("검색 결과가 없습니다.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            } else {
                LazyColumn {
                    items(searchResults) { station ->
                        StationItem(station = station) {
                            // 아이템을 클릭하면 -> 선택된 정보를 넘겨주고 -> 검색결과 초기화
                            onStationSelected(station)
                            viewModel.clearSearchResults()
                        }
                    }
                }
            }
        }
    }
}

// 리스트에 들어갈 카드 모양
@Composable
fun StationItem(station: StationInfo, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onClick() },
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = station.stationName,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = station.laneName, // 예: 수도권 2호선
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}