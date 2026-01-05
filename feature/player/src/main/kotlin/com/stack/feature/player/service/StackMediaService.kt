package com.stack.feature.player.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaSession
import androidx.media3.session.MediaSessionService
import com.stack.core.logging.Logger
import com.stack.core.player.StackPlayerManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Foreground service for media playback with MediaSession integration.
 *
 * SSOT Reference: Section 8.4 (MediaSessionService Requirements)
 * - Persistent notification with artwork, title, controls
 * - MediaSession for Bluetooth/Android Auto/Lock screen
 * - Survives app backgrounding
 */
@AndroidEntryPoint
class StackMediaService : MediaSessionService() {

    @Inject
    lateinit var playerManager: StackPlayerManager

    @Inject
    lateinit var logger: Logger

    private var mediaSession: MediaSession? = null
    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    companion object {
        private const val TAG = "StackMediaService"
        const val NOTIFICATION_CHANNEL_ID = "stack_playback_channel"
        const val NOTIFICATION_ID = 1001
    }

    override fun onCreate() {
        super.onCreate()
        logger.d(TAG, "Service onCreate")

        createNotificationChannel()
        initializeMediaSession()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                "Music Playback",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Shows currently playing track"
                setShowBadge(false)
            }

            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    @OptIn(UnstableApi::class)
    private fun initializeMediaSession() {
        serviceScope.launch {
            playerManager.initialize()

            val player = playerManager.getPlayer() ?: run {
                logger.e(TAG, "Failed to get player instance")
                return@launch
            }

            // Create pending intent for notification tap
            val launchIntent = packageManager.getLaunchIntentForPackage(packageName)?.apply {
                flags = Intent.FLAG_ACTIVITY_SINGLE_TOP
            }
            val pendingIntent = PendingIntent.getActivity(
                this@StackMediaService,
                0,
                launchIntent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )

            mediaSession = MediaSession.Builder(this@StackMediaService, player)
                .setSessionActivity(pendingIntent)
                .setCallback(MediaSessionCallback())
                .build()

            logger.d(TAG, "MediaSession initialized")
        }
    }

    override fun onGetSession(controllerInfo: MediaSession.ControllerInfo): MediaSession? {
        return mediaSession
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        logger.d(TAG, "Task removed")

        val player = mediaSession?.player
        if (player == null || !player.playWhenReady || player.mediaItemCount == 0) {
            // Stop service if not playing
            stopSelf()
        }
    }

    override fun onDestroy() {
        logger.d(TAG, "Service onDestroy")

        serviceScope.launch {
            playerManager.release()
        }

        mediaSession?.run {
            player.release()
            release()
            mediaSession = null
        }

        super.onDestroy()
    }

    /**
     * Custom MediaSession callback for handling commands.
     */
    private inner class MediaSessionCallback : MediaSession.Callback {

        override fun onConnect(
            session: MediaSession,
            controller: MediaSession.ControllerInfo
        ): MediaSession.ConnectionResult {
            // Allow all connections
            return MediaSession.ConnectionResult.AcceptedResultBuilder(session)
                .setAvailableSessionCommands(
                    MediaSession.ConnectionResult.DEFAULT_SESSION_COMMANDS
                )
                .build()
        }
    }
}
