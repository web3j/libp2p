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
package io.web3j.libp2p.transport.tcp.provider.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.web3j.libp2p.crypto.unmarshalPrivateKey
import io.web3j.libp2p.crypto.unmarshalPublicKey
import io.web3j.libp2p.security.secio.model.ProposeMessage
import io.web3j.libp2p.security.secio.util.PrototypeUtil
import io.web3j.libp2p.shared.conversion.Varint
import org.slf4j.LoggerFactory
import spipe.pb.Spipe
import java.nio.ByteBuffer
import java.util.*

class AccumulatorInboundHandler : ChannelInboundHandlerAdapter() {

    var receivedMultistream = false
    var sentMultistream = false
    var receivedSecio = false
    var sentSecio = false
    var receivedSecioPropose = false
    var receivedSecioExchange = false

    var sentSecioPropose = false
    var proposedByPeer: ProposeMessage? = null


    // 1st: /multistream/1.0.0
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        msg as ByteArray

        if (!receivedMultistream) {
            processMultistream(msg, ctx)
        } else if (!receivedSecio) {
            processSecio(msg, ctx)
        } else {
            processProtocolMesssage(msg, ctx)
        }


//                LOGGER.warn("0 bytes indicated by leading varint")
//                val proposedFromBytes = PrototypeUtil.parseProposeMessage(msg)
//                sendProposeMessage(ctx.channel()!!)
//                super.channelRead(ctx, msg)


    }

    private fun processMultistream(msg: ByteArray, ctx: ChannelHandlerContext) {
        val content = readVarintPrefixedMessage(msg)
        if (content == null) {
            LOGGER.error("Invalid message received, expected a multistream")
            return
        }
        val receivedStr = String(content)

        if (receivedStr == "/multistream/1.0.0\n") {
            receivedMultistream = true

            if (!sentMultistream) {
                sendAsync("multistream", "/multistream/1.0.0\n", ctx)
                sentMultistream = true

                sendAsync("secio", "/secio/1.0.0\n", ctx)
                sentSecio = true
            }
        } else {
            LOGGER.error("Incorrect message sequence: received '$content' but expected a multistream")
        }
    }

    private fun processSecio(msg: ByteArray, ctx: ChannelHandlerContext) {
        val content = readVarintPrefixedMessage(msg)
        if (content == null) {
            LOGGER.error("Invalid message received, expected secio")
            return
        }
        val receivedStr = String(content)

        if (receivedStr == "/secio/1.0.0\n") {
            receivedSecio = true
            if (!sentSecio) {
                sendAsync("secio", "/secio/1.0.0\n", ctx)
                sentSecio = true
            }
        } else {
            LOGGER.error("Incorrect message sequence: received '$content' but expected a secio")
        }
    }

    private fun processProtocolMesssage(msg: ByteArray, ctx: ChannelHandlerContext) {
        // Do protocol negotiation.
        if (!receivedSecioPropose) {
            receivedSecioPropose = true
            proposedByPeer = PrototypeUtil.parseProposeMessage(msg)
            sendSecioPropose(ctx)
        } else {
            LOGGER.error("UNHANDLED PROTOCOL MESSAGE")
        }

    }

    private fun sendSecioPropose(ctx: ChannelHandlerContext) {
        if (!sentSecioPropose) {
            sentSecioPropose = true
            sendAsync("secio-propose", proposeToByteArray(createOurProposal()), ctx)
        }
    }

    /**
     * Creates a serialized version of the given message that can be sent to a peer.
     * @param proposeMessage the message to be serialized.
     * @return the byte array equivalent.
     */
    private fun proposeToByteArray(proposeMessage: Spipe.Propose): ByteArray {
        val bytes = proposeMessage.toByteArray()
        val byteCount = bytes.size

        val byteCountAsBytes = ByteBuffer.allocate(java.lang.Integer.BYTES).putInt(byteCount).array()
        val newArray = ByteArray(byteCount + 4)
        System.arraycopy(byteCountAsBytes, 0, newArray, 0, 4)
        System.arraycopy(bytes, 0, newArray, 4, byteCount)

        return newArray
    }

    private fun readVarintPrefixedMessage(msg: ByteArray): ByteArray? {
        val numBytesJustReceived = msg.size
        val expectedByteCount = Varint.fromVarint(msg)
        if (expectedByteCount.toLong() > 0L) {

            val sizeOfVarintInBytes = Varint.sizeOf(expectedByteCount)
            val dataPortion = msg.drop(sizeOfVarintInBytes)


            if (numBytesJustReceived == sizeOfVarintInBytes + expectedByteCount.toInt()) {
                // Have the entire stream!
                return dataPortion.toByteArray()
            } else {
                LOGGER.error("Invalid varint-prefixed message received, discarding it")
            }
        }
        return null
    }

    private fun handleError(description: String) {
        // TODO: how to handle errors mid stream?
        throw RuntimeException(description)
    }


    private fun sendAsync(step: String, message: Any, ctx: ChannelHandlerContext): Unit {
        val cf = ctx.channel().write(message)
        cf.addListener { future1 ->
            if (future1.isSuccess) {
                LOGGER.debug("[$step]: message written")
            } else {
                LOGGER.error("[$step]: message write failed")
                throw future1.cause()
            }
        }
        ctx.flush()

    }

    fun createOurProposal(): Spipe.Propose {
        val privateKeyB64 =
            "CAASvQIwggE5AgEAAkEApenMpr5Uvx1hzLOkhT8o6vC6smF/RNOsIgZXUauqkGjlhhmuGC+b7GFEBzLfXlz0XsgXsX29Dl4Q80/AAbaDqwIDAQABAkAsXMj7Vs4LMfyKAwi9FifHNin9c2NX0G9ow6BKdqfLJ8rcutKjzNEIjBKtdaeaOeLDLjImQjsiVxFCLnRonpOtAiEA1TKgWeSmIimxjLChxT64G6kztk9bN8GeNV6ze7quZccCIQDHOPh4+GLQwkD92ueK1VuJ+S7jHmqyoz0iqB/3EWyi/QIgcOiC65igM2+JTE0vH1r7/go6DM8yK/EqbHFe9KQFQHkCICc2BFmy8agLA8WzJy2BLuIqJFtZakC8tlSy6I+1Yz91AiAr1lVJlso8Wprhqy3scXOhOJ/V9khJkDADSUGhqAnMKQ=="
        val publicKeyB64 =
            "CAASXjBcMA0GCSqGSIb3DQEBAQUAA0sAMEgCQQCl6cymvlS/HWHMs6SFPyjq8LqyYX9E06wiBldRq6qQaOWGGa4YL5vsYUQHMt9eXPReyBexfb0OXhDzT8ABtoOrAgMBAAE="

        val privBytes = Base64.getDecoder().decode(privateKeyB64.toByteArray())
        val privkey = unmarshalPrivateKey(privBytes)

        val pubBytes = Base64.getDecoder().decode(publicKeyB64.toByteArray())
        val pubkey = unmarshalPublicKey(pubBytes)

        val random = Base64.getDecoder().decode("QJLd77t1DO9ZVseiUyM9xQ==".toByteArray())

        val pm = ProposeMessage(unmarshalPublicKey(pubBytes), random)
            .withCiphers("AES-128", "AES-256")
            .withHashes("SHA256", "SHA512")
            .withExchanges("P-256", "P-384", "P-521")


        return pm.asPropose()
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        LOGGER.warn("----- exceptionCaught: ${cause.message}", cause)
        super.exceptionCaught(ctx, cause)
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(AccumulatorInboundHandler::class.java.name)!!
    }
}