package com.apartment.watertracker.feature.dashboard.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.apartment.watertracker.core.ui.components.PrimaryScaffold
import com.apartment.watertracker.core.ui.state.UiState
import com.apartment.watertracker.domain.model.UserRole
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.ui.draw.clip
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.CloudDone
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.ExperimentalMaterial3Api
import com.apartment.watertracker.core.ui.components.PremiumCard
import com.apartment.watertracker.core.ui.components.shimmerEffect

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onScanClick: () -> Unit,
    onVendorsClick: () -> Unit,
    onReportsClick: () -> Unit,
    onAnalyticsClick: () -> Unit,
    onRequestTankerClick: () -> Unit,
    onBillingClick: () -> Unit,
    onApartmentSetupClick: () -> Unit,
    onApartmentSwitchClick: () -> Unit,
    onApartmentAdminClick: () -> Unit,
    onTeamClick: () -> Unit,
    viewModel: DashboardViewModel = hiltViewModel(),
) {
    val uiStateWrapper = viewModel.uiState.collectAsStateWithLifecycle().value
    val context = LocalContext.current
    
    val uiState = (uiStateWrapper as? UiState.Success)?.data ?: DashboardUiState()
    
    val allActions = listOf(
        DashboardAction(
            title = "Scan Tanker",
            subtitle = "Start a fresh entry from vendor QR",
            icon = Icons.Outlined.QrCodeScanner,
            onClick = onScanClick,
            roles = listOf(UserRole.SECURITY_GUARD, UserRole.SOCIETY_ADMIN, UserRole.FACILITY_MANAGER)
        ),
        DashboardAction(
            title = "Vendor Desk",
            subtitle = "Register suppliers and print QR",
            icon = Icons.Outlined.LocalShipping,
            onClick = onVendorsClick,
            roles = listOf(UserRole.SOCIETY_ADMIN, UserRole.FACILITY_MANAGER)
        ),
        DashboardAction(
            title = "Analytics",
            subtitle = "Track water quality and TDS trends",
            icon = Icons.Outlined.Assessment,
            onClick = onAnalyticsClick,
            roles = listOf(UserRole.SOCIETY_ADMIN, UserRole.FACILITY_MANAGER)
        ),
        DashboardAction(
            title = "Request Tanker",
            subtitle = "Broadcast needs to local vendors",
            icon = Icons.Outlined.Add,
            onClick = onRequestTankerClick,
            roles = listOf(UserRole.SOCIETY_ADMIN, UserRole.FACILITY_MANAGER)
        ),
        DashboardAction(
            title = "Billing & Payments",
            subtitle = "Generate invoices and settle accounts",
            icon = Icons.Outlined.Assessment,
            onClick = onBillingClick,
            roles = listOf(UserRole.SOCIETY_ADMIN)
        ),
    )

    val primaryActions = allActions.filter { uiState.userRole in it.roles }
    
    val adminActions = listOf(
        DashboardAction(
            title = "Apartment Setup",
            subtitle = "Set apartment name and profile",
            icon = Icons.Outlined.Apartment,
            onClick = onApartmentSetupClick,
            roles = listOf(UserRole.SOCIETY_ADMIN)
        ),
        DashboardAction(
            title = "Team Access",
            subtitle = "Invite operators and manage access",
            icon = Icons.Outlined.Groups,
            onClick = onTeamClick,
            roles = listOf(UserRole.SOCIETY_ADMIN)
        ),
        DashboardAction(
            title = "All Apartments",
            subtitle = "Owner-level control of subscriptions",
            icon = Icons.Outlined.Assessment,
            onClick = onApartmentAdminClick,
            roles = listOf(UserRole.PLATFORM_OWNER)
        ),
    ).filter { uiState.userRole in it.roles }

    PrimaryScaffold(title = "Dashboard") { paddingValues ->
        val isRefreshing = uiStateWrapper is UiState.Loading
        
        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { viewModel.refresh() },
            modifier = Modifier.padding(paddingValues)
        ) {
            Box(modifier = Modifier.fillMaxSize()) {
                when (uiStateWrapper) {
                    is UiState.Loading -> {
                        DashboardSkeleton()
                    }
                    is UiState.Error -> {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("Error: ${uiStateWrapper.message}")
                        }
                    }
                    is UiState.Empty, is UiState.Success -> {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxSize()
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
                                    monthVolumeLiters = uiState.monthVolumeLiters,
                                    activeVendors = uiState.activeVendors,
                                )
                            }
                            
                            item {
                                FinanceSummaryCard(
                                    monthSpend = uiState.monthSpend,
                                    avgPricePerLitre = uiState.avgPricePerLitre
                                )
                            }

                            // Intelligence: Activity Chart
                            item {
                                ActivityChart(data = uiState.last7DaysData)
                            }

                            // Smart Forecast Insight
                            uiState.forecast?.nextTankerDate?.let { date ->
                                item {
                                    ForecastInsightCard(
                                        nextDate = date,
                                        avgUsage = uiState.forecast.averageDailyUsageLiters,
                                        currentLevel = uiState.forecast.predictedCurrentLevelLiters,
                                        confidence = uiState.forecast.confidenceScore
                                    )
                                }
                            }

                            // Timeline View
                            if (uiState.recentDeliveries.isNotEmpty()) {
                                item {
                                    SectionTitle(
                                        title = "Recent Deliveries",
                                        subtitle = "Live feed of tankers arriving at your apartment.",
                                    )
                                }
                                item {
                                    TimelineView(deliveries = uiState.recentDeliveries)
                                }
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
                                            modifier = Modifier.padding(20.dp),
                                            verticalArrangement = Arrangement.spacedBy(12.dp),
                                        ) {
                                            Text(
                                                text = "Subscription inactive",
                                                style = MaterialTheme.typography.titleLarge,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                            Text(
                                                text = "Access is blocked for this apartment. Please renew or switch to an active apartment to continue tracking tankers.",
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.onErrorContainer,
                                            )
                                            Button(
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = {
                                                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse("https://watertracker.in/renew"))
                                                    context.startActivity(intent)
                                                },
                                            ) {
                                                Text("Renew Subscription via UPI")
                                            }
                                            Button(
                                                modifier = Modifier.fillMaxWidth(),
                                                onClick = onApartmentSwitchClick,
                                            ) {
                                                Text("Switch Apartment")
                                            }
                                        }
                                    }
                                }
                            } else {
                                item {
                                    SectionTitle(
                                        title = "Daily operations",
                                        subtitle = "Quick links for vendor and report management.",
                                    )
                                }
                                item {
                                    ActionGrid(actions = primaryActions)
                                }
                                if (uiState.userRole == UserRole.SOCIETY_ADMIN) {
                                    item {
                                        SectionTitle(
                                            title = "Admin controls",
                                            subtitle = "Configuration and team access stay separate.",
                                        )
                                    }
                                    item {
                                        ActionGrid(actions = adminActions)
                                    }
                                }
                            }
                            item { Spacer(modifier = Modifier.height(100.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun DashboardSkeleton(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Hero Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(180.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        
        // Stats Skeleton
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Box(modifier = Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.medium).shimmerEffect())
            Box(modifier = Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.medium).shimmerEffect())
            Box(modifier = Modifier.weight(1f).height(100.dp).clip(MaterialTheme.shapes.medium).shimmerEffect())
        }
        
        // Large Card Skeleton
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
                .clip(MaterialTheme.shapes.medium)
                .shimmerEffect()
        )
        
        // List Title Skeleton
        Box(
            modifier = Modifier
                .width(150.dp)
                .height(24.dp)
                .clip(MaterialTheme.shapes.small)
                .shimmerEffect()
        )
        
        // List Item Skeletons
        repeat(3) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .clip(MaterialTheme.shapes.medium)
                    .shimmerEffect()
            )
        }
    }
}

@Composable
private fun HeroPanel(
    userName: String,
    apartmentName: String,
    userRole: UserRole,
) {
    val brandGradient = Brush.linearGradient(
        colors = listOf(
            MaterialTheme.colorScheme.primary,
            MaterialTheme.colorScheme.secondary
        )
    )

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary),
        elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
    ) {
        Box(
            modifier = Modifier
                .background(brandGradient)
                .padding(24.dp),
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Good morning,",
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.7f),
                )
                Text(
                    text = userName,
                    style = MaterialTheme.typography.displayLarge,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Apartment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = apartmentName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onPrimary,
                    )
                }
                
                Surface(
                    modifier = Modifier.padding(top = 8.dp),
                    color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(999.dp),
                ) {
                    Text(
                        text = userRole.displayLabel().uppercase(),
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
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
    monthVolumeLiters: Long,
    activeVendors: Int,
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
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
        
        // Large volume card
        PremiumCard(
            modifier = Modifier.fillMaxWidth(),
            containerColor = MaterialTheme.colorScheme.primary,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column {
                    Text(
                        text = "Total Monthly Volume",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.8f)
                    )
                    Text(
                        text = "${monthVolumeLiters / 1000}k Liters",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                }
                Icon(
                    imageVector = Icons.Outlined.Assessment,
                    contentDescription = null,
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.2f)
                )
            }
        }
    }
}

@Composable
private fun StatCard(
    modifier: Modifier = Modifier,
    label: String,
    value: String,
) {
    PremiumCard(
        modifier = modifier,
        containerColor = MaterialTheme.colorScheme.surface,
    ) {
        Column(
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
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun FinanceSummaryCard(
    monthSpend: Double,
    avgPricePerLitre: Double
) {
    var isExpanded by remember { mutableStateOf(false) }

    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
        onClick = { isExpanded = !isExpanded },
        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Financial Overview",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                Text(
                    text = if (isExpanded) "Hide details" else "Tap for details",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f)
                )
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Column {
                    Text(
                        text = "Estimated Spend",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(java.util.Locale.US, "%.0f", monthSpend)}",
                        style = MaterialTheme.typography.displayLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "Avg Price",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = "₹${String.format(java.util.Locale.US, "%.2f", avgPricePerLitre)}/L",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            if (isExpanded) {
                HorizontalDivider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f))
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Current Rate (Avg)", style = MaterialTheme.typography.bodySmall)
                        Text("₹600 / tanker", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text("Projected Savings", style = MaterialTheme.typography.bodySmall)
                        Text("N/A", style = MaterialTheme.typography.bodySmall, color = Color(0xFF4CAF50))
                    }
                }
            }
        }
    }
}

@Composable
private fun ActivityChart(data: List<DailyChartData>) {
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column {
            Text(
                text = "Weekly Activity",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(24.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth().height(120.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val maxCount = data.maxOfOrNull { it.count }?.coerceAtLeast(1) ?: 1
                data.forEach { day ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.weight(1f)
                    ) {
                        val barHeight = (day.count.toFloat() / maxCount)
                        Box(
                            modifier = Modifier
                                .fillMaxHeight(barHeight)
                                .width(16.dp)
                                .background(
                                    brush = if (day.label == "Today") Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.primary,
                                            MaterialTheme.colorScheme.secondary
                                        )
                                    )
                                    else Brush.verticalGradient(
                                        colors = listOf(
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                            MaterialTheme.colorScheme.outline.copy(alpha = 0.1f)
                                        )
                                    ),
                                    shape = RoundedCornerShape(topStart = 6.dp, topEnd = 6.dp)
                                )
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = day.label,
                            style = MaterialTheme.typography.labelSmall,
                            color = if (day.label == "Today") MaterialTheme.colorScheme.primary 
                                    else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (day.label == "Today") FontWeight.Bold else FontWeight.Normal
                        )
                    }
                }
            }
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
    PremiumCard(
        modifier = Modifier.fillMaxWidth(),
        onClick = action.onClick,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f),
                        shape = MaterialTheme.shapes.small,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                Icon(
                    imageVector = action.icon,
                    contentDescription = action.title,
                    tint = MaterialTheme.colorScheme.primary,
                )
            }
            Text(
                text = action.title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = action.subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 16.sp
            )
        }
    }
}

@Composable
private fun TimelineView(deliveries: List<RecentDeliveryUiModel>) {
    var showAll by remember { mutableStateOf(false) }
    val visibleDeliveries = if (showAll) deliveries else deliveries.take(3)
    var expandedId by remember { mutableStateOf<String?>(null) }

    PremiumCard(
        modifier = Modifier
            .fillMaxWidth()
            .animateContentSize(),
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
            visibleDeliveries.forEach { delivery ->
                val isExpanded = expandedId == delivery.id
                
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { 
                            expandedId = if (isExpanded) null else delivery.id 
                        }
                        .animateContentSize()
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant,
                                    shape = CircleShape
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.History,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = delivery.vendorName,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${delivery.volumeLiters}L",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.secondary,
                                    fontWeight = FontWeight.Bold
                                )
                                if (delivery.isDuplicate) {
                                    Surface(
                                        color = MaterialTheme.colorScheme.error.copy(alpha = 0.1f),
                                        shape = RoundedCornerShape(4.dp)
                                    ) {
                                        Text(
                                            text = "DUPLICATE",
                                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                            style = MaterialTheme.typography.labelSmall.copy(fontSize = 9.sp),
                                            color = MaterialTheme.colorScheme.error,
                                            fontWeight = FontWeight.Black
                                        )
                                    }
                                }
                            }
                        }
                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = delivery.timeAgo,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                val syncColor = if (delivery.isSynced) Color(0xFF43A047) else Color(0xFFFFA000)
                                Icon(
                                    imageVector = if (delivery.isSynced) Icons.Outlined.CloudDone else Icons.Outlined.CloudUpload,
                                    contentDescription = if (delivery.isSynced) "Synced" else "Pending",
                                    tint = syncColor,
                                    modifier = Modifier.size(12.dp)
                                )
                                Text(
                                    text = if (delivery.isSynced) "Synced" else "Pending",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = syncColor,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = 52.dp)
                                .background(
                                    color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                                    shape = MaterialTheme.shapes.small
                                )
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text("Vehicle", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text(delivery.vehicleNumber ?: "N/A", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                            Column(horizontalAlignment = Alignment.End) {
                                Text("Hardness", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                                Text("${delivery.hardnessPpm} PPM", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }

                if (delivery != visibleDeliveries.last()) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.1f),
                        modifier = Modifier.padding(start = 52.dp)
                    )
                }
            }

            if (deliveries.size > 3) {
                TextButton(
                    onClick = { showAll = !showAll },
                    modifier = Modifier.align(Alignment.CenterHorizontally)
                ) {
                    Text(
                        text = if (showAll) "SHOW LESS" else "VIEW ALL (${deliveries.size})",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun ForecastInsightCard(
    nextDate: java.time.LocalDate,
    avgUsage: Long,
    currentLevel: Long,
    confidence: Float
) {
    val formatter = java.time.format.DateTimeFormatter.ofPattern("EEEE, dd MMM")
    
    PremiumCard(
        containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.4f)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Outlined.Assessment,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.tertiary,
                        modifier = Modifier.size(20.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Smart Prediction",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                
                if (currentLevel < 10000) {
                    Surface(
                        color = MaterialTheme.colorScheme.error,
                        shape = RoundedCornerShape(4.dp)
                    ) {
                        Text(
                            text = "LOW WATER",
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onError,
                            fontWeight = FontWeight.Black
                        )
                    }
                }
            }
            
            Text(
                text = "Next Tanker Expected on:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.7f)
            )
            Text(
                text = nextDate.format(formatter),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Black,
                color = MaterialTheme.colorScheme.onTertiaryContainer
            )
            
            HorizontalDivider(
                color = MaterialTheme.colorScheme.onTertiaryContainer.copy(alpha = 0.1f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text("Daily Avg", style = MaterialTheme.typography.labelSmall)
                    Text("${avgUsage}L", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
                Column {
                    Text("Est. Level", style = MaterialTheme.typography.labelSmall)
                    Text("${currentLevel}L", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold, color = if (currentLevel < 10000) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onTertiaryContainer)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text("Confidence", style = MaterialTheme.typography.labelSmall)
                    Text("${(confidence * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

private data class DashboardAction(
    val title: String,
    val subtitle: String,
    val icon: ImageVector,
    val onClick: () -> Unit,
    val roles: List<UserRole> = emptyList()
)

private fun UserRole.displayLabel(): String = when (this) {
    UserRole.SOCIETY_ADMIN -> "Admin"
    UserRole.SECURITY_GUARD -> "Guard"
    UserRole.FACILITY_MANAGER -> "Manager"
    UserRole.PLATFORM_OWNER -> "Owner"
}

private fun gridHeightFor(itemCount: Int): androidx.compose.ui.unit.Dp {
    val rows = ((itemCount - 1) / 2) + 1
    return (rows * 174).dp
}
