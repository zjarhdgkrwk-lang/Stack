package com.stack.feature.nowplaying

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.QueueMusic
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.feature.nowplaying.components.NowPlayingArtwork
import com.stack.feature.nowplaying.components.NowPlayingControls
import com.stack.feature.nowplaying.components.NowPlayingSeekBar
import com.stack.feature.nowplaying.components.NowPlayingTrackInfo
import com.stack.feature.nowplaying.components.QueueBottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NowPlayingScreen(
    onNavigateBack: () -> Unit,
    viewModel: NowPlayingViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    // Use derivedStateOf to minimize recompositions for progress calculation
    val progress by remember(uiState.duration) {
        derivedStateOf {
            if (uiState.duration > 0) {
                val currentPosition = if (uiState.isSeeking) uiState.seekPosition else uiState.position
                currentPosition.toFloat() / uiState.duration.toFloat()
            } else {
                0f
            }
        }
    }

    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        label = "seek_progress"
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.Default.ExpandMore,
                            contentDescription = "Collapse"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.onIntent(NowPlayingIntent.ShowQueueSheet) }
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.QueueMusic,
                            contentDescription = "Queue"
                        )
                    }
                    IconButton(onClick = { /* TODO: More menu */ }) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "More options"
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .background(MaterialTheme.colorScheme.background)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                // Artwork
                NowPlayingArtwork(
                    artworkUri = uiState.currentTrack?.albumArtUri,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f, fill = false)
                )

                Spacer(modifier = Modifier.height(32.dp))

                // Track Info
                NowPlayingTrackInfo(
                    title = uiState.currentTrack?.title ?: "No track",
                    artist = uiState.currentTrack?.artist ?: "Unknown artist",
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Seek Bar
                NowPlayingSeekBar(
                    progress = if (uiState.isSeeking) progress else animatedProgress,
                    position = if (uiState.isSeeking) uiState.seekPosition else uiState.position,
                    duration = uiState.duration,
                    onSeekStart = { position ->
                        viewModel.onIntent(NowPlayingIntent.StartSeek(position))
                    },
                    onSeekChange = { position ->
                        viewModel.onIntent(NowPlayingIntent.UpdateSeekPosition(position))
                    },
                    onSeekFinish = { position ->
                        viewModel.onIntent(NowPlayingIntent.FinishSeek(position))
                    },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Controls
                NowPlayingControls(
                    isPlaying = uiState.isPlaying,
                    shuffleEnabled = uiState.shuffleEnabled,
                    repeatMode = uiState.repeatMode,
                    onPlayPauseClick = { viewModel.onIntent(NowPlayingIntent.TogglePlayPause) },
                    onPreviousClick = { viewModel.onIntent(NowPlayingIntent.PlayPrevious) },
                    onNextClick = { viewModel.onIntent(NowPlayingIntent.PlayNext) },
                    onShuffleClick = { viewModel.onIntent(NowPlayingIntent.ToggleShuffle) },
                    onRepeatClick = { viewModel.onIntent(NowPlayingIntent.CycleRepeatMode) },
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(48.dp))
            }
        }
    }

    // Queue Bottom Sheet
    if (uiState.isQueueSheetVisible) {
        ModalBottomSheet(
            onDismissRequest = { viewModel.onIntent(NowPlayingIntent.HideQueueSheet) },
            sheetState = sheetState
        ) {
            QueueBottomSheet(
                queue = uiState.queue,
                currentIndex = uiState.currentQueueIndex,
                onItemClick = { index ->
                    viewModel.onIntent(NowPlayingIntent.PlayFromQueue(index))
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 32.dp)
            )
        }
    }
}
