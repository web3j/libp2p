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

import io.web3j.libp2p.shared.env.Libp2pException
import java.io.ByteArrayInputStream
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.nio.ByteBuffer
import kotlin.experimental.and

/**
 * Common methods to encode and decode varints and varlongs into ByteBuffers and
 * arrays. <br />
 * This is a direct port (mostly auto-ported thanks to IntelliJ) of:
 * [https://github.com/bazelbuild/bazel/blob/master/src/main/java/com/google/devtools/build/lib/util/VarInt.java]
 */
object Varint {

    /**
     * Maximum encoded size of 32-bit positive integers (in bytes)
     */
    val MAX_VARINT_SIZE = 5

    /**
     * maximum encoded size of 64-bit longs, and negative 32-bit ints (in bytes)
     */
    val MAX_VARLONG_SIZE = 10

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
     * Reads an long  varint from the given byte array.
     * @param inputStream the byte array.
     * @return the equivalent long value.
     */
    fun readVarLong(inputStream: InputStream): Long {
        return getVarLong(inputStream)
    }

    /**
     * Reads an integer varint from the given byte array.
     * @param inputStream the byte array.
     * @return the equivalent int value.
     */
    fun readVarInt(inputStream: InputStream): Int {
        return getVarInt(inputStream)
    }

    /**
     * Returns the number of bytes required to store the given Long as a varint.
     * @param value the value.
     * @return the number of bytes.
     */
    fun sizeOf(value: Long): Int {
        return varLongSize(value)
    }

    /**
     * Returns the number of bytes required to store the given Int as a varint.
     * @param value the value.
     * @return the number of bytes.
     */
    fun sizeOf(value: Int): Int {
        return varIntSize(value)
    }

    /**
     * Returns the number of bytes required to store the given ULong as a varint.
     * @param value the value.
     * @return the number of bytes.
     */
    fun sizeOf(value: ULong): Int {
        return varLongSize(value.toLong())
    }

    /**
     * Creates an varint from the given long value.
     * @param value the long value to be converted.
     * @return the varint as a byte array.
     */
    fun toVarLong(value: Long): ByteArray {
        return putVarLong(value)
    }

    /**
     * Creates an varint from the given int value.
     * @param value the int value to be converted.
     * @return the varint as a byte array.
     */
    fun toVarInt(value: Int): ByteArray {
        return putVarInt(value)
    }

    /**
     * Creates the equivalent int value from the given varint.
     * @param bytes the bytes of the varint.
     * @return the int value.
     */
    fun fromVarInt(bytes: ByteArray): Int {
        return ByteArrayInputStream(bytes).use { readVarInt(it) }
    }

    /**
     * Creates the equivalent long value from the given varint.
     * @param bytes the bytes of the varint.
     * @return the long value.
     */
    fun fromVarLong(bytes: ByteArray): Long {
        return ByteArrayInputStream(bytes).use { readVarLong(it) }
    }

    /**
     * Returns the encoding size in bytes of its input value.
     *
     * @param value the integer to be measured
     * @return the encoding size in bytes of its input value
     */
    fun varIntSize(value: Int): Int {
        var i = value
        var result = 0
        do {
            result++
            i = i ushr 7
        } while (i != 0)
        return result
    }

    /**
     * Reads a varint  from src, places its values into the first element of
     * dst and returns the offset in to src of the first byte after the varint.
     *
     * @param src source buffer to retrieve from
     * @param offsetValue offset within src
     * @param dst the resulting int value
     * @return the updated offset after reading the varint
     */
    fun getVarInt(src: ByteArray, offsetValue: Int, dst: IntArray): Int {
        var offset = offsetValue
        var result = 0
        var shift = 0
        var b: Int
        do {
            if (shift >= 32) {
                // Out of range
                throw IndexOutOfBoundsException("varint too long")
            }
            // Get 7 bits from next byte
            b = src[offset++].toInt()
            result = result or (b and 0x7F shl shift)
            shift += 7
        } while (b and 0x80 != 0)
        dst[0] = result
        return offset
    }

    /**
     * Encodes an integer in a variable-length encoding, 7 bits per byte, into a
     * destination byte[], following the protocol buffer convention.
     *
     * @param value the int value to write to sink
     * @param sink the sink buffer to write to
     * @param offsetValue the offset within sink to begin writing
     * @return the updated offset after writing the varint
     */
    fun putVarInt(value: Int, sink: ByteArray, offsetValue: Int): Int {
        var v = value
        var offset = offsetValue
        do {
            // Encode next 7 bits + terminator bit
            val bits = v and 0x7F
            v = v ushr 7
            val b = (bits + if (v != 0) 0x80 else 0).toByte()
            sink[offset++] = b
        } while (v != 0)
        return offset
    }

    /**
     * Writes the given varint to the sink.
     * @param value the int value to be written.
     * @param sink the sink to write the varint to.
     * @return the updated sink.
     */
    fun putVarInt(value: Int, sink: ByteArray): ByteArray {
        var v = value
        var offset = 0
        do {
            // Encode next 7 bits + terminator bit
            val bits = v and 0x7F
            v = v ushr 7
            val b = (bits + if (v != 0) 0x80 else 0).toByte()
            sink[offset++] = b
        } while (v != 0)
        return sink
    }

    /**
     * Writes the given varint to a byte array.
     * @param value the int value to be written.
     * @return the byte array containing the varint.
     */
    fun putVarInt(value: Int): ByteArray {
        val result = ByteArray(varIntSize(value))
        var v = value
        var offset = 0
        do {
            // Encode next 7 bits + terminator bit
            val bits = v and 0x7F
            v = v ushr 7
            val b = (bits + if (v != 0) 0x80 else 0).toByte()
            result[offset++] = b
        } while (v != 0)
        return result
    }

    fun getVarInt(src: ByteBuffer): Int {
        return getVarInt { src.get().toInt() }
    }

    fun getVarInt(src: ByteArray): Int {
        return getVarInt(ByteBuffer.wrap(src))
    }

    /**
     * Reads a varint from the current position of the given ByteBuffer and
     * returns the decoded value as 32 bit integer.
     *
     *
     * The position of the buffer is advanced to the first byte after the
     * decoded varint.
     *
     * @param readNext the function that returns int values to be processed.
     * @return The integer value of the decoded varint
     */
    private fun getVarInt(readNext: () -> Int): Int {
        var tmp: Int = readNext()

        if (tmp >= 0) {
            return tmp
        }
        var result = tmp and 0x7f
        tmp = readNext()
        if (tmp >= 0) {
            result = result or (tmp shl 7)
        } else {
            result = result or (tmp and 0x7f shl 7)
            tmp = readNext()
            if (tmp >= 0) {
                result = result or (tmp shl 14)
            } else {
                result = result or (tmp and 0x7f shl 14)
                tmp = readNext()
                if (tmp >= 0) {
                    result = result or (tmp shl 21)
                } else {
                    result = result or (tmp and 0x7f shl 21)
                    val t2 = readNext()
                    result = result or (t2 shl 28)
                    while (tmp < 0) {
                        // We get into this loop only in the case of overflow.
                        // By doing this, we can call getVarInt() instead of
                        // getVarLong() when we only need an int.
                        tmp = readNext()
                    }
                }
            }
        }
        return result
    }

    /**
     * Encodes an integer in a variable-length encoding, 7 bits per byte, to a
     * ByteBuffer sink.
     *
     * @param value the value to encode
     * @param sink the ByteBuffer to add the encoded value
     */
    fun putVarInt(value: Int, sink: ByteBuffer) {
        var v = value
        while (true) {
            val bits = v and 0x7f
            v = v ushr 7
            if (v == 0) {
                sink.put(bits.toByte())
                return
            }
            sink.put((bits or 0x80).toByte())
        }
    }

    /**
     * Reads a varint from the given InputStream and returns the decoded value
     * as an int.
     *
     * @param inputStream the InputStream to read from
     */
    @Throws(IOException::class)
    fun getVarInt(inputStream: InputStream): Int {
        var result = 0
        var shift = 0
        var b: Int
        do {
            if (shift >= 32) {
                // Out of range
                throw IndexOutOfBoundsException("varint too long")
            }
            // Get 7 bits from next byte
            b = inputStream.read()
            result = result or (b and 0x7F shl shift)
            shift += 7
        } while (b and 0x80 != 0)
        return result
    }

    /**
     * Encodes an integer in a variable-length encoding, 7 bits per byte, and
     * writes it to the given OutputStream.
     *
     * @param v the value to encode
     * @param outputStream the OutputStream to write to
     */
    @Throws(IOException::class)
    fun putVarInt(v: Int, outputStream: OutputStream) {
        val bytes = ByteArray(varIntSize(v))
        putVarInt(v, bytes, 0)
        outputStream.write(bytes)
    }

    /**
     * Returns the encoding size in bytes of its input value.
     *
     * @param value the long to be measured
     * @return the encoding size in bytes of a given long value.
     */
    fun varLongSize(value: Long): Int {
        var v = value
        var result = 0
        do {
            result++
            v = v ushr 7
        } while (v != 0L)
        return result
    }

    fun getVarLong(src: InputStream): Long {
        val readNext: () -> Long = {
            // read() returns an unsigned int, which is not good.
            with(ByteArray(1)) {
                if (src.read(this) == 0) {
                    // End of stream.
                    throw Libp2pException("No more bytes available to read")
                }
                this[0].toLong()
            }
        }

        return getVarLong(readNext)
    }

    /**
     * Reads an up to 64 bit long varint from the current position of the
     * given ByteBuffer and returns the decoded value as long.
     *
     *
     * The position of the buffer is advanced to the first byte after the
     * decoded varint.
     *
     * @param src the ByteBuffer to get the var int from
     * @return The integer value of the decoded long varint
     */
    fun getVarLong(src: ByteBuffer): Long {
        return getVarLong { src.get().toLong() }
    }

    fun getVarLong(src: ByteArray): Long {
        return getVarLong(ByteBuffer.wrap(src))
    }

    private fun getVarLong(readNext: () -> Long): Long {
        var tmp: Long = readNext()
        if (tmp >= 0) {
            return tmp
        }
        var result = tmp and 0x7f
        tmp = readNext()
        if (tmp >= 0) {
            result = result or (tmp shl 7)
        } else {
            result = result or (tmp and 0x7f shl 7)
            tmp = readNext()
            if (tmp >= 0) {
                result = result or (tmp shl 14)
            } else {
                result = result or (tmp and 0x7f shl 14)
                tmp = readNext()
                if (tmp >= 0) {
                    result = result or (tmp shl 21)
                } else {
                    result = result or (tmp and 0x7f shl 21)
                    tmp = readNext()
                    if (tmp >= 0) {
                        result = result or (tmp shl 28)
                    } else {
                        result = result or (tmp and 0x7f shl 28)
                        tmp = readNext()
                        if (tmp >= 0) {
                            result = result or (tmp shl 35)
                        } else {
                            result = result or (tmp and 0x7f shl 35)
                            tmp = readNext()
                            if (tmp >= 0) {
                                result = result or (tmp shl 42)
                            } else {
                                result = result or (tmp and 0x7f shl 42)
                                tmp = readNext()
                                if (tmp >= 0) {
                                    result = result or (tmp shl 49)
                                } else {
                                    result = result or (tmp and 0x7f shl 49)
                                    tmp = readNext()
                                    if (tmp >= 0) {
                                        result = result or (tmp shl 56)
                                    } else {
                                        result = result or (tmp and 0x7f shl 56)
                                        result = result or (readNext() shl 63)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return result
    }

    /**
     * Encodes a long integer in a variable-length encoding, 7 bits per byte, to a
     * ByteBuffer sink.
     *
     * @param value the value to encode
     * @param sink the ByteBuffer to add the encoded value
     */
    fun putVarLong(value: Long, sink: ByteBuffer) {
        var v = value
        while (true) {
            val bits = v.toInt() and 0x7f
            v = v ushr 7
            if (v == 0L) {
                sink.put(bits.toByte())
                return
            }
            sink.put((bits or 0x80).toByte())
        }
    }

    /**
     * Encodes a long integer in a variable-length encoding, 7 bits per byte, to a
     * ByteArray.
     *
     * @param value the value to encode
     * @return the byte array containing the encoded value.
     */
    fun putVarLong(value: Long): ByteArray {
        val result = mutableListOf<Byte>()

        var v = value
        while (true) {
            val bits = v.toInt() and 0x7f
            v = v ushr 7
            if (v == 0L) {
                result.add(bits.toByte())
                return result.toByteArray()
            }
            result.add((bits or 0x80).toByte())
        }
    }

    /**
     * Encodes a long integer in a variable-length encoding, 7 bits per byte, to a
     * OutputStream sink.
     *
     * @param v the value to encode
     * @param outputStream the OutputStream to add the encoded value
     */
    fun putVarLong(v: Long, outputStream: OutputStream) {
        outputStream.write(putVarLong(v))
    }

    /**
     * Writes the given byte array to a destination byte array and prefixes it
     * with the length of the data as a varint.
     * @param data the data to be written.
     * @return a byte array containing the varint length followed by the [data].
     */
    fun writeVarintPrefixedBytes(data: ByteArray): ByteArray {
        val dataLength = data.size
        val varintSize = varIntSize(dataLength)
        val result = ByteArray(varintSize + dataLength)
        System.arraycopy(data, 0, putVarInt(dataLength, result), varintSize, dataLength)
        return result
    }

    /**
     * Converts the given ULong value into a varint.
     * @param longVal the unsigned long value.
     * @return the bytes that form the varint.
     */
    fun toVarULong(longVal: ULong): ByteArray {
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

    fun getVarULong(src: ByteBuffer): ULong {
        return getVarULong { src.get() }
    }

    fun getVarULong(src: ByteArray): ULong {
        return getVarULong(ByteBuffer.wrap(src))
    }

    fun getVarULong(src: InputStream): ULong {
        val readNext: () -> Byte? = {
            // read() returns an unsigned int, which is not good.
            with(ByteArray(1)) {
                if (src.read(this) == 0) {
                    null
                }
                this[0]
            }
        }

        return getVarULong(readNext)
    }

    /**
     * Reads an unsigned varint from the givne byte array.
     * @param inputStream the byte array.
     * @return the equivalent unsigned long value.
     */
    private fun getVarULong(readNext: () -> Byte?): ULong {
        var shift = 0
        val mask = 0x80.toByte()

        val oneByte = readNext() ?: return UINT_0
        var shouldReadMore = oneByte.and(mask) == mask
        var result = oneByte.and(0x7F).toULong().shl(shift)
        shift += 7

        while (shouldReadMore) {
            val byteRead = readNext()
            if (byteRead == null) {
                byteRead!!
                result += byteRead.and(0x7F).toULong().shl(shift)
                shouldReadMore = byteRead.and(mask) == mask
            } else {
                shouldReadMore = false
            }

            // Next iteration.
            shift += 7
        }

        return result
    }
}
