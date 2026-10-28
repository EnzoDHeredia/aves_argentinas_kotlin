package com.example.avesargentinas.ui.sound

import org.junit.Assert.*
import org.junit.Test

class AudioWindowBufferTest {

    @Test
    fun rejectsUnsupportedSampleRates() {
        for (rate in listOf(8_000, 16_000, 22_050, 24_000, 96_000)) {
            assertThrows(IllegalArgumentException::class.java) {
                AudioWindowBuffer(rate)
            }
        }
    }

    @Test
    fun acceptsSupportedSampleRates() {
        for (rate in listOf(32_000, 44_100, 48_000)) {
            val buffer = AudioWindowBuffer(rate)
            assertEquals(rate, buffer.sampleRate)
            assertEquals(rate * 3, buffer.windowSize)
            assertEquals(rate, buffer.hopSize)
            assertEquals(0L, buffer.totalSamplesWritten)
        }
    }

    @Test
    fun rejectsInvalidWriteRanges() {
        val buffer = AudioWindowBuffer(32_000)
        val data = ShortArray(100)
        assertThrows(IllegalArgumentException::class.java) {
            buffer.write(data, offset = -1, length = 10)
        }
        assertThrows(IllegalArgumentException::class.java) {
            buffer.write(data, offset = 0, length = -1)
        }
        assertThrows(IllegalArgumentException::class.java) {
            buffer.write(data, offset = 90, length = 20)
        }
    }

    @Test
    fun emptyWriteReturnsEmptyListAndDoesNotAdvancePosition() {
        val buffer = AudioWindowBuffer(32_000)
        val windows = buffer.write(ShortArray(0))
        assertTrue(windows.isEmpty())
        assertEquals(0L, buffer.totalSamplesWritten)
    }

    @Test
    fun firstWindowDeliveredAfterExactlyThreeSecondsAndNextEverySecond() {
        for (rate in listOf(32_000, 44_100, 48_000)) {
            val buffer = AudioWindowBuffer(rate)
            val windowSamples = rate * 3
            val hopSamples = rate

            // Feed samples just 1 short of 3 seconds
            val beforeFirst = buffer.write(ShortArray(windowSamples - 1))
            assertTrue(beforeFirst.isEmpty())
            assertEquals((windowSamples - 1).toLong(), buffer.totalSamplesWritten)

            // Feed the 1 missing sample -> exactly 3 seconds
            val first = buffer.write(ShortArray(1) { 42 })
            assertEquals(1, first.size)
            assertEquals(windowSamples.toLong(), buffer.totalSamplesWritten)
            assertEquals(windowSamples.toLong(), first[0].endPosition)
            assertEquals(windowSamples, first[0].audio.samples.size)
            assertEquals(rate, first[0].audio.sampleRate)
            assertEquals(42.toShort(), first[0].audio.samples.last())

            // Feed samples just 1 short of the next hop
            val beforeSecond = buffer.write(ShortArray(hopSamples - 1))
            assertTrue(beforeSecond.isEmpty())
            assertEquals((windowSamples + hopSamples - 1).toLong(), buffer.totalSamplesWritten)

            // Feed the 1 missing sample -> exactly 4 seconds
            val second = buffer.write(ShortArray(1) { 99 })
            assertEquals(1, second.size)
            val expectedSecondEnd = (windowSamples + hopSamples).toLong()
            assertEquals(expectedSecondEnd, buffer.totalSamplesWritten)
            assertEquals(expectedSecondEnd, second[0].endPosition)
            assertEquals(windowSamples, second[0].audio.samples.size)
            assertEquals(99.toShort(), second[0].audio.samples.last())
        }
    }

    @Test
    fun handlesUnequalAndSmallChunkSizesCorrectly() {
        val rate = 32_000
        val buffer = AudioWindowBuffer(rate)
        // Feed audio with values 1, 2, 3... in uneven chunk sizes (primes)
        val chunkSizes = intArrayOf(257, 509, 1021, 733, 113, 89)
        var sampleValue: Short = 1
        var totalFed = 0
        val allWindows = mutableListOf<AudioWindow>()

        var chunkIdx = 0
        while (totalFed < rate * 5) {
            val size = chunkSizes[chunkIdx % chunkSizes.size]
            val chunk = ShortArray(size) { (sampleValue++).toShort() }
            allWindows.addAll(buffer.write(chunk))
            totalFed += size
            chunkIdx++
        }

        // With 5 seconds of audio, we expect 3 windows: at 3s (96,000), 4s (128,000), 5s (160,000)
        assertEquals(3, allWindows.size)
        assertEquals(96_000L, allWindows[0].endPosition)
        assertEquals(128_000L, allWindows[1].endPosition)
        assertEquals(160_000L, allWindows[2].endPosition)

        // Verify that the window slices match exact chronological samples
        for (w in allWindows) {
            val end = w.endPosition.toInt()
            val start = end - rate * 3
            assertEquals(rate * 3, w.audio.samples.size)
            assertEquals((start + 1).toShort(), w.audio.samples.first())
            assertEquals(end.toShort(), w.audio.samples.last())
        }
    }

    @Test
    fun singleLargeReadCrossingMultipleBoundariesEmitsMultipleWindows() {
        val rate = 32_000
        val buffer = AudioWindowBuffer(rate)
        // Feed 5.5 seconds in a single call
        val inputSize = (rate * 5.5).toInt()
        val input = ShortArray(inputSize) { (it + 1).toShort() }

        val windows = buffer.write(input)
        assertEquals(3, windows.size)
        assertEquals(96_000L, windows[0].endPosition)
        assertEquals(128_000L, windows[1].endPosition)
        assertEquals(160_000L, windows[2].endPosition)
        assertEquals(inputSize.toLong(), buffer.totalSamplesWritten)

        // Check exact contents of all 3 windows
        for (w in windows) {
            val end = w.endPosition.toInt()
            val start = end - rate * 3
            assertEquals((start + 1).toShort(), w.audio.samples.first())
            assertEquals(end.toShort(), w.audio.samples.last())
        }
    }

    @Test
    fun windowCopiesAreIndependentAndImmutableUnderSubsequentWrites() {
        val rate = 32_000
        val buffer = AudioWindowBuffer(rate)
        val initialInput = ShortArray(rate * 3) { 100 }
        val windows1 = buffer.write(initialInput)
        assertEquals(1, windows1.size)
        val firstWindowSamples = windows1[0].audio.samples

        // Write subsequent audio with entirely different values (overwriting ring buffer)
        val subsequentInput = ShortArray(rate * 3) { 999 }
        val windows2 = buffer.write(subsequentInput)
        assertEquals(3, windows2.size)

        // Verify that the first window was NOT mutated by the subsequent writes
        assertTrue(firstWindowSamples.all { it == 100.toShort() })
    }

    @Test
    fun simulatesOneHourOfContinuousAudioWithConstantMemory() {
        val rate = 48_000
        val buffer = AudioWindowBuffer(rate)
        val seconds = 3600
        val chunkSize = 1920 // 40 ms at 48 kHz, typical AudioRecord buffer read
        val chunk = ShortArray(chunkSize) { (it % 1000).toShort() }
        val totalSamplesToFeed = seconds.toLong() * rate

        var samplesFed = 0L
        var totalWindowsReceived = 0

        while (samplesFed < totalSamplesToFeed) {
            val toFeed = minOf(chunkSize.toLong(), totalSamplesToFeed - samplesFed).toInt()
            val windows = buffer.write(chunk, 0, toFeed)
            totalWindowsReceived += windows.size
            samplesFed += toFeed
        }

        // In 3600 seconds, first window is at 3s, then 1 per second up to 3600s:
        // Total windows = 3600 - 3 + 1 = 3598
        assertEquals(totalSamplesToFeed, buffer.totalSamplesWritten)
        assertEquals(3598, totalWindowsReceived)
    }
}
