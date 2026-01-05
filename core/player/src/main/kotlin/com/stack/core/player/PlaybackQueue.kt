package com.stack.core.player

import com.stack.domain.model.Track
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.random.Random

/**
 * Manages the playback queue with shuffle support.
 *
 * SSOT Reference: Section 8.4 (Shuffle/Repeat)
 * - Shuffle: Fisher-Yates algorithm, maintains original order
 * - Previous track: ≤3s returns to previous, >3s restarts current
 */
@Singleton
class PlaybackQueue @Inject constructor() {

    private val _originalQueue = MutableStateFlow<List<Track>>(emptyList())
    private val _shuffledQueue = MutableStateFlow<List<Track>>(emptyList())
    private val _currentIndex = MutableStateFlow(-1)
    private val _isShuffled = MutableStateFlow(false)

    val currentQueue: StateFlow<List<Track>>
        get() = if (_isShuffled.value) _shuffledQueue.asStateFlow() else _originalQueue.asStateFlow()

    val currentIndex: StateFlow<Int> = _currentIndex.asStateFlow()
    val isShuffled: StateFlow<Boolean> = _isShuffled.asStateFlow()

    val currentTrack: Track?
        get() {
            val queue = if (_isShuffled.value) _shuffledQueue.value else _originalQueue.value
            val index = _currentIndex.value
            return queue.getOrNull(index)
        }

    val hasNext: Boolean
        get() {
            val queue = if (_isShuffled.value) _shuffledQueue.value else _originalQueue.value
            return _currentIndex.value < queue.size - 1
        }

    val hasPrevious: Boolean
        get() = _currentIndex.value > 0

    /**
     * Set a new queue and start from a specific track.
     * Used when user clicks a track in a list context.
     */
    fun setQueue(tracks: List<Track>, startTrack: Track) {
        val startIndex = tracks.indexOfFirst { it.id == startTrack.id }.coerceAtLeast(0)
        _originalQueue.value = tracks
        _currentIndex.value = startIndex

        if (_isShuffled.value) {
            reshuffleQueue(keepCurrent = true)
        }
    }

    /**
     * Enable/disable shuffle mode.
     * When enabling, current track stays at index 0, rest is shuffled.
     */
    fun setShuffle(enabled: Boolean) {
        if (enabled == _isShuffled.value) return

        _isShuffled.value = enabled
        if (enabled) {
            reshuffleQueue(keepCurrent = true)
        } else {
            // Restore original order, find current track's original index
            val current = currentTrack
            if (current != null) {
                _currentIndex.value = _originalQueue.value.indexOfFirst { it.id == current.id }
                    .coerceAtLeast(0)
            }
        }
    }

    /**
     * Fisher-Yates shuffle algorithm.
     * Keeps current track at index 0 if keepCurrent is true.
     */
    private fun reshuffleQueue(keepCurrent: Boolean) {
        val original = _originalQueue.value.toMutableList()
        if (original.isEmpty()) return

        val current = currentTrack

        // Fisher-Yates shuffle
        for (i in original.size - 1 downTo 1) {
            val j = Random.nextInt(i + 1)
            val temp = original[i]
            original[i] = original[j]
            original[j] = temp
        }

        // Move current track to front if needed
        if (keepCurrent && current != null) {
            val currentInShuffled = original.indexOfFirst { it.id == current.id }
            if (currentInShuffled > 0) {
                original.removeAt(currentInShuffled)
                original.add(0, current)
            }
            _currentIndex.value = 0
        }

        _shuffledQueue.value = original
    }

    /**
     * Move to next track.
     * @return true if moved successfully, false if at end
     */
    fun moveToNext(): Boolean {
        val queue = if (_isShuffled.value) _shuffledQueue.value else _originalQueue.value
        return if (_currentIndex.value < queue.size - 1) {
            _currentIndex.update { it + 1 }
            true
        } else {
            false
        }
    }

    /**
     * Move to previous track.
     * @return true if moved successfully, false if at start
     */
    fun moveToPrevious(): Boolean {
        return if (_currentIndex.value > 0) {
            _currentIndex.update { it - 1 }
            true
        } else {
            false
        }
    }

    /**
     * Skip to a specific index in the queue.
     */
    fun skipToIndex(index: Int): Boolean {
        val queue = if (_isShuffled.value) _shuffledQueue.value else _originalQueue.value
        return if (index in queue.indices) {
            _currentIndex.value = index
            true
        } else {
            false
        }
    }

    /**
     * Add track to play next (after current).
     */
    fun playNext(track: Track) {
        val insertIndex = (_currentIndex.value + 1).coerceAtLeast(0)

        _originalQueue.update { list ->
            list.toMutableList().apply { add(insertIndex, track) }
        }

        if (_isShuffled.value) {
            _shuffledQueue.update { list ->
                list.toMutableList().apply { add(insertIndex, track) }
            }
        }
    }

    /**
     * Add track to end of queue.
     */
    fun addToQueue(track: Track) {
        _originalQueue.update { it + track }
        if (_isShuffled.value) {
            _shuffledQueue.update { it + track }
        }
    }

    /**
     * Remove track at index.
     */
    fun removeAt(index: Int) {
        _originalQueue.update { list ->
            list.toMutableList().apply {
                if (index in indices) removeAt(index)
            }
        }

        if (_isShuffled.value) {
            _shuffledQueue.update { list ->
                list.toMutableList().apply {
                    if (index in indices) removeAt(index)
                }
            }
        }

        // Adjust current index if needed
        if (index < _currentIndex.value) {
            _currentIndex.update { it - 1 }
        } else if (index == _currentIndex.value) {
            // Current track removed, stay at same index (now pointing to next)
            val queue = if (_isShuffled.value) _shuffledQueue.value else _originalQueue.value
            _currentIndex.value = _currentIndex.value.coerceIn(0, queue.size - 1)
        }
    }

    /**
     * Clear the entire queue.
     */
    fun clear() {
        _originalQueue.value = emptyList()
        _shuffledQueue.value = emptyList()
        _currentIndex.value = -1
    }

    /**
     * Get the next track for preloading (Dual Player support).
     */
    fun peekNext(): Track? {
        val queue = if (_isShuffled.value) _shuffledQueue.value else _originalQueue.value
        val nextIndex = _currentIndex.value + 1
        return queue.getOrNull(nextIndex)
    }
}
