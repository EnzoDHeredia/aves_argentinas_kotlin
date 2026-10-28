package com.example.avesargentinas.ui.sound

import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.log10
import kotlin.math.sin
import kotlin.math.sqrt
import org.junit.Assert.*
import org.junit.Test

class BirdSoundAudioTest {
    @Test
    fun pcmExtremesAndInputArePreserved() {
        val input = ShortArray(320_000)
        input[0] = Short.MIN_VALUE
        input[1] = Short.MAX_VALUE
        val original = input.copyOf()
        val result = BirdSoundAudio.toModelSamples(CapturedAudio(input, 32_000))
        assertEquals(320_000, result.size)
        assertEquals(-1f, result[0], 0f)
        assertEquals(32767f / 32768f, result[1], 0f)
        assertArrayEquals(original, input)
    }

    @Test
    fun rejectsUnsupportedRatesAndIncompleteOrExcessCaptures() {
        for ((rate, count) in listOf(
            16_000 to 160_000,
            32_000 to 319_999,
            32_000 to 95_999,
            32_000 to 96_001,
            32_000 to 32_000 * 2,
            44_100 to 441_001,
            44_100 to 44_100 * 4,
            48_000 to 0,
            48_000 to 48_000 * 5
        )) {
            assertThrows(IllegalArgumentException::class.java) {
                BirdSoundAudio.toModelSamples(CapturedAudio(ShortArray(count), rate))
            }
        }
    }

    @Test
    fun threeSecondCapturesProduceExactModelSamplesAndPreserveExtremes() {
        for (rate in listOf(32_000, 44_100, 48_000)) {
            val input = ShortArray(rate * 3)
            input[0] = Short.MIN_VALUE
            input[1] = Short.MAX_VALUE
            val original = input.copyOf()
            val result = BirdSoundAudio.toModelSamples(CapturedAudio(input, rate))
            assertEquals(96_000, result.size)
            assertArrayEquals(original, input)
            if (rate == 32_000) {
                assertEquals(-1f, result[0], 0f)
                assertEquals(32767f / 32768f, result[1], 0f)
            }
        }
    }

    @Test
    fun threeSecondSilenceAndConstantSignalsStayConstantAtAllRates() {
        for (rate in listOf(32_000, 44_100, 48_000)) {
            for (value in listOf(0, 12345)) {
                val output = BirdSoundAudio.toModelSamples(CapturedAudio(ShortArray(rate * 3) { value.toShort() }, rate))
                assertEquals(96_000, output.size)
                assertTrue(output.all { abs(it - value / 32768f) < 0.00001f })
            }
        }
    }

    @Test
    fun threeSecondLastInputSampleIncludedAtAllRates() {
        for (rate in listOf(44_100, 48_000)) {
            val tail = ShortArray(rate * 3)
            tail[rate] = 10_000
            tail[tail.lastIndex] = Short.MAX_VALUE
            val original = tail.copyOf()
            val converted = BirdSoundAudio.toModelSamples(CapturedAudio(tail, rate))
            assertEquals(96_000, converted.size)
            assertArrayEquals(original, tail)
            assertTrue("last input sample lost at $rate Hz in 3s window", converted.takeLast(64).any { abs(it) > 0.00001f })
        }
    }

    @Test
    fun threeSecondResamplingPreservesPassbandAndRejectsAliasing() {
        for (rate in listOf(44_100, 48_000)) {
            for (frequency in listOf(1_000, 10_000, 18_000)) {
                val input = ShortArray(rate * 3) { i -> (16_000 * sin(2 * PI * frequency * i / rate)).toInt().toShort() }
                val output = BirdSoundAudio.toModelSamples(CapturedAudio(input, rate))
                assertEquals(96_000, output.size)
                val rms = sqrt(output.asSequence().drop(128).take(output.size - 256).map { it.toDouble() * it }.average())
                val db = 20 * log10(rms / (16_000.0 / 32768 / sqrt(2.0)))
                println("3s: $rate Hz -> 32000 Hz, $frequency Hz: $db dB")
                if (frequency == 18_000) assertTrue("3s $rate Hz, $frequency Hz: $db dB", db <= -40)
                else assertTrue("3s $rate Hz, $frequency Hz: $db dB", abs(db) <= 1)
            }
        }
    }

    @Test
    fun silenceAndConstantSignalsStayConstantAtAllRates() {
        for (rate in listOf(32_000, 44_100, 48_000)) {
            for (value in listOf(0, 12345)) {
                val output = BirdSoundAudio.toModelSamples(CapturedAudio(ShortArray(rate * 10) { value.toShort() }, rate))
                assertEquals(320_000, output.size)
                assertTrue(output.all { abs(it - value / 32768f) < 0.00001f })
            }
        }
    }

    @Test
    fun windowsCoverInOrderAndIncludeTheLastSample() {
        val input = ShortArray(320_000)
        input[0] = 1000
        input[96_000] = 2000
        input[192_000] = 3000
        input[224_000] = 4000
        input[319_999] = 5000
        val samples = BirdSoundAudio.toModelSamples(CapturedAudio(input, 32_000))
        val windows = BirdSoundAudio.windowStarts.map { samples.copyOfRange(it, it + 96_000) }
        assertEquals(listOf(0, 96_000, 192_000, 224_000), BirdSoundAudio.windowStarts)
        assertEquals(4, windows.size)
        assertEquals(listOf(1000, 2000, 3000, 4000).map { it / 32768f }, windows.map { it.first() })
        assertEquals(5000 / 32768f, windows.last().last(), 0f)
        for (rate in listOf(44_100, 48_000)) {
            val tail = ShortArray(rate * 10)
            tail[rate] = 10_000
            tail[rate * 5] = 20_000
            tail[tail.lastIndex] = Short.MAX_VALUE
            val original = tail.copyOf()
            val converted = BirdSoundAudio.toModelSamples(CapturedAudio(tail, rate))
            assertArrayEquals(original, tail)
            assertTrue(converted[32_000] > 0.1f)
            assertTrue(converted[160_000] > converted[32_000])
            assertTrue("last input sample lost at $rate Hz", converted.takeLast(64).any { abs(it) > 0.00001f })
        }
    }

    @Test
    fun resamplingPreservesPassbandAndRejectsAliasing() {
        for (rate in listOf(44_100, 48_000)) {
            for (frequency in listOf(1_000, 10_000, 18_000)) {
                val input = ShortArray(rate * 10) { i -> (16_000 * sin(2 * PI * frequency * i / rate)).toInt().toShort() }
                val output = BirdSoundAudio.toModelSamples(CapturedAudio(input, rate))
                val rms = sqrt(output.asSequence().drop(128).take(output.size - 256).map { it.toDouble() * it }.average())
                val db = 20 * log10(rms / (16_000.0 / 32768 / sqrt(2.0)))
                println("$rate Hz -> 32000 Hz, $frequency Hz: $db dB")
                if (frequency == 18_000) assertTrue("$rate Hz, $frequency Hz: $db dB", db <= -40)
                else assertTrue("$rate Hz, $frequency Hz: $db dB", abs(db) <= 1)
            }
        }
    }
}
