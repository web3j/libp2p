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
package io.web3j.libp2p.shared.ext

import io.web3j.libp2p.shared.conversion.Varint
import io.web3j.libp2p.shared.env.Libp2pException
import java.nio.ByteBuffer
import java.util.Base64

/**
 * Encodes this byte array using base-64 encoding.
 * @return the encoded value.
 */
fun ByteArray.toBase64(): ByteArray = Base64.getEncoder().encode(this)

/**
 * Encodes this byte array using base-64 encoding.
 * @return the encoded value as a string.
 */
fun ByteArray.toBase64String(): String = String(toBase64())

/**
 * Converts the given byte array to an int, assuming there are exactly 4 bytes in the array.
 * @return the integer value in the byte array.
 * @throws Libp2pException if the array cannot be converted.
 */
@Throws(Libp2pException::class)
fun ByteArray.toInt(): Int {
    if (this.size > 4) {
        throw Libp2pException("Cannot convert ${this.size} bytes to an int")
    }
    return ByteBuffer.wrap(this).int
}

/**
 * Reads the contents of this array as a varint-prefixed array.
 * @return a pair containing the contents of the byte array (matching the size of the varint), and any remaining
 * bytes in the array.
 */
fun ByteArray.readVarintPrefixedMessage(): Pair<ByteArray, ByteArray?>? {
    val numBytesJustReceived = size
    val expectedByteCount = Varint.fromVarInt(this)
    if (expectedByteCount > 0) {

        val sizeOfVarintInBytes = Varint.sizeOf(expectedByteCount)
        val dataPortion = drop(sizeOfVarintInBytes)

        if (numBytesJustReceived == sizeOfVarintInBytes + expectedByteCount) {
            // Have the entire stream, nothing left.
            return Pair(dataPortion.toByteArray(), null)
        } else {
            // There are multiple parts to the stream
            return Pair(
                dataPortion.subList(0, expectedByteCount).toByteArray(),
                dataPortion.subList(expectedByteCount, dataPortion.size).toByteArray()
            )
        }
    }
    return null
}

/**
 * Creates a serialized version of the given message that can be sent to a peer.
 * @param bytes the byte array to be serialized.
 * @return the byte array equivalent.
 */
fun ByteArray.toVarintPrefixedByteArray(): ByteArray {
    return Varint.writeVarintPrefixedBytes(this)
}

/**
 * Compares the contents of this array against the other instance,
 * returning the comparison result of the first pair of unequal bytes.
 * @param other the other byte array to compare against.
 * @return the result.
 */
fun ByteArray.compareAgainst(other: ByteArray): Int {
    for (i in 0..size) {
        if (i >= other.size) {
            return -1
        }
        val comparison = this[i].compareTo(other[i])
        if (comparison != 0) {
            return comparison
        }
    }
    return 0
}
