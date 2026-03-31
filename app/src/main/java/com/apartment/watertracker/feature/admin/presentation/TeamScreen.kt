package com.apartment.watertracker.feature.admin.presentation

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
fun TeamScreen(
    viewModel: TeamViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value

    PrimaryScaffold(title = "Team") { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            item {
                Text(text = "Invite operator")
            }
            item {
                OutlinedTextField(
                    value = uiState.inviteEmail,
                    onValueChange = viewModel::updateInviteEmail,
                    label = { Text(text = "Operator email") },
                )
            }
            item {
                Button(
                    onClick = viewModel::createInvite,
                    enabled = uiState.inviteEmail.isNotBlank() && !uiState.isSaving,
                ) {
                    Text(text = if (uiState.isSaving) "Saving..." else "Create Invite")
                }
            }
            uiState.message?.let { message ->
                item { Text(text = message) }
            }
            item {
                Text(text = "Current apartment users")
            }
            items(uiState.users, key = { it.id }) { user ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = user.name)
                    Text(text = "${user.email} • ${user.role.name}")
                }
            }
            item {
                Text(text = "Pending invites")
            }
            items(uiState.invites, key = { it.id }) { invite ->
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    Text(text = invite.email)
                    Text(text = "${invite.role.name} • ${invite.status}")
                }
            }
        }
    }
}
