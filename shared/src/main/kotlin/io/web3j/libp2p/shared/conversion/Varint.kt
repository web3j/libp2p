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

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
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
     * Reads an unsigned varint from the givne byte array.
     * @param inputStream the byte array.
     * @return the equivalent unsigned long value.
     */
    fun readUnsignedVarint(inputStream: InputStream): ULong {
        var shift = 0
        val mask = 0x80.toByte()

        val oneByteArray = ByteArray(1)
        var numBytesRead = inputStream.read(oneByteArray)

        var shouldReadMore = numBytesRead == 1 && oneByteArray[0].and(mask) == mask
        var result = if (numBytesRead == 1) oneByteArray[0].and(0x7F).toULong().shl(shift) else UINT_0
        shift += 7

        while (shouldReadMore) {
            numBytesRead = inputStream.read(oneByteArray)
            if (numBytesRead == 1) {
                result += oneByteArray[0].and(0x7F).toULong().shl(shift)
            }

            // Next iteration.
            shift += 7
            shouldReadMore = numBytesRead == 1 && oneByteArray[0].and(mask) == mask
        }

        return result
    }

    /**
     * Creates an unsigned varint from the given long value.
     * @param value the long value to be converted.
     * @return the varint as a byte array.
     */
    fun toVarint(value: ULong): ByteArray {
        val outputStream = ByteArrayOutputStream()
        outputStream.use {
            Varint.writeUnsignedVarint(value, it)
            return outputStream.toByteArray()
        }
    }

    /**
     * Creates the equivalent ULong value from the given varint.
     * @param bytes the bytes of the varint.
     * @return the ulong value.
     */
    fun fromVarint(bytes: ByteArray): ULong {
        return ByteArrayInputStream(bytes).use { Varint.readUnsignedVarint(it) }
    }

    /**
     * Converts the given int value into a varint and writes the bytes onto the output stream.
     * @param intVal the int value.
     * @param outputStream the output stream to write the varint to.
     */
    fun writeUnsignedVarint(intVal: Int, outputStream: OutputStream): Unit =
        writeUnsignedVarint(intVal.toULong(), outputStream)

    /**
     * Converts the given long value into a varint and writes the bytes onto the output stream.
     * @param longVal the unsigned long value.
     * @param outputStream the output stream to write the varint to.
     */
    fun writeUnsignedVarint(longVal: Long, outputStream: OutputStream): Unit =
        writeUnsignedVarint(longVal.toULong(), outputStream)

    /**
     * Converts the given ULong value into a varint and writes the bytes onto the output stream.
     * @param longVal the unsigned long value.
     * @param outputStream the output stream to write the varint to.
     */
    fun writeUnsignedVarint(longVal: ULong, outputStream: OutputStream): Unit {
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

        outputStream.write(byteList.toByteArray())
    }
}