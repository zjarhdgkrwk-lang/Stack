package com.stack.feature.search

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.stack.core.player.StackPlayerManager
import com.stack.domain.model.Track
import com.stack.domain.repository.TrackRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val trackRepository: TrackRepository,
    private val playerManager: StackPlayerManager,
    private val savedStateHandle: SavedStateHandle
) : ViewModel() {

    companion object {
        private const val DEBOUNCE_DELAY_MS = 300L
        private const val MIN_QUERY_LENGTH = 2
        private const val MAX_RECENT_SEARCHES = 10
        private const val KEY_RECENT_SEARCHES = "recent_searches"
    }

    private val _uiState = MutableStateFlow(SearchUiState())
    val uiState: StateFlow<SearchUiState> = _uiState.asStateFlow()

    private val queryFlow = MutableStateFlow("")

    init {
        // Restore recent searches from SavedStateHandle
        val savedRecent = savedStateHandle.get<List<String>>(KEY_RECENT_SEARCHES) ?: emptyList()
        _uiState.update { it.copy(recentSearches = savedRecent) }

        setupSearchDebounce()
    }

    @OptIn(FlowPreview::class)
    private fun setupSearchDebounce() {
        queryFlow
            .debounce(DEBOUNCE_DELAY_MS)
            .distinctUntilChanged()
            .filter { it.length >= MIN_QUERY_LENGTH }
            .onEach { query ->
                performSearch(query)
            }
            .launchIn(viewModelScope)
    }

    fun onIntent(intent: SearchIntent) {
        when (intent) {
            is SearchIntent.UpdateQuery -> {
                _uiState.update { it.copy(query = intent.query, hasSearched = false) }
                queryFlow.value = intent.query

                if (intent.query.isEmpty()) {
                    _uiState.update { it.copy(results = emptyList(), hasSearched = false) }
                }
            }

            SearchIntent.ClearQuery -> {
                _uiState.update {
                    it.copy(query = "", results = emptyList(), hasSearched = false)
                }
                queryFlow.value = ""
            }

            is SearchIntent.PlayTrack -> {
                saveRecentSearch(_uiState.value.query)
                viewModelScope.launch {
                    playerManager.play(intent.track, listOf(intent.track))
                }
            }

            is SearchIntent.SelectRecentSearch -> {
                _uiState.update { it.copy(query = intent.query) }
                queryFlow.value = intent.query
                viewModelScope.launch {
                    performSearch(intent.query)
                }
            }

            SearchIntent.ClearRecentSearches -> {
                _uiState.update { it.copy(recentSearches = emptyList()) }
                savedStateHandle[KEY_RECENT_SEARCHES] = emptyList<String>()
            }
        }
    }

    private suspend fun performSearch(query: String) {
        _uiState.update { it.copy(isSearching = true) }

        try {
            trackRepository.searchTracks(query).collect { result ->
                _uiState.update {
                    it.copy(results = result, isSearching = false, hasSearched = true)
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(results = emptyList(), isSearching = false, hasSearched = true)
            }
        }
    }

    private fun saveRecentSearch(query: String) {
        if (query.length < MIN_QUERY_LENGTH) return

        val currentRecent = _uiState.value.recentSearches.toMutableList()
        currentRecent.remove(query)
        currentRecent.add(0, query)

        val trimmed = currentRecent.take(MAX_RECENT_SEARCHES)

        _uiState.update { it.copy(recentSearches = trimmed) }
        savedStateHandle[KEY_RECENT_SEARCHES] = trimmed
    }
}
