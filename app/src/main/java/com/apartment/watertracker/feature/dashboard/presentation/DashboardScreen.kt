package com.apartment.watertracker.feature.dashboard.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Apartment
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Groups
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.domain.model.UserRole

@Composable
fun DashboardScreen(
    onScanClick: () -> Unit,
    onVendorsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onApartmentSetupClick: () -> Unit,
    onApartmentSwitchClick: () -> Unit,
    onApartmentAdminClick: () -> Unit,
    onTeamClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiState = viewModel.uiState.collectAsStateWithLifecycle().value
    val primaryActions = listOf(
        DashboardAction(
            title = "Scan Tanker",
            subtitle = "Start a fresh entry from vendor QR",
            icon = Icons.Outlined.QrCodeScanner,
            onClick = onScanClick,
        ),
        DashboardAction(
            title = "Vendor Desk",
            subtitle = "Register suppliers and print QR",
            icon = Icons.Outlined.LocalShipping,
            onClick = onVendorsClick,
        ),
        DashboardAction(
            title = "Monthly Reports",
            subtitle = "Review tanker counts and duplicates",
            icon = Icons.Outlined.Assessment,
            onClick = onReportsClick,
        ),
    )
    val adminActions = listOf(
        DashboardAction(
            title = "Apartment Setup",
            subtitle = "Set apartment name and profile",
            icon = Icons.Outlined.Apartment,
            onClick = onApartmentSetupClick,
        ),
        DashboardAction(
            title = "Switch Apartment",
            subtitle = "Move to another building you manage",
            icon = Icons.Outlined.Apartment,
            onClick = onApartmentSwitchClick,
        ),
        DashboardAction(
            title = "Team Access",
            subtitle = "Invite operators and manage access",
            icon = Icons.Outlined.Groups,
            onClick = onTeamClick,
        ),
        DashboardAction(
            title = "All Apartments",
            subtitle = "Owner-level control of subscriptions",
            icon = Icons.Outlined.Assessment,
            onClick = onApartmentAdminClick,
        ),
    )

    PrimaryScaffold(title = "Dashboard") { paddingValues ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item {
                HeroPanel(
                    userName = uiState.userName,
                    apartmentName = uiState.apartmentName,
                    userRole = uiState.userRole,
                )
            }
            item {
                StatsStrip(
                    todayTankers = uiState.todayTankers,
                    monthTankers = uiState.monthTankers,
                    activeVendors = uiState.activeVendors,
                )
            }
            if (!uiState.subscriptionActive) {
                item {
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                        ),
                        shape = MaterialTheme.shapes.extraLarge,
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp),
                        ) {
                            Text(
                                text = "Subscription inactive",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Text(
                                text = "Access is blocked for this apartment. Please renew or switch to an active apartment.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onErrorContainer,
                            )
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = onApartmentSwitchClick,
                            ) {
                                Text("Switch apartment")
                            }
                        }
                    }
                }
            } else {
                item {
                    SectionTitle(
                        title = "Daily operations",
                        subtitle = "These are the actions your staff will use most often.",
                    )
                }
                item {
                    ActionGrid(actions = primaryActions)
                }
                if (uiState.userRole == UserRole.ADMIN) {
                    item {
                        SectionTitle(
                            title = "Admin controls",
                            subtitle = "Configuration and team access stay separate from daily entry work.",
                        )
                    }
                    item {
                        ActionGrid(actions = adminActions)
                    }
                }
            }
        }
    }
}

@Composable
private fun HeroPanel(
    userName: String,
    apartmentName: String,
    userRole: UserRole,
) {
    Card(
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text(
                    text = "Welcome back, $userName",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = apartmentName,
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Box(
                    modifier = Modifier
                        .background(
                            color = MaterialTheme.colorScheme.primaryContainer,
                            shape = RoundedCornerShape(999.dp),
                        )
                        .padding(horizontal = 12.dp, vertical = 6.dp),
                ) {
                    Text(
                        text = "Role: ${userRole.displayLabel()}",
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.labelLarge,
                    )
                }
            }
        }
    }
}

@Composable
private fun StatsStrip(
    todayTankers: Int,
    monthTankers: Int,
    activeVendors: Int,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Today",
            value = todayTankers.toString(),
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "This month",
            value = monthTankers.toString(),
        )
        StatCard(
            modifier = Modifier.weight(1f),
            label = "Vendors",
            value = activeVendors.toString(),
        )
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        shape = MaterialTheme.shapes.large,
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    subtitle: String,
) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun ActionGrid(actions: List<DashboardAction>) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(2),
        modifier = Modifier
            .fillMaxWidth()
            .height(gridHeightFor(actions.size)),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
        userScrollEnabled = false,
    ) {
        items(actions) { action ->
            ActionCard(action = action)
        }
    }
}

@Composable
private fun ActionCard(action: DashboardAction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = action.onClick),
        shape = MaterialTheme.shapes.extraLarge,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(44.dp)
                    .height(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.secondaryContainer,
                        shape = MaterialTheme.shapes.large,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

private data class DashboardAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
)

private fun UserRole.displayLabel(): String = when (this) {
    UserRole.ADMIN -> "Admin"
    UserRole.OPERATOR -> "Operator"
}

private fun gridHeightFor(itemCount: Int): androidx.compose.ui.unit.Dp {
    val rows = ((itemCount - 1) / 2) + 1
    return (rows * 174).dp
}
