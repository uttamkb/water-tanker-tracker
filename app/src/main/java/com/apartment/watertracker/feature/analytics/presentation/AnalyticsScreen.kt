package com.apartment.watertracker.feature.analytics.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.core.ui.components.QualityChart

@Composable
fun AnalyticsScreen(
    onBackClick: () -> Unit,
    viewModel: AnalyticsViewModel = hiltViewModel()
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    PrimaryScaffold(
        title = "Water Quality Trends",
        onBackClick = onBackClick
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            ChartSection(
                title = "pH Level Trend",
                data = uiState.phTrends,
                lineColor = MaterialTheme.colorScheme.primary
            )

            ChartSection(
                title = "TDS Level (PPM)",
                data = uiState.tdsTrends,
                lineColor = MaterialTheme.colorScheme.secondary
            )

            ChartSection(
                title = "Hardness (PPM)",
                data = uiState.hardnessTrends,
                lineColor = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

@Composable
private fun ChartSection(
    title: String,
    data: List<Float>,
    lineColor: androidx.compose.ui.graphics.Color
) {
    PremiumCard {
        Column(modifier = Modifier.padding(8.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
            )
            if (data.size < 2) {
                Text(
                    text = "Not enough data to display trend yet.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(16.dp)
                )
            } else {
                QualityChart(
                    dataPoints = data,
                    lineColor = lineColor
                )
            }
        }
    }
}
