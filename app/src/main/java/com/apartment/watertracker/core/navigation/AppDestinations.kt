package com.apartment.watertracker.core.navigation

sealed class AppDestination(val route: String) {
    data object Login : AppDestination("login")
    data object Dashboard : AppDestination("dashboard")
    data object ApartmentSetup : AppDestination("apartment_setup")
    data object ApartmentSwitch : AppDestination("apartment_switch")
    data object ApartmentAdmin : AppDestination("apartment_admin")
    data object Team : AppDestination("team")
    data object Vendors : AppDestination("vendors")
    data object Scan : AppDestination("scan")
    data object SupplyEntry : AppDestination("supply_entry/{vendorId}") {
        fun createRoute(vendorId: String): String = "supply_entry/$vendorId"
    }
    data object Reports : AppDestination("reports")
}

val primaryRoutes = setOf(
    AppDestination.Dashboard.route,
    AppDestination.Vendors.route,
    AppDestination.Scan.route,
    AppDestination.Reports.route,
)
