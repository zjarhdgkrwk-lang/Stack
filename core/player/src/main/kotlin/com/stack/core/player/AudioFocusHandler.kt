package com.stack.core.player

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import com.stack.core.logging.Logger
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Manages audio focus for playback.
 *
 * SSOT Reference: Section 6.4 (Audio Focus Policy)
 * - Pause on phone call
 * - Duck for notifications (reduce volume temporarily)
 */
@Singleton
class AudioFocusHandler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val logger: Logger
) {
    private val audioManager: AudioManager =
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager

    private var focusRequest: AudioFocusRequest? = null
    private var focusChangeListener: AudioFocusListener? = null
    private var isHoldingFocus = false

    interface AudioFocusListener {
        fun onFocusGained()
        fun onFocusLostTransient()      // Pause (will regain)
        fun onFocusLostTransientCanDuck() // Duck volume
        fun onFocusLostPermanent()       // Stop
    }

    fun setListener(listener: AudioFocusListener) {
        focusChangeListener = listener
    }

    /**
     * Request audio focus before starting playback.
     * @return true if focus was granted
     */
    fun requestFocus(): Boolean {
        if (isHoldingFocus) return true

        val focusChangeCallback = AudioManager.OnAudioFocusChangeListener { focusChange ->
            when (focusChange) {
                AudioManager.AUDIOFOCUS_GAIN -> {
                    logger.d(TAG, "Audio focus gained")
                    focusChangeListener?.onFocusGained()
                }
                AudioManager.AUDIOFOCUS_LOSS -> {
                    logger.d(TAG, "Audio focus lost permanently")
                    isHoldingFocus = false
                    focusChangeListener?.onFocusLostPermanent()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> {
                    logger.d(TAG, "Audio focus lost transiently")
                    focusChangeListener?.onFocusLostTransient()
                }
                AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> {
                    logger.d(TAG, "Audio focus lost - can duck")
                    focusChangeListener?.onFocusLostTransientCanDuck()
                }
            }
        }

        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_MEDIA)
            .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
            .build()

        focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN)
            .setAudioAttributes(audioAttributes)
            .setWillPauseWhenDucked(false) // We handle ducking ourselves
            .setOnAudioFocusChangeListener(focusChangeCallback)
            .build()

        val result = audioManager.requestAudioFocus(focusRequest!!)
        isHoldingFocus = (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED)

        logger.d(TAG, "Audio focus request result: $isHoldingFocus")
        return isHoldingFocus
    }

    /**
     * Abandon audio focus when stopping playback.
     */
    fun abandonFocus() {
        focusRequest?.let { request ->
            audioManager.abandonAudioFocusRequest(request)
            isHoldingFocus = false
            logger.d(TAG, "Audio focus abandoned")
        }
        focusRequest = null
    }

    companion object {
        private const val TAG = "AudioFocusHandler"
    }
}
