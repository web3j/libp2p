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

import kotlin.experimental.and

/**
 * Our custom implementation of a <code>varint</code>.
 *
 * [@see https://developers.google.com/protocol-buffers/docs/encoding#varints]
 */
object Varint {

    /**
     * The 0 UInt value.
     */
    private val UINT_0 = 0.toULong()

    /**
     * Used to obtain the last 7 bits.
     */
    private val _0x7F = 0x7f.toULong()

    /**
     * Used to set the 7th bit.
     */
    private val _0x80 = 0x80.toULong()

    /**
     * Converts the given byte array into an unsigned long.
     * @param bytes the byte array.
     * @return the equivalent unsigned long value.
     */
    fun readUnsignedVarint(bytes: ByteArray): ULong {
        var shift = 0
        var result = UINT_0

        bytes.forEach { byte ->
            result += byte.and(0x7F).toULong().shl(shift)
            shift += 7
        }

        return result
    }

    /**
     * Converts the given UInt value into a varint in a byte array.
     * @param longVal the unsigned integer value.
     * @return the byte array containing the varint representation.
     */
    fun writeUnsignedVarint(longVal: ULong): ByteArray {
        val byteList = mutableListOf<Byte>()

        var current = longVal
        var processingFinished = false
        // Grab the last 7 bits.
        while (!processingFinished) {
            val last7Bits: ULong = current.and(_0x7F)
            current = current.shr(7)
            processingFinished = current == UINT_0

            if (processingFinished) {
                // No need to set the 7th bit.
                byteList.add(last7Bits.toByte())
            } else {
                // Set the 7th bit to indicate there is more to come.
                byteList.add(_0x80.or(last7Bits).toByte())
            }
        }

        return byteList.toByteArray()
    }
}