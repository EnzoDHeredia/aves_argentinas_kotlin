package com.example.avesargentinas.ui.sound

internal data class AudioWindow(
    val audio: CapturedAudio,
    val endPosition: Long
)

internal class AudioWindowBuffer(val sampleRate: Int) {
    init {
        require(sampleRate in SUPPORTED_RATES) {
            "Frecuencia de muestreo no admitida: $sampleRate"
        }
    }

    val windowSize: Int = sampleRate * 3
    val hopSize: Int = sampleRate

    private val ring = ShortArray(windowSize)
    var totalSamplesWritten: Long = 0L
        private set
    private var nextWindowEnd: Long = windowSize.toLong()

    fun write(samples: ShortArray, offset: Int = 0, length: Int = samples.size - offset): List<AudioWindow> {
        require(offset >= 0 && length >= 0 && offset + length <= samples.size) {
            "Rango inválido: offset=$offset, length=$length, size=${samples.size}"
        }
        if (length == 0) return emptyList()

        val windows = mutableListOf<AudioWindow>()
        var currentOffset = offset
        var remaining = length

        while (remaining > 0) {
            val neededToNext = nextWindowEnd - totalSamplesWritten
            val toWrite = if (remaining.toLong() < neededToNext) remaining else neededToNext.toInt()

            writeToRing(samples, currentOffset, toWrite)
            totalSamplesWritten += toWrite
            currentOffset += toWrite
            remaining -= toWrite

            if (totalSamplesWritten == nextWindowEnd) {
                windows.add(extractWindow(nextWindowEnd))
                nextWindowEnd += hopSize
            }
        }
        return windows
    }

    private fun writeToRing(src: ShortArray, srcOffset: Int, count: Int) {
        val writePos = (totalSamplesWritten % windowSize).toInt()
        val firstPart = (windowSize - writePos).coerceAtMost(count)
        System.arraycopy(src, srcOffset, ring, writePos, firstPart)
        val secondPart = count - firstPart
        if (secondPart > 0) {
            System.arraycopy(src, srcOffset + firstPart, ring, 0, secondPart)
        }
    }

    private fun extractWindow(endPosition: Long): AudioWindow {
        val out = ShortArray(windowSize)
        val startPos = (totalSamplesWritten % windowSize).toInt()
        val firstPart = windowSize - startPos
        System.arraycopy(ring, startPos, out, 0, firstPart)
        if (startPos > 0) {
            System.arraycopy(ring, 0, out, firstPart, startPos)
        }
        return AudioWindow(CapturedAudio(out, sampleRate), endPosition)
    }

    companion object {
        val SUPPORTED_RATES = listOf(32_000, 44_100, 48_000)
    }
}
