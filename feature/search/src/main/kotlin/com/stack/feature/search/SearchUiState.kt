package com.stack.feature.search

import com.stack.domain.model.Track

data class SearchUiState(
    val query: String = "",
    val results: List<Track> = emptyList(),
    val isSearching: Boolean = false,
    val hasSearched: Boolean = false,
    val recentSearches: List<String> = emptyList()
)
