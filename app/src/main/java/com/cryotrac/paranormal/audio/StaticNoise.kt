package com.cryotrac.paranormal.audio

import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioTrack
import java.util.Random
import kotlin.math.PI
import kotlin.math.sin

/**
 * Looping radio-style static played under the GHOSTEC sweep.
 * The noise is generated once into a 2-second buffer and looped by AudioTrack,
 * so it costs no CPU while playing and needs no audio asset.
 */
class StaticNoise {

    private var track: AudioTrack? = null

    fun start(volume: Float = 0.3f) {
        if (track != null) return
        val samples = buildStatic()
        track = AudioTrack.Builder()
            .setAudioAttributes(
                AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                    .build()
            )
            .setAudioFormat(
                AudioFormat.Builder()
                    .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                    .setSampleRate(SAMPLE_RATE)
                    .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                    .build()
            )
            .setTransferMode(AudioTrack.MODE_STATIC)
            .setBufferSizeInBytes(samples.size * 2)
            .build()
            .apply {
                write(samples, 0, samples.size)
                setLoopPoints(0, samples.size, -1)
                setVolume(volume)
                play()
            }
    }

    fun stop() {
        track?.run { stop(); release() }
        track = null
    }

    // Band-limited white noise with a slow swell and occasional crackle.
    // The swell completes a whole number of cycles so the loop point is seamless.
    private fun buildStatic(): ShortArray {
        val n = SAMPLE_RATE * 2
        val out = ShortArray(n)
        val rnd = Random(7)
        var lp = 0f; var hp = 0f; var prev = 0f
        for (i in 0 until n) {
            val white = rnd.nextFloat() * 2f - 1f
            lp += 0.45f * (white - lp)                 // soften the harsh top end
            hp = 0.97f * (hp + lp - prev); prev = lp    // strip low rumble
            val t = i.toFloat() / n
            val swell = 0.7f + 0.3f * sin(2 * PI * 3 * t).toFloat()
            var s = hp * swell * 1.6f
            if (rnd.nextFloat() < 0.0004f) s += (rnd.nextFloat() * 2f - 1f) * 0.8f
            out[i] = (s.coerceIn(-1f, 1f) * Short.MAX_VALUE).toInt().toShort()
        }
        return out
    }

    private companion object {
        const val SAMPLE_RATE = 44_100
    }
}
