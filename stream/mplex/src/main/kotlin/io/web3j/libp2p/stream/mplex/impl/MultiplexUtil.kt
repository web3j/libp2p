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
package io.web3j.libp2p.stream.mplex.impl

import io.web3j.libp2p.shared.conversion.Varint
import org.slf4j.LoggerFactory
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.io.OutputStream
import kotlin.experimental.and

/**
 * A set of utility functions for implementing the mplex protocol.
 */
object MultiplexUtil {

    private val LOGGER = LoggerFactory.getLogger(MultiplexUtil.javaClass)!!

    const val MPLEX_VERSION = "mplex/6.7.0"

    const val FLAG_NEW_STREAM = 0x00.toByte()
    const val FLAG_MESSAGE_RECEIVER = 0x01.toByte()
    const val FLAG_MESSAGE_INITIATOR = 0x02.toByte()
    const val FLAG_CLOSE_RECEIVER = 0x03.toByte()
    const val FLAG_CLOSE_INITIATOR = 0x04.toByte()
    const val FLAG_RESET_RECEIVER = 0x05.toByte()
    const val FLAG_RESET_INITIATOR = 0x06.toByte()


    /**
     * Reads the protocol data from the input stream.
     * @param inputStream the stream to read from.
     * @return a triple containing the stream ID, flags, and data as a byte array.
     */
    fun readProtocolData(inputStream: InputStream): Triple<ULong, Byte, ByteArray> {
        // header.
        val headerVarint: ULong = Varint.readUnsignedVarint(inputStream)
        val flags: ULong = headerVarint.and(0x07.toULong())
        val streamId: ULong = headerVarint.shr(3)

        // data length.
        val dataLength: ULong = Varint.readUnsignedVarint(inputStream)
        val dataLengthAsInt = dataLength.toInt()

        // Data!
        val data = ByteArray(dataLengthAsInt)
        val bytesRead = inputStream.read(data)
        if (bytesRead != dataLengthAsInt) {
            // TODO: handle this case!
            LOGGER.warn("Not all bytes were read: $bytesRead but wanted $dataLengthAsInt")
        }

        return Triple(streamId, flags.toByte(), data)
    }

    /**
     * Reads the protocol data from the byte array.
     * @param byteArray the byte array to read from.
     * @return a triple containing the stream ID, flags, and data as a byte array.
     */
    fun readProtocolData(byteArray: ByteArray): Triple<ULong, Byte, ByteArray> {
        return ByteArrayInputStream(byteArray).use {
            return readProtocolData(
                it
            )
        }
    }

    /**
     * Composes the protocol data to be written out to a stream.
     * @param streamId the ID of the stream that the message is intended for.
     * @param flags the flags to be used in the message header.
     * @param data the data to be sent to the other party.
     * @return a byte array that can be written out that encapsulates the given parameters.
     */
    fun composeProtocolData(streamId: ULong, flags: Byte, data: ByteArray = ByteArray(0)): ByteArray {
        val outputStream = ByteArrayOutputStream()
        outputStream.use {
            composeProtocolData(streamId, flags, data, outputStream)
            return outputStream.toByteArray()
        }
    }

    /**
     * Composes the protocol data to be written out to a stream.
     * @param streamId the ID of the stream that the message is intended for.
     * @param flags the flags to be used in the message header.
     * @param data the data to be sent to the other party.
     * @param outputStream the stream to write the protocol data to.
     */
    fun composeProtocolData(streamId: ULong, flags: Byte, data: ByteArray, outputStream: OutputStream): Unit {
        // Ensure we only have the last 3 bits sits.
        val flagsAsLong = flags.and(0x07).toULong()

        // The last 3 bits contain the flags, the remaining bits contain the stream ID.
        val header: ByteArray = Varint.toVarint(streamId.shl(3).or(flagsAsLong))
        val headerLength = header.size

        val dataLength = data.size
        val lengthOfDataAsVarint: ByteArray = Varint.toVarint(dataLength.toULong())
        val lengthOfDataVarint = lengthOfDataAsVarint.size

        // Concatenate the arrays.
        val result = ByteArray(headerLength + lengthOfDataVarint + dataLength)

        var bytesWritten = 0
        System.arraycopy(header, 0, result, 0, headerLength)

        bytesWritten += headerLength
        System.arraycopy(lengthOfDataAsVarint, 0, result, bytesWritten, lengthOfDataVarint)

        bytesWritten += lengthOfDataVarint
        System.arraycopy(data, 0, result, bytesWritten, dataLength)
        outputStream.write(result)
    }
}