/*
 * Copyright 2019 BLK Technologies Limited (web3labs.com).
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */
package io.web3j.libp2p.shared.conversion

import io.web3j.libp2p.shared.ext.toBase64String
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.io.ByteArrayInputStream
import java.nio.ByteBuffer

class VarintTest {

    /**
     * Tests the conversion of a ULong into a varint.
     */
    @Test
    fun varintTest1() {
        doVarintTest(300, "rAI=")
        doVarintTest(98839, "l4QG")
    }

    @Test
    fun varlongTest1() {
        doVarlongTest(300L, "rAI=")
        doVarlongTest(98839L, "l4QG")
        doVarlongTest(948820459L, "67O3xAM=")
        doVarlongTest(9012390123902158445L, "7Yy1w5ainIl9")
    }

    private fun doVarintTest(inputInt: Int, expectedVarintB64: String) {
        // First, create a byte array.
        val bytes: ByteArray = Varint.putVarInt(inputInt)
        assertEquals(expectedVarintB64, bytes.toBase64String())

        // Now convert the byte array.
        val fromBytes: Int = Varint.getVarInt(bytes)
        assertEquals(inputInt, fromBytes)

        // Convert the byte buffer.
        val fromByteBuffer: Int = Varint.getVarInt(ByteBuffer.wrap(bytes))
        assertEquals(inputInt, fromByteBuffer)

        // Convert the stream.
        val fromStream: Int = Varint.getVarInt(ByteArrayInputStream(bytes))
        assertEquals(inputInt, fromStream)
    }

    private fun doVarlongTest(inputLong: Long, expectedVarintB64: String) {
        // First, create a byte array.
        val bytes: ByteArray = Varint.putVarLong(inputLong)
        assertEquals(expectedVarintB64, bytes.toBase64String())

        // Now convert the byte array.
        val fromBytes: Long = Varint.getVarLong(bytes)
        assertEquals(inputLong, fromBytes)

        // Convert the byte buffer.
        val fromByteBuffer: Long = Varint.getVarLong(ByteBuffer.wrap(bytes))
        assertEquals(inputLong, fromByteBuffer)

        // Convert the stream.
        val fromStream: Long = Varint.getVarLong(ByteArrayInputStream(bytes))
        assertEquals(inputLong, fromStream)
    }
}
