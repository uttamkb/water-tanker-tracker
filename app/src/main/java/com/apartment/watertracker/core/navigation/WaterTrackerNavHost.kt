package com.apartment.watertracker.core.navigation

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apartment.watertracker.core.ui.components.GlassSurface
import com.apartment.watertracker.feature.admin.presentation.ApartmentSetupScreen
import com.apartment.watertracker.feature.admin.presentation.ApartmentSwitchScreen
import com.apartment.watertracker.feature.admin.presentation.AllApartmentsScreen
import com.apartment.watertracker.feature.admin.presentation.TeamScreen
import com.apartment.watertracker.feature.auth.presentation.LoginScreen
import com.apartment.watertracker.feature.dashboard.presentation.DashboardScreen
import com.apartment.watertracker.feature.entries.presentation.SupplyEntryScreen
import com.apartment.watertracker.feature.reports.presentation.EntryDetailScreen
import com.apartment.watertracker.feature.reports.presentation.ReportsScreen
import com.apartment.watertracker.feature.scan.presentation.ScanScreen
import com.apartment.watertracker.feature.vendors.presentation.VendorsScreen

@OptIn(ExperimentalSharedTransitionApi::class)
@Composable
fun WaterTrackerNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val guardViewModel: SubscriptionGuardViewModel = hiltViewModel()
    val subscriptionState = guardViewModel.uiState.collectAsStateWithLifecycle().value
    val roleViewModel: NavigationUserViewModel = hiltViewModel()
    val roleState = roleViewModel.uiState.collectAsStateWithLifecycle().value

    val allPrimaryItems = listOf(
        BottomNavItem(
            route = AppDestination.Dashboard.route,
            label = "Home",
            icon = Icons.Outlined.Dashboard,
        ),
        BottomNavItem(
            route = AppDestination.Vendors.route,
            label = "Vendors",
            icon = Icons.Outlined.LocalShipping,
        ),
        BottomNavItem(
            route = AppDestination.Scan.route,
            label = "Scan",
            icon = Icons.Outlined.QrCodeScanner,
        ),
        BottomNavItem(
            route = AppDestination.Reports.route,
            label = "Reports",
            icon = Icons.Outlined.Assessment,
        ),
    )
    val primaryItems = when (roleState.isOperator) {
        true -> allPrimaryItems.filter { it.route != AppDestination.Vendors.route }
        false -> allPrimaryItems
    }

    // Helper for top-level navigation (Bottom Nav & Dashboard cards)
    val navigateToTopLevel = { route: String ->
        navController.navigate(route) {
            launchSingleTop = true
            restoreState = true
            popUpTo(AppDestination.Dashboard.route) {
                saveState = true
            }
        }
    }

    LaunchedEffect(subscriptionState.isActive, currentDestination?.route) {
        val currentRoute = currentDestination?.route
        if (!subscriptionState.isActive && currentRoute in primaryRoutes - setOf(AppDestination.Dashboard.route)) {
            navController.navigate(AppDestination.ApartmentSwitch.route) {
                launchSingleTop = true
            }
        }
    }

    SharedTransitionLayout {
        Scaffold(
            modifier = Modifier,
            bottomBar = {
                val currentRoute = currentDestination?.route
                if (currentRoute in primaryRoutes && subscriptionState.isActive) {
                    GlassSurface(
                        modifier = Modifier
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        shape = RoundedCornerShape(28.dp),
                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.7f)
                    ) {
                        NavigationBar(
                            containerColor = Color.Transparent,
                            contentColor = MaterialTheme.colorScheme.onSurface,
                            tonalElevation = 0.dp
                        ) {
                            primaryItems.forEach { item ->
                                val selected = currentDestination?.hierarchy?.any { it.route == item.route } == true
                                NavigationBarItem(
                                    selected = selected,
                                    onClick = { navigateToTopLevel(item.route) },
                                    icon = {
                                        Icon(
                                            imageVector = item.icon,
                                            contentDescription = item.label,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    label = {
                                        Text(
                                            text = item.label,
                                            style = MaterialTheme.typography.labelSmall
                                        )
                                    },
                                    colors = NavigationBarItemDefaults.colors(
                                        selectedIconColor = MaterialTheme.colorScheme.primary,
                                        selectedTextColor = MaterialTheme.colorScheme.primary,
                                        unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                        indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f)
                                    )
                                )
                            }
                        }
                    }
                }
            },
        ) { innerPadding ->
            NavHost(
                navController = navController,
                startDestination = AppDestination.Login.route,
                modifier = Modifier.padding(innerPadding),
                enterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(400)) },
                exitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(400)) },
                popEnterTransition = { fadeIn(animationSpec = tween(400)) + slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(400)) },
                popExitTransition = { fadeOut(animationSpec = tween(400)) + slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(400)) }
            ) {
                composable(AppDestination.Login.route) {
                    LoginScreen(
                        onSignedIn = {
                            navController.navigate(AppDestination.Dashboard.route) {
                                popUpTo(AppDestination.Login.route) { inclusive = true }
                            }
                        },
                    )
                }

                composable(AppDestination.Dashboard.route) {
                    DashboardScreen(
                        onScanClick = { navigateToTopLevel(AppDestination.Scan.route) },
                        onVendorsClick = { navigateToTopLevel(AppDestination.Vendors.route) },
                        onReportsClick = { navigateToTopLevel(AppDestination.Reports.route) },
                        onAnalyticsClick = { navigateToTopLevel(AppDestination.Analytics.route) },
                        onRequestTankerClick = { navigateToTopLevel(AppDestination.RequestTanker.route) },
                        onBillingClick = { navigateToTopLevel(AppDestination.Billing.route) },
                        onApartmentSetupClick = { navController.navigate(AppDestination.ApartmentSetup.route) },
                        onApartmentSwitchClick = { navController.navigate(AppDestination.ApartmentSwitch.route) },
                        onApartmentAdminClick = { navController.navigate(AppDestination.ApartmentAdmin.route) },
                        onTeamClick = { navController.navigate(AppDestination.Team.route) },
                    )
                }

                composable(AppDestination.ApartmentSetup.route) {
                    ApartmentSetupScreen()
                }

                composable(AppDestination.ApartmentSwitch.route) {
                    ApartmentSwitchScreen()
                }

                composable(AppDestination.ApartmentAdmin.route) {
                    AllApartmentsScreen()
                }
                composable(AppDestination.Team.route) {
                    TeamScreen()
                }

                composable(AppDestination.Vendors.route) {
                    VendorsScreen(
                        onBackClick = { navigateToTopLevel(AppDestination.Dashboard.route) }
                    )
                }

                composable(AppDestination.Scan.route) {
                    ScanScreen(
                        onVendorScanned = { vendorId ->
                            navController.navigate(AppDestination.SupplyEntry.createRoute(vendorId))
                        },
                        onBackClick = { navigateToTopLevel(AppDestination.Dashboard.route) }
                    )
                }

                composable(AppDestination.SupplyEntry.route) {
                    SupplyEntryScreen(
                        onEntrySaved = {
                            navController.navigate(AppDestination.Scan.route) {
                                popUpTo(AppDestination.Dashboard.route)
                            }
                        },
                        onBackClick = {
                            navController.popBackStack()
                        }
                    )
                }

                composable(AppDestination.Reports.route) {
                    ReportsScreen(
                        onBackClick = { navigateToTopLevel(AppDestination.Dashboard.route) },
                        onEntryClick = { entryId ->
                            navController.navigate(AppDestination.EntryDetail.createRoute(entryId))
                        }
                    )
                }
                
                composable(AppDestination.EntryDetail.route) {
                    EntryDetailScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(AppDestination.Analytics.route) {
                    com.apartment.watertracker.feature.analytics.presentation.AnalyticsScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(AppDestination.RequestTanker.route) {
                    com.apartment.watertracker.feature.marketplace.presentation.RequestTankerScreen(
                        onBackClick = { navController.popBackStack() },
                        onViewBids = { requestId ->
                            navController.navigate(AppDestination.Bids.createRoute(requestId))
                        }
                    )
                }

                composable(AppDestination.Bids.route) {
                    com.apartment.watertracker.feature.marketplace.presentation.BidsScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }

                composable(AppDestination.Billing.route) {
                    com.apartment.watertracker.feature.marketplace.presentation.BillingScreen(
                        onBackClick = { navController.popBackStack() }
                    )
                }
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
