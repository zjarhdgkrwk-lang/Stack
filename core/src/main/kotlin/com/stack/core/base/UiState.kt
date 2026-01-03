package com.stack.core.base

/**
 * Base interface for UI state representation.
 * All screen states should implement this interface.
 */
interface UiState

/**
 * Common loading state representation.
 * Can be combined with screen-specific state.
 */
sealed interface LoadingState {
    data object Idle : LoadingState
    data object Loading : LoadingState
    data class Error(val message: String, val throwable: Throwable? = null) : LoadingState
    data object Success : LoadingState
}

/**
 * Wrapper for UI state with loading indicator.
 * Useful for screens that need to show loading overlay.
 */
data class StatefulData<T>(
    val data: T,
    val loadingState: LoadingState = LoadingState.Idle
) {
    val isLoading: Boolean get() = loadingState is LoadingState.Loading
    val isError: Boolean get() = loadingState is LoadingState.Error
    val errorMessage: String? get() = (loadingState as? LoadingState.Error)?.message
}

/**
 * Base sealed class for one-time UI events (snackbar, navigation, etc.)
 * Should be collected and consumed only once.
 */
interface UiEvent

/**
 * Common UI events that can be used across screens.
 */
sealed interface CommonUiEvent : UiEvent {
    data class ShowSnackbar(val message: String) : CommonUiEvent
    data class ShowToast(val message: String) : CommonUiEvent
    data object NavigateBack : CommonUiEvent
}
