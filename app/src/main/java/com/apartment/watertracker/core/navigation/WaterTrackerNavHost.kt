package com.apartment.watertracker.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Assessment
import androidx.compose.material.icons.outlined.Dashboard
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.QrCodeScanner
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hierarchy
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.apartment.watertracker.feature.admin.presentation.ApartmentSetupScreen
import com.apartment.watertracker.feature.admin.presentation.ApartmentSwitchScreen
import com.apartment.watertracker.feature.admin.presentation.AllApartmentsScreen
import com.apartment.watertracker.feature.admin.presentation.TeamScreen
import com.apartment.watertracker.feature.auth.presentation.LoginScreen
import com.apartment.watertracker.feature.dashboard.presentation.DashboardScreen
import com.apartment.watertracker.feature.entries.presentation.SupplyEntryScreen
import com.apartment.watertracker.feature.reports.presentation.ReportsScreen
import com.apartment.watertracker.feature.scan.presentation.ScanScreen
import com.apartment.watertracker.feature.vendors.presentation.VendorsScreen

@Composable
fun WaterTrackerNavHost() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = navBackStackEntry?.destination
    val guardViewModel: SubscriptionGuardViewModel = hiltViewModel()
    val subscriptionState = guardViewModel.uiState.collectAsStateWithLifecycle().value

    val primaryItems = listOf(
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

    LaunchedEffect(subscriptionState.isActive, currentDestination?.route) {
        val currentRoute = currentDestination?.route
        if (!subscriptionState.isActive && currentRoute in primaryRoutes - setOf(AppDestination.Dashboard.route)) {
            navController.navigate(AppDestination.ApartmentSwitch.route) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        modifier = Modifier,
        bottomBar = {
            val currentRoute = currentDestination?.route
            if (currentRoute in primaryRoutes && subscriptionState.isActive) {
                NavigationBar {
                    primaryItems.forEach { item ->
                        val selected = currentDestination
                            ?.hierarchy
                            ?.any { it.route == item.route } == true
                        NavigationBarItem(
                            selected = selected,
                            onClick = {
                                navController.navigate(item.route) {
                                    launchSingleTop = true
                                    restoreState = true
                                    popUpTo(AppDestination.Dashboard.route) {
                                        saveState = true
                                    }
                                }
                            },
                            icon = {
                                androidx.compose.material3.Icon(
                                    imageVector = item.icon,
                                    contentDescription = item.label,
                                )
                            },
                            label = { Text(text = item.label) },
                        )
                    }
                }
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestination.Login.route,
            modifier = Modifier,
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
                    onScanClick = { navController.navigate(AppDestination.Scan.route) },
                    onVendorsClick = { navController.navigate(AppDestination.Vendors.route) },
                    onReportsClick = { navController.navigate(AppDestination.Reports.route) },
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
                VendorsScreen()
            }

            composable(AppDestination.Scan.route) {
                ScanScreen(
                    onVendorScanned = { vendorId ->
                        navController.navigate(AppDestination.SupplyEntry.createRoute(vendorId))
                    },
                )
            }

            composable(AppDestination.SupplyEntry.route) {
                SupplyEntryScreen(
                    onEntrySaved = {
                        navController.navigate(AppDestination.Scan.route) {
                            popUpTo(AppDestination.Dashboard.route)
                        }
                    },
                )
            }

            composable(AppDestination.Reports.route) {
                ReportsScreen()
            }
        }
    }
}

private data class BottomNavItem(
    val route: String,
    val label: String,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
)
