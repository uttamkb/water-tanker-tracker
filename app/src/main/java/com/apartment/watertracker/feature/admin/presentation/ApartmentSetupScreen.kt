package com.apartment.watertracker.feature.admin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.apartment.watertracker.core.ui.components.PrimaryScaffold

@Composable
fun ApartmentSetupScreen(
    viewModel: ApartmentSetupViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    PrimaryScaffold(title = "Apartment Setup") { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(text = "Set the apartment name that operators and reports will use.")
            OutlinedTextField(
                value = uiState.apartmentName,
                onValueChange = viewModel::updateApartmentName,
                label = { Text(text = "Apartment name") },
            )
            Button(
                onClick = viewModel::saveApartmentName,
                enabled = uiState.apartmentName.isNotBlank() && !uiState.isSaving,
            ) {
                Text(text = if (uiState.isSaving) "Saving..." else "Save Apartment")
            }
            uiState.saveMessage?.let { message ->
                Text(text = message)
            }
        }
    }
}
