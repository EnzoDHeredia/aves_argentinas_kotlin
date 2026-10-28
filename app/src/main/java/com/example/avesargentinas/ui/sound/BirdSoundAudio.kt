package com.example.avesargentinas.ui.sound

import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

internal data class CapturedAudio(val samples: ShortArray, val sampleRate: Int)

internal object BirdSoundAudio {
    val windowStarts = listOf(0, 96_000, 192_000, 224_000)

    private val filters48k by lazy { createFilters(48_000, 2) }
    private val filters44k by lazy { createFilters(44_100, 320) }

    private fun createFilters(rate: Int, phaseCount: Int): Array<DoubleArray> {
        val radius = 64
        val cutoff = 14_000.0 / rate
        return Array(phaseCount) { phase ->
            val fraction = phase.toDouble() / phaseCount
            DoubleArray(radius * 2) { tap ->
                val distance = tap - radius + 1 - fraction
                if (kotlin.math.abs(distance) >= radius) 0.0 else {
                    val sinc = if (distance == 0.0) 2 * cutoff else sin(2 * PI * cutoff * distance) / (PI * distance)
                    sinc * (0.5 + 0.5 * cos(PI * distance / radius))
                }
            }
        }
    }

    fun toModelSamples(audio: CapturedAudio): FloatArray {
        val rate = audio.sampleRate
        require(rate in listOf(32_000, 44_100, 48_000))
        val sampleCount = audio.samples.size
        require(sampleCount == rate * 3 || sampleCount == rate * 10) {
            "Longitud de audio no admitida: $sampleCount muestras para $rate Hz (se requieren 3 o 10 segundos)"
        }
        val targetSize = (sampleCount / rate) * 32_000
        if (rate == 32_000) return FloatArray(targetSize) { audio.samples[it] / 32768f }

        val phaseCount = if (rate == 48_000) 2 else 320
        val filters = if (rate == 48_000) filters48k else filters44k
        val radius = 64
        return FloatArray(targetSize) { index ->
            val position = index.toLong() * rate
            val center = (position / 32_000).toInt()
            val phase = ((position % 32_000) * phaseCount / 32_000).toInt()
            val filter = filters[phase]
            var sum = 0.0
            var weight = 0.0
            for (tap in filter.indices) {
                val source = center + tap - radius + 1
                if (source in audio.samples.indices) {
                    sum += audio.samples[source] * filter[tap]
                    weight += filter[tap]
                }
            }
            (sum / weight / 32768.0).toFloat()
        }
    }
}
