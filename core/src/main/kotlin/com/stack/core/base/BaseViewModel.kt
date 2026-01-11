package com.stack.core.base

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * Base ViewModel implementing MVI pattern with State-Intent-Effect architecture.
 *
 * This is a simplified, MVI-focused base class that:
 * - Manages immutable UI state with StateFlow
 * - Handles user intents (actions)
 * - Emits one-time side effects via Channel
 *
 * @param S State type - immutable data class representing UI state
 * @param I Intent type - sealed class representing user actions
 * @param E Effect type - sealed class representing one-time side effects
 */
abstract class BaseViewModel<S, I, E>(
    initialState: S
) : ViewModel() {

    private val _state = MutableStateFlow(initialState)
    val state: StateFlow<S> = _state.asStateFlow()

    private val _effect = Channel<E>(Channel.BUFFERED)
    val effect: Flow<E> = _effect.receiveAsFlow()

    /**
     * Handle user intent (action).
     * Subclasses must implement this to process all possible intents.
     */
    abstract fun handleIntent(intent: I)

    /**
     * Convenience method to send intent from UI.
     * Can be called directly or via `onIntent` in Composables.
     */
    fun onIntent(intent: I) {
        handleIntent(intent)
    }

    /**
     * Update state atomically with a reducer function.
     * Thread-safe due to MutableStateFlow's internal synchronization.
     */
    protected fun updateState(reducer: S.() -> S) {
        _state.update { it.reducer() }
    }

    /**
     * Set state directly (prefer updateState for atomic updates).
     */
    protected fun setState(newState: S) {
        _state.value = newState
    }

    /**
     * Send one-time effect to UI.
     * Effects are consumed only once and should trigger side effects like:
     * - Navigation
     * - Showing toast/snackbar
     * - Launching system dialogs
     */
    protected fun sendEffect(effect: E) {
        viewModelScope.launch {
            _effect.send(effect)
        }
    }

    /**
     * Emit effect (alias for sendEffect for naming consistency).
     */
    protected fun emitEffect(effect: E) {
        sendEffect(effect)
    }
}
