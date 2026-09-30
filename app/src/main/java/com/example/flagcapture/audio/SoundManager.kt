package com.example.flagcapture.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.math.PI
import kotlin.math.sin

class SoundManager {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    private val sampleRate = 22050

    fun playMove() {
        scope.launch {
            // Crisp tactical wooden/stone piece placement click (~40ms)
            val durationMs = 45
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = (1.0 - (i.toDouble() / numSamples))
                // Blend of 520Hz and 280Hz with fast decay
                val s = (sin(2 * PI * 520 * t) * 0.6 + sin(2 * PI * 280 * t) * 0.4) * env
                samples[i] = (s * Short.MAX_VALUE * 0.5).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playCapture() {
        scope.launch {
            // Tactical metallic impact with punch (~130ms)
            val durationMs = 130
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = (1.0 - (i.toDouble() / numSamples)) * (1.0 - (i.toDouble() / numSamples))
                val freq = 440.0 - (t * 1800.0).coerceAtLeast(0.0)
                val s = (sin(2 * PI * freq * t) * 0.7 + sin(2 * PI * (freq * 1.5) * t) * 0.3) * env
                samples[i] = (s * Short.MAX_VALUE * 0.65).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playFlagDiscovered() {
        scope.launch {
            // Radiant alert tone (~350ms)
            val durationMs = 350
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate
                val env = (1.0 - (i.toDouble() / numSamples))
                val s = (sin(2 * PI * 880 * t) * 0.5 + sin(2 * PI * 1320 * t) * 0.4 + sin(2 * PI * 1760 * t) * 0.2) * env
                samples[i] = (s * Short.MAX_VALUE * 0.7).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    fun playVictory() {
        scope.launch {
            // Victorious 4-note ascending fanfare (C5 -> E5 -> G5 -> C6) (~700ms)
            val durationMs = 700
            val numSamples = (sampleRate * durationMs) / 1000
            val samples = ShortArray(numSamples)
            val segmentSamples = numSamples / 4
            val freqs = doubleArrayOf(523.25, 659.25, 783.99, 1046.50)

            for (i in 0 until numSamples) {
                val seg = (i / segmentSamples).coerceAtMost(3)
                val freq = freqs[seg]
                val t = i.toDouble() / sampleRate
                val segIndex = i % segmentSamples
                val env = 1.0 - (segIndex.toDouble() / segmentSamples * 0.5)
                val s = (sin(2 * PI * freq * t) * 0.75 + sin(2 * PI * (freq * 2) * t) * 0.25) * env
                samples[i] = (s * Short.MAX_VALUE * 0.7).toInt().coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
            }
            playPcm(samples)
        }
    }

    private fun playPcm(samples: ShortArray) {
        try {
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
                .setBufferSizeInBytes(samples.size * 2)
                .setTransferMode(AudioTrack.MODE_STATIC)
                .build()

            audioTrack.write(samples, 0, samples.size)
            audioTrack.play()

            // Release track after play duration in background
            scope.launch {
                val waitTimeMs = (samples.size * 1000L / sampleRate) + 100L
                kotlinx.coroutines.delay(waitTimeMs)
                try {
                    audioTrack.stop()
                    audioTrack.release()
                } catch (_: Exception) {}
            }
        } catch (_: Exception) {}
    }
}
