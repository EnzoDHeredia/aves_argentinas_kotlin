package com.example.avesargentinas.ui.sound

import org.junit.Assert.assertThrows
import org.junit.Test
import java.nio.ByteBuffer

class BirdNetModelIntegrityTest {
    @Test
    fun rejectsModelWithWrongSizeWithoutMovingBufferPosition() {
        val model = ByteBuffer.wrap(byteArrayOf(1, 2, 3))
        model.position(1)
        assertThrows(IllegalArgumentException::class.java) { verifyBirdNetModel(model) }
        org.junit.Assert.assertEquals(1, model.position())
    }

    @Test
    fun rejectsModelWithCorrectSizeButWrongHash() {
        val model = ByteBuffer.allocate(65_830_813)
        assertThrows(IllegalArgumentException::class.java) { verifyBirdNetModel(model) }
    }
}
