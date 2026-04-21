package com.apartment.watertracker.core.ui.state

/**
 * Standard events for one-off actions like side effects (Snackbars, Navigation)
 */
sealed interface UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent
    data class ShowToast(val message: String) : UiEvent
    data class Navigate(val route: String) : UiEvent
    data object NavigateUp : UiEvent
    data object Unauthorized : UiEvent
}
