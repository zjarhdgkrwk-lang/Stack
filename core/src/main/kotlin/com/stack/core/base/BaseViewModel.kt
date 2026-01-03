package com.stack.core.base

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.util.DispatcherProvider
import com.stack.core.util.Result
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing MVI pattern with StateFlow.
 *
 * Features:
 * - Type-safe UI state management with StateFlow
 * - One-time event handling via Channel
 * - Coroutine exception handling
 * - SavedStateHandle integration for process death recovery
 *
 * Usage:
 * ```kotlin
 * data class MyScreenState(
 *     val items: List<Item> = emptyList(),
 *     val isLoading: Boolean = false
 * ) : UiState
 *
 * sealed interface MyScreenEvent : UiEvent {
 *     data class ItemClicked(val id: Long) : MyScreenEvent
 * }
 *
 * class MyViewModel @Inject constructor(
 *     savedStateHandle: SavedStateHandle,
 *     dispatchers: DispatcherProvider
 * ) : BaseViewModel<MyScreenState, MyScreenEvent>(
 *     initialState = MyScreenState(),
 *     savedStateHandle = savedStateHandle,
 *     dispatchers = dispatchers
 * ) {
 *     fun loadItems() {
 *         launchWithLoading {
 *             // load items
 *         }
 *     }
 * }
 * ```
 *
 * @param S UI State type
 * @param E UI Event type (one-time events)
 */
abstract class BaseViewModel<S : UiState, E : UiEvent>(
    initialState: S,
    protected val savedStateHandle: SavedStateHandle,
    protected val dispatchers: DispatcherProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow(initialState)
    val uiState: StateFlow<S> = _uiState.asStateFlow()

    /**
     * Channel for one-time UI events (snackbar, navigation, etc.)
     * Buffered to ensure events are not lost.
     */
    private val _events = Channel<E>(Channel.BUFFERED)
    val events: Flow<E> = _events.receiveAsFlow()

    /**
     * Current state value for convenience.
     */
    protected val currentState: S get() = _uiState.value

    /**
     * Coroutine exception handler for unhandled exceptions.
     * Override to customize error handling.
     */
    protected open val exceptionHandler = CoroutineExceptionHandler { _, throwable ->
        handleException(throwable)
    }

    /**
     * Update UI state atomically.
     * Thread-safe due to MutableStateFlow's internal synchronization.
     */
    protected fun updateState(reducer: S.() -> S) {
        _uiState.update { it.reducer() }
    }

    /**
     * Set state directly (use updateState for atomic updates).
     */
    protected fun setState(newState: S) {
        _uiState.value = newState
    }

    /**
     * Send one-time event to UI.
     * Will be collected and consumed only once.
     */
    protected fun sendEvent(event: E) {
        viewModelScope.launch(dispatchers.main) {
            _events.send(event)
        }
    }

    /**
     * Launch coroutine with error handling on IO dispatcher.
     */
    protected fun launchIO(block: suspend () -> Unit): Job {
        return viewModelScope.launch(dispatchers.io + exceptionHandler) {
            block()
        }
    }

    /**
     * Launch coroutine with error handling on Default dispatcher.
     */
    protected fun launchDefault(block: suspend () -> Unit): Job {
        return viewModelScope.launch(dispatchers.default + exceptionHandler) {
            block()
        }
    }

    /**
     * Launch coroutine with error handling on Main dispatcher.
     */
    protected fun launchMain(block: suspend () -> Unit): Job {
        return viewModelScope.launch(dispatchers.main + exceptionHandler) {
            block()
        }
    }

    /**
     * Handle Result from use cases with convenient callbacks.
     */
    protected inline fun <T> handleResult(
        result: Result<T>,
        onSuccess: (T) -> Unit,
        onError: (Throwable, String?) -> Unit = { _, _ -> }
    ) {
        when (result) {
            is Result.Success -> onSuccess(result.data)
            is Result.Error -> onError(result.exception, result.message)
            is Result.Loading -> { /* Usually handled by state */ }
        }
    }

    /**
     * Handle exceptions from coroutines.
     * Override to add custom error handling logic.
     */
    protected open fun handleException(throwable: Throwable) {
        // Default: log and ignore
        // Subclasses can override to show error state/snackbar
    }

    /**
     * Save value to SavedStateHandle for process death recovery.
     */
    protected fun <T> saveState(key: String, value: T) {
        savedStateHandle[key] = value
    }

    /**
     * Restore value from SavedStateHandle.
     */
    protected fun <T> restoreState(key: String): T? {
        return savedStateHandle[key]
    }

    /**
     * Get StateFlow from SavedStateHandle for automatic saving/restoring.
     */
    protected fun <T> savedStateFlow(key: String, initialValue: T): StateFlow<T> {
        return savedStateHandle.getStateFlow(key, initialValue)
    }
}
