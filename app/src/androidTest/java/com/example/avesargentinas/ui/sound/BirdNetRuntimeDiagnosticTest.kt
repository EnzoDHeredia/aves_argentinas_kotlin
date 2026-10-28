package com.example.avesargentinas.ui.sound

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import android.util.Log
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Test
import org.junit.runner.RunWith
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.channels.FileChannel
import kotlin.math.abs

@RunWith(AndroidJUnit4::class)
class BirdNetRuntimeDiagnosticTest {
    @Test fun compareCpuOptimizations() {
        val testAssets = InstrumentationRegistry.getInstrumentation().context.assets
        val assets = InstrumentationRegistry.getInstrumentation().targetContext.assets
        val model = assets.openFd("birdnet/$BIRDNET_MODEL_FILE").use { fd ->
            fd.createInputStream().use { it.channel.map(FileChannel.MapMode.READ_ONLY, fd.startOffset, fd.length) }
        }
        val env = OrtEnvironment.getEnvironment()
        for (level in listOf(OrtSession.SessionOptions.OptLevel.NO_OPT, OrtSession.SessionOptions.OptLevel.ALL_OPT)) {
            OrtSession.SessionOptions().use { options ->
                options.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.SEQUENTIAL)
                options.setIntraOpNumThreads(2)
                options.setOptimizationLevel(level)
                env.createSession(model, options).use { session ->
                    for (n in 1..3) {
                        val signal = testAssets.open("birdnet/signal-$n.f32").use { it.readBytes() }
                        val ref = testAssets.open("birdnet/reference-$n.f32").use { it.readBytes() }
                        val expected = ByteBuffer.wrap(ref).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()
                        val floats = ByteBuffer.allocateDirect(signal.size).order(ByteOrder.nativeOrder()).asFloatBuffer()
                        floats.put(ByteBuffer.wrap(signal).order(ByteOrder.LITTLE_ENDIAN).asFloatBuffer()).rewind()
                        OnnxTensor.createTensor(env, floats, longArrayOf(1, 96000)).use { input ->
                            session.run(mapOf("input" to input), setOf("predictions")).use { result ->
                                val scores = ((result[0] as OnnxTensor).value as Array<*>)[0] as FloatArray
                                var max = 0f
                                for (score in scores) max = maxOf(max, abs(score - expected.get()))
                                Log.i("BirdNetT4Diag", "level=$level fixture=$n max=$max")
                            }
                        }
                    }
                }
            }
        }
    }
}
