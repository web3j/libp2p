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
package io.web3j.libp2p.stream.mplex

import io.web3j.libp2p.stream.mplex.impl.MultiplexStreamStatus
import io.web3j.libp2p.stream.mplex.impl.MultiplexUtil
import io.web3j.libp2p.stream.mplex.model.MultiplexData
import io.web3j.streammux.MuxedStream
import org.slf4j.LoggerFactory
import java.io.IOException

/**
 * Represents a single stream in a multiplexed session.
 *
 * @param name the name of the stream.
 * @param id the ID of the stream.
 * @param initiator whether this peer is the stream initiator.
 * @param io the communications channel to be used by the stream.
 * @param ownerSession the session that owns this stream.
 *
 * [@see https://github.com/libp2p/go-mplex/blob/master/stream.go]
 */
class MultiplexStream(
    val id: ULong,
    val name: String,
    val initiator: Boolean,
    private val io: MultiplexStreamIO,
    private val ownerSession: MultiplexSession
) :
    MuxedStream {

    /**
     * The stream status.
     */
    private val status = MultiplexStreamStatus()

    /**
     * Sends data to the other peer indicating that a new stream is to be established.
     * @throws IOException if the message could not be sent.
     */
    @Throws(IOException::class)
    fun initiateNewStream() {
        write(MultiplexUtil.FLAG_NEW_STREAM, name.toByteArray())
    }

    /**
     * Fired when data is available for processing.
     * @param data the data available.
     */
    fun onDataReceived(data: ByteArray) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * Closes the other side of this stream.
     * @return true if both sides of the stream are now closed.
     */
    fun markOtherSideAsClosed(): Boolean {
        // TODO: Wait for the receive loop to finish?
        status.markClosedRemotely()
        return status.isClosedLocally()
    }

    /**
     * Writes the given bytes to the stream.
     * @param byteArray the byte array to be written out.
     * @return the number of bytes written.
     */
    override fun write(byteArray: ByteArray): Long {
        write(if (initiator) MultiplexUtil.FLAG_MESSAGE_INITIATOR else MultiplexUtil.FLAG_MESSAGE_RECEIVER, byteArray)
        return byteArray.size.toLong()
    }

    /**
     * Resets this stream so that no data ought to be subsequently sent nor received as a result of an error.
     */
    override fun reset() {
        status.markAsReset()
        write(if (initiator) MultiplexUtil.FLAG_RESET_INITIATOR else MultiplexUtil.FLAG_RESET_RECEIVER)
        io.closeInError()
    }

    /**
     * Closes the stream completely.
     * @return true if the stream was successfully closed.
     */
    override fun close(): Boolean {
        ownerSession.closeBothEnds(this)
        return true
    }

    /**
     * @return the bytes from the stream..
     */
    override fun read(): ByteArray {
        TODO("NOT IMPLEMENTED YET")
    }

    /**
     * Marks this stream has having been reset.
     */
    fun markAsReset() {
        status.markAsReset()
    }

    /**
     * @return true if the stream is in a state that can receive messages.
     */
    fun canReceive(): Boolean = status.canRead()

    /**
     * Writes the given bytes and flag as a message to the stream.
     * @param flag the flag to be written.
     * @param data the data to be written.
     * @return true if the
     */
    private fun write(flag: Byte, data: ByteArray = ByteArray(0)): Boolean {
        return if (status.canWrite()) {
            io.send(MultiplexUtil.composeProtocolData(id, flag, data))
            true
        } else {
            LOGGER.warn("Cannot write to a closed/reset stream")
            false
        }
    }

    /**
     * Checks if the given message has a flag that respects the `initiator` role.
     * @param message the message to be checked.
     * @return true if this message has the correct flag direction and ought to be accepted.
     */
    fun isCorrectMessageFlagDirection(message: MultiplexData): Boolean = initiator.xor(message.flags.initiator)

    override fun toString(): String {
        return "Stream[id=$id,name=$name,initiator=$initiator]"
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MultiplexStream.javaClass)!!
    }
}
