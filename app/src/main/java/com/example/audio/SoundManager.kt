package com.example.audio

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.math.sin

/**
 * Procedural low-latency Audio and Haptic feedback generator.
 * Generates harmonic pentatonic chimes, crisp clicks, and victory fanfare
 * without needing bulky external sound files.
 */
class SoundManager(context: Context) {
    private val appContext = context.applicationContext
    private val vibrator: Vibrator? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val vibratorManager = appContext.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
        vibratorManager?.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        appContext.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
    }

    var soundEnabled: Boolean = true
    var hapticEnabled: Boolean = true

    private val audioScope = CoroutineScope(Dispatchers.Default)

    // Pentatonic scale frequencies in Hz (C5, D5, E5, G5, A5, C6, D6, E6, G6, A6, C7)
    private val pentatonicScale = floatArrayOf(
        523.25f, 587.33f, 659.25f, 783.99f, 880.00f,
        1046.50f, 1174.66f, 1318.51f, 1567.98f, 1760.00f, 2093.00f
    )

    fun playArrowLaunch(comboIndex: Int) {
        if (hapticEnabled) {
            vibrateLaunch()
        }
        if (!soundEnabled) return

        audioScope.launch {
            val noteIndex = (comboIndex.coerceAtLeast(0)) % pentatonicScale.size
            val freq = pentatonicScale[noteIndex]
            playTone(freq = freq, durationMs = 140, attackMs = 10, decayMs = 120, harmonic = 0.25f)
        }
    }

    fun playBlocked() {
        if (hapticEnabled) {
            vibrateBlocked()
        }
        if (!soundEnabled) return

        audioScope.launch {
            playTone(freq = 160f, durationMs = 100, attackMs = 5, decayMs = 80, harmonic = 0.5f)
        }
    }

    fun playRotate() {
        if (hapticEnabled) {
            vibrateClick()
        }
        if (!soundEnabled) return

        audioScope.launch {
            playTone(freq = 720f, durationMs = 60, attackMs = 5, decayMs = 50, harmonic = 0.15f)
        }
    }

    fun playVictory() {
        if (hapticEnabled) {
            vibrateSuccess()
        }
        if (!soundEnabled) return

        audioScope.launch {
            val arpeggio = listOf(523.25f, 659.25f, 783.99f, 1046.50f, 1318.51f, 1567.98f)
            for (freq in arpeggio) {
                playTone(freq = freq, durationMs = 160, attackMs = 10, decayMs = 140, harmonic = 0.2f)
                kotlinx.coroutines.delay(65)
            }
        }
    }

    fun playUndo() {
        if (hapticEnabled) {
            vibrateClick()
        }
        if (!soundEnabled) return

        audioScope.launch {
            playTone(freq = 380f, durationMs = 70, attackMs = 5, decayMs = 60, harmonic = 0.2f)
        }
    }

    private fun playTone(
        freq: Float,
        durationMs: Int,
        attackMs: Int,
        decayMs: Int,
        harmonic: Float
    ) {
        try {
            val sampleRate = 22050
            val numSamples = (sampleRate * durationMs) / 1000
            val buffer = ShortArray(numSamples)

            val attackSamples = (sampleRate * attackMs) / 1000
            val decaySamples = (sampleRate * decayMs) / 1000

            for (i in 0 until numSamples) {
                val envelope: Float = when {
                    i < attackSamples -> i.toFloat() / attackSamples
                    i > numSamples - decaySamples -> (numSamples - i).toFloat() / decaySamples
                    else -> 1.0f
                }
                val t = i.toFloat() / sampleRate
                val primaryWave = sin(2.0 * Math.PI * freq * t)
                val harmonicWave = sin(2.0 * Math.PI * (freq * 2.0) * t) * harmonic
                val sampleVal = ((primaryWave + harmonicWave) / (1f + harmonic) * envelope * 24000).toInt()
                buffer[i] = sampleVal.coerceIn(-32767, 32767).toShort()
            }

            val audioTrack = AudioTrack.Builder()
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_GAME)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                .setAudioFormat(
                    AudioFormat.Builder()
                        .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                        .setSampleRate(sampleRate)
                        .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                        .build()
                )
                .setBufferSizeInBytes(buffer.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(buffer, 0, buffer.size)
            audioTrack.play()
            Thread.sleep(durationMs.toLong() + 20)
            audioTrack.release()
        } catch (_: Exception) {
            // Silently handle any audio track creation failures on low-end emulators
        }
    }

    private fun vibrateLaunch() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(20)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateBlocked() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_HEAVY_CLICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(50)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateClick() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(10)
            }
        } catch (_: Exception) {}
    }

    private fun vibrateSuccess() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(
                        longArrayOf(0, 40, 60, 40, 60, 80),
                        intArrayOf(0, 180, 0, 200, 0, 255),
                        -1
                    )
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(150)
            }
        } catch (_: Exception) {}
    }
}
