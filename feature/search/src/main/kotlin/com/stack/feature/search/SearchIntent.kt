package com.stack.feature.search

import com.stack.domain.model.Track

sealed interface SearchIntent {
    data class UpdateQuery(val query: String) : SearchIntent
    data object ClearQuery : SearchIntent
    data class PlayTrack(val track: Track) : SearchIntent
    data class SelectRecentSearch(val query: String) : SearchIntent
    data object ClearRecentSearches : SearchIntent
}
