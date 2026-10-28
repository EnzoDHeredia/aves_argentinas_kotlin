package com.example.avesargentinas.ui.sound

import ai.onnxruntime.OnnxJavaType
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.TensorInfo
import android.content.res.AssetManager
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import java.security.MessageDigest

internal const val BIRDNET_MODEL_FILE = "BirdNET+_V3.0-preview3.1_Global_10K-pruned_FP16_CPU.onnx"

internal fun verifyBirdNetModel(model: ByteBuffer) {
    require(model.capacity() == 65_830_813) { "Modelo BirdNET incompatible" }
    val bytes = model.duplicate().apply { clear() }
    val sha256 = MessageDigest.getInstance("SHA-256")
    sha256.update(bytes)
    val digest = sha256.digest().joinToString("") { "%02x".format(it) }
    require(digest == "cc696820bc4882f2469a2cd0071b299aa400858aad7843e1f65879867bf5eaa5") { "Modelo BirdNET incompatible" }
}

internal open class BirdNetClassifier(private val assets: AssetManager) {
    private val mutex = Mutex()
    private val stateLock = Any()
    private val environment = OrtEnvironment.getEnvironment()
    private var session: OrtSession? = null
    private var model: ByteBuffer? = null
    private var running: OrtSession.RunOptions? = null
    private var generation = 0L
    private var closed = false

    open suspend fun initialize() = withContext(Dispatchers.IO) {
        val token = synchronized(stateLock) { check(!closed); generation }
        mutex.withLock {
            checkCurrent(token)
            if (session != null) return@withLock
            val buffer = assets.openFd("birdnet/$BIRDNET_MODEL_FILE").use { asset ->
                asset.createInputStream().use { it.channel.map(FileChannel.MapMode.READ_ONLY, asset.startOffset, asset.length) }
            }
            verifyBirdNetModel(buffer)
            val created = OrtSession.SessionOptions().use { options ->
                options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
                options.setIntraOpNumThreads(2)
                environment.createSession(buffer, options)
            }
            try {
                val input = created.inputInfo["input"]?.info as? TensorInfo
                val output = created.outputInfo["predictions"]?.info as? TensorInfo
                check(created.inputNames == setOf("input") && input != null && output != null)
                check(input.type == OnnxJavaType.FLOAT && input.shape.size == 2 &&
                    (input.shape[0] == 1L || input.shape[0] < 0) && (input.shape[1] == 96_000L || input.shape[1] < 0))
                check(output.type == OnnxJavaType.FLOAT && output.shape.size == 2 &&
                    (output.shape[0] == 1L || output.shape[0] < 0) && output.shape[1] == 9789L)
                currentCoroutineContext().ensureActive()
                synchronized(stateLock) {
                    if (closed || generation != token) throw CancellationException("Initialization cancelled")
                    model = buffer
                    session = created
                }
            } catch (e: Throwable) {
                created.close()
                throw e
            }
        }
    }

    open suspend fun classifyWindow(samples: FloatArray): FloatArray = withContext(Dispatchers.IO) {
        require(samples.size == 96_000 && samples.all { it.isFinite() })
        val token = synchronized(stateLock) { check(!closed); generation }
        mutex.withLock {
            checkCurrent(token)
            val active = checkNotNull(session) { "Classifier not initialized" }
            val first = samples[0]
            val scores = if ((1 until 96_000).all { samples[it] == first }) {
                FloatArray(9789)
            } else {
                runWindowLocked(active, samples, 0, token)
            }
            currentCoroutineContext().ensureActive()
            checkCurrent(token)
            scores
        }
    }

    open suspend fun classify(samples: FloatArray): FloatArray = withContext(Dispatchers.IO) {
        require(samples.size == 320_000 && samples.all { it.isFinite() })
        val token = synchronized(stateLock) { check(!closed); generation }
        mutex.withLock {
            checkCurrent(token)
            val active = checkNotNull(session) { "Classifier not initialized" }
            val scores = FloatArray(9789)
            for (start in BirdSoundAudio.windowStarts) {
                currentCoroutineContext().ensureActive()
                checkCurrent(token)
                val first = samples[start]
                if ((start + 1 until start + 96_000).all { samples[it] == first }) continue
                val values = runWindowLocked(active, samples, start, token)
                for (i in scores.indices) scores[i] = maxOf(scores[i], values[i])
            }
            currentCoroutineContext().ensureActive()
            checkCurrent(token)
            scores
        }
    }

    private fun runWindowLocked(
        active: OrtSession,
        samples: FloatArray,
        start: Int,
        token: Long
    ): FloatArray {
        val input = ByteBuffer.allocateDirect(96_000 * 4).order(ByteOrder.nativeOrder()).asFloatBuffer()
        input.put(samples, start, 96_000).rewind()
        return OnnxTensor.createTensor(environment, input, longArrayOf(1, 96_000)).use { tensor ->
            OrtSession.RunOptions().use { options ->
                synchronized(stateLock) {
                    if (closed || generation != token) throw CancellationException("Inference cancelled")
                    running = options
                }
                try {
                    active.run(mapOf("input" to tensor), setOf("predictions"), options).use { result ->
                        val prediction = result[0] as OnnxTensor
                        check(prediction.info.type == OnnxJavaType.FLOAT && prediction.info.shape.contentEquals(longArrayOf(1, 9789)))
                        val values = (prediction.value as Array<*>).single() as FloatArray
                        check(values.size == 9789 && values.all { it.isFinite() && it in 0f..1f })
                        values
                    }
                } finally {
                    synchronized(stateLock) { running = null }
                }
            }
        }
    }

    open fun cancelCurrentRun() = synchronized(stateLock) {
        generation++
        running?.setTerminate(true)
    }

    open suspend fun close() {
        synchronized(stateLock) {
            closed = true
            generation++
            running?.setTerminate(true)
        }
        withContext(NonCancellable + Dispatchers.IO) {
            mutex.withLock {
                session?.close()
                session = null
                model = null
            }
        }
    }

    private fun checkCurrent(token: Long) = synchronized(stateLock) {
        if (closed || generation != token) throw CancellationException("Operation cancelled")
    }
}
