package com.stack.player

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stack.core.ui.theme.StackTheme
import com.stack.domain.usecase.gate.GetGateReadyStatusUseCase
import com.stack.feature.player.MiniPlayer
import com.stack.feature.player.PlayerViewModel
import com.stack.feature.player.service.StackMediaService
import com.stack.player.navigation.NavRoutes
import com.stack.player.navigation.StackNavHost
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var getGateReadyStatusUseCase: GetGateReadyStatusUseCase

    private val playerViewModel: PlayerViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Start media service for background playback
        startService(Intent(this, StackMediaService::class.java))

        setContent {
            val isGateReady by getGateReadyStatusUseCase()
                .collectAsStateWithLifecycle(initialValue = false)

            val playerState by playerViewModel.playerState.collectAsStateWithLifecycle()
            val navController = rememberNavController()

            // Handle player events (Toast messages)
            LaunchedEffect(Unit) {
                playerViewModel.events.collect { event ->
                    when (event) {
                        is com.stack.feature.player.PlayerEvent.ShowToast -> {
                            Toast.makeText(this@MainActivity, event.message, Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }

            StackTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Column(modifier = Modifier.fillMaxSize()) {
                        // Main navigation content
                        Box(modifier = Modifier.weight(1f)) {
                            StackNavHost(
                                isGateReady = isGateReady,
                                navController = navController
                            )
                        }

                        // Mini player at bottom
                        MiniPlayer(
                            playerState = playerState,
                            onPlayPauseClick = playerViewModel::onPlayPauseClick,
                            onNextClick = playerViewModel::onNextClick,
                            onMiniPlayerClick = {
                                navController.navigate(NavRoutes.NowPlaying.route)
                            }
                        )
                    }
                }
            }
        }
    }
}
