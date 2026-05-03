package com.apartment.watertracker.feature.dashboard.presentation

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.assertIsDisplayed
import com.apartment.watertracker.core.ui.theme.WaterTrackerTheme
import com.apartment.watertracker.domain.model.UserRole
import com.apartment.watertracker.core.ui.state.UiState
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class DashboardUiTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun securityGuard_sees_only_assigned_actions() {
        val guardState = DashboardUiState(
            userRole = UserRole.SECURITY_GUARD
        )
        
        composeTestRule.setContent {
            WaterTrackerTheme {
                StatelessDashboardContent(
                    uiStateWrapper = UiState.Success(guardState),
                    onRefresh = {},
                    onScanClick = {},
                    onVendorsClick = {},
                    onReportsClick = {},
                    onAnalyticsClick = {},
                    onRequestTankerClick = {},
                    onBillingClick = {},
                    onApartmentSetupClick = {},
                    onApartmentSwitchClick = {},
                    onApartmentAdminClick = {},
                    onTeamClick = {}
                )
            }
        }

        // Check if Scan Tanker is visible via testTag
        composeTestRule.onNodeWithTag("action_card_scan_tanker", useUnmergedTree = true).assertExists()
        
        // Check that Billing is NOT visible via testTag
        composeTestRule.onNodeWithTag("action_card_billing_&_payments", useUnmergedTree = true).assertDoesNotExist()
    }
}
