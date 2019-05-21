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
package io.web3j.libp2p.security.secio.util

import io.web3j.libp2p.security.secio.SecioErrorCodes
import io.web3j.libp2p.security.secio.SecioException
import io.web3j.libp2p.security.secio.model.ExchangeMessage
import io.web3j.libp2p.security.secio.model.ProposeMessage
import org.slf4j.LoggerFactory
import spipe.pb.Spipe
import java.io.InputStream
import java.nio.ByteBuffer

/**
 * Provides convenience methods to operate on prototype messages.
 */
object PrototypeUtil {

    private val LOGGER = LoggerFactory.getLogger(PrototypeUtil.javaClass)!!

    /**
     * Parses the given byte array into a `Propose` message.
     * @param byteArray the byte array.
     * @return the propose message.
     */
    private fun parsePrototypeProposeMessage(byteArray: ByteArray): Spipe.Propose {
        val dataByteCount = with(byteArray.sliceArray(IntRange(0, 3))) {
            ByteBuffer.wrap(this).getInt(0)
        }

        if (dataByteCount + 4 != byteArray.size) {
            LOGGER.warn("Propose message stream does not contain the expected byte count:" +
                    " expected $dataByteCount but got ${byteArray.size - 4}")
            throw SecioException(SecioErrorCodes.INVALID_PROPOSAL_STRUCTURE)
        }

        return Spipe.Propose.parseFrom(byteArray.drop(4).toByteArray())
    }

    /**
     * Parses the given byte stream into a `Propose` message.
     * @param inputStream the byte stream.
     * @return the propose message.
     */
    private fun parsePrototypeProposeMessageStream(inputStream: InputStream): Spipe.Propose {
        inputStream.use {
            val dataByteCount = with(ByteArray(4)) {
                val numBytesRead = it.read(this, 0, 4)
                if (numBytesRead != 4) {
                    LOGGER.warn("Propose message stream did not provide 4 bytes for leading int, got $numBytesRead")
                    throw SecioException(SecioErrorCodes.PROPOSAL_STREAM_ERROR)
                }
                ByteBuffer.wrap(this).getInt(0)
            }

            return with(ByteArray(dataByteCount)) {
                val dataBytesReadCount = inputStream.read(this)

                if (dataBytesReadCount != dataByteCount) {
                    LOGGER.warn("Propose message stream was not read completely")
                    throw SecioException(SecioErrorCodes.PROPOSAL_STREAM_ERROR)
                }

                Spipe.Propose.parseFrom(this)
            }
        }
    }

    /**
     * Parses the given byte array into a wrapper that extends a `Propose` message.
     * @param byteArray the byte array.
     * @return an extension to the `Propose` message.
     */
    fun parseProposeMessage(byteArray: ByteArray): ProposeMessage {
        return ProposeMessage.fromPrototype(parsePrototypeProposeMessage(byteArray))
    }

    /**
     * Parses the given byte stream into a wrapper that extends a `Propose` message.
     * @param inputStream the byte stream.
     * @return an extension to the `Propose` message.
     */
    fun parseProposeMessageStream(inputStream: InputStream): ProposeMessage {
        return ProposeMessage.fromPrototype(parsePrototypeProposeMessageStream(inputStream))
    }

    /**
     * Parses the given byte array into a `Exchange` message.
     * @param byteArray the byte array.
     * @return the exchange message.
     */
    private fun parsePrototypeExchangeMessage(byteArray: ByteArray): Spipe.Exchange {
        val dataByteCount = with(byteArray.sliceArray(IntRange(0, 3))) {
            ByteBuffer.wrap(this).getInt(0)
        }

        if (dataByteCount + 4 != byteArray.size) {
            LOGGER.warn("Propose message stream does not contain the expected byte count: " +
                    "expected $dataByteCount but got ${byteArray.size - 4}")
            throw SecioException(SecioErrorCodes.INVALID_PROPOSAL_STRUCTURE)
        }

        return Spipe.Exchange.parseFrom(byteArray.drop(4).toByteArray())
    }

    /**
     * Parses the given byte stream into a `Exchange` message.
     * @param inputStream the byte stream.
     * @return the exchange message.
     */
    private fun parsePrototypeExchangeMessageStream(inputStream: InputStream): Spipe.Exchange {
        inputStream.use {
            val dataByteCount = with(ByteArray(4)) {
                val numBytesRead = it.read(this, 0, 4)
                if (numBytesRead != 4) {
                    LOGGER.warn("Propose message stream did not provide 4 bytes for leading int, got $numBytesRead")
                    throw SecioException(SecioErrorCodes.PROPOSAL_STREAM_ERROR)
                }
                ByteBuffer.wrap(this).getInt(0)
            }

            return with(ByteArray(dataByteCount)) {
                val dataBytesReadCount = inputStream.read(this)

                if (dataBytesReadCount != dataByteCount) {
                    LOGGER.warn("Propose message stream was not read completely")
                    throw SecioException(SecioErrorCodes.PROPOSAL_STREAM_ERROR)
                }

                Spipe.Exchange.parseFrom(this)
            }
        }
    }

    /**
     * Parses the given byte array into a wrapper that extends a `Exchange` message.
     * @param byteArray the byte array.
     * @return an extension to the `Exchange` message.
     */
    fun parseExchangeMessage(byteArray: ByteArray): ExchangeMessage {
        return ExchangeMessage.fromPrototype(parsePrototypeExchangeMessage(byteArray))
    }

    /**
     * Parses the given byte stream into a wrapper that extends a `Exchange` message.
     * @param inputStream the byte stream.
     * @return an extension to the `Exchange` message.
     */
    fun parseExchangeMessageStream(inputStream: InputStream): ExchangeMessage {
        return ExchangeMessage.fromPrototype(parsePrototypeExchangeMessageStream(inputStream))
    }
}
