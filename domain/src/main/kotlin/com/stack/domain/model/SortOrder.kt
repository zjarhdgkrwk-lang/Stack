package com.stack.domain.model

/**
 * Sort options for track lists.
 */
enum class TrackSortOrder {
    TITLE_ASC,
    TITLE_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    ALBUM_ASC,
    ALBUM_DESC,
    DATE_ADDED_ASC,
    DATE_ADDED_DESC,      // Default (SSOT 9.4)
    DURATION_ASC,
    DURATION_DESC,
    YEAR_ASC,
    YEAR_DESC
}

/**
 * Sort options for album lists.
 */
enum class AlbumSortOrder {
    NAME_ASC,
    NAME_DESC,
    ARTIST_ASC,
    ARTIST_DESC,
    YEAR_ASC,
    YEAR_DESC,
    TRACK_COUNT_ASC,
    TRACK_COUNT_DESC
}

/**
 * Sort options for playlist lists.
 */
enum class PlaylistSortOrder {
    NAME_ASC,
    NAME_DESC,
    CREATED_ASC,
    CREATED_DESC,
    UPDATED_ASC,
    UPDATED_DESC,
    TRACK_COUNT_ASC,
    TRACK_COUNT_DESC
}
