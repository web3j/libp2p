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

import io.web3j.libp2p.stream.mplex.model.HeaderFlag
import io.web3j.libp2p.stream.mplex.model.MultiplexData
import org.slf4j.LoggerFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.locks.ReentrantLock

/**
 * Manages the [MultiplexStream]s that are created on a connection, and manages
 * the set of all streams (channels) as a session.
 *
 * @param startingSessionId the session ID to start allocating from.
 * @param streamFactory a factory for creating [MultiplexStream] instances.
 * @param operationTimeoutMillis the timeout (in milliseconds) for any operation to complete.
 * @param receiveTimeoutMillis the time to wait before resetting a connection if data is not received.
 *
 * @see [https://github.com/libp2p/specs/tree/master/mplex]
 * @see [https://github.com/libp2p/go-mplex/blob/master/multiplex.go]
 */
class MultiplexSession(
    startingSessionId: ULong,
    private val streamFactory: MultiplexStreamFactory,
    private val operationTimeoutMillis: Long = 30000,
    private val receiveTimeoutMillis: Long = 5000
) {

    /**
     * The next session ID to be allocated.
     */
    private var sessionIdGenerator = AtomicLong(startingSessionId.toLong())

    /**
     * Maps stream ID to the stream instance.
     */
    private val streamsById = ConcurrentHashMap<ULong, MultiplexStream>()

    /**
     * A lock to control write access to our channels map.
     */
    private val streamsByIdWriteLock = ReentrantLock()

    /**
     * Creates a new stream with an optional name.
     * @param name an optional stream name.
     * @return the new stream.
     */
    fun createNewStream(name: String = ""): MultiplexStream {
        val streamId = sessionIdGenerator.getAndIncrement().toULong()
        val stream = streamFactory.create(streamId, true, name)
        stream.initiateNewStream()

        // Maintain this new stream in our collection.
        streamsByIdWriteLock.tryLock(operationTimeoutMillis, TimeUnit.MILLISECONDS)
        streamsById[streamId] = stream
        streamsByIdWriteLock.unlock()

        return stream
    }

    // TODO: onProtocolMessageReceived will be called by some listener that calls: MultiplexUtil.readProtocolData()

    /**
     * Fired when a message is received on a stream.
     * @param message the message received.
     */
    fun onProtocolMessageReceived(message: MultiplexData) {
        LOGGER.debug("New message received: $message")
        when (message.flags) {
            HeaderFlag.NewStream -> onNewStreamEvent(message)
            HeaderFlag.MessageReceiver, HeaderFlag.MessageInitiator -> onMessageEvent(message)
            HeaderFlag.ResetInitiator, HeaderFlag.ResetReceiver -> onResetEvent(message)
            HeaderFlag.CloseInitiator, HeaderFlag.CloseReceiver -> onCloseEvent(message)
        }
    }

    /**
     * Fired when a message with data is received on a stream.
     * @param message the message received.
     */
    private fun onMessageEvent(message: MultiplexData) {
        val stream = this.streamsById[message.streamId]
        if (stream == null) {
            LOGGER.warn("Received a DATA message for a non-existent stream with id=${message.streamId}")
            resetUnmanagedStreamForMessage(message)
            return
        }

        // If we initiate the stream we should receive a CloseReceiver, and vice-versa
        if (stream.isCorrectMessageFlagDirection(message)) {
            // Now check that the stream is in the right state.
            if (stream.canReceive()) {
                stream.onDataReceived(message.data)
            } else {
                LOGGER.warn("Stream is not in a state to receive data: $stream")
                resetStreamInError(stream)
            }
        } else {
            LOGGER.warn("Received a DATA message with an incorrect direction: $stream | $message")
            resetStreamInError(stream)
        }
    }

    /**
     * Fired when a message indicative of a new stream has been received.
     * @param message the message received.
     */
    private fun onNewStreamEvent(message: MultiplexData) {
        val existingStream = this.streamsById[message.streamId]
        if (existingStream != null) {
            LOGGER.warn(
                "Received a NEW_STREAM message for an existing stream " +
                        "with id=${message.streamId}, closing session"
            )
            resetStreamInError(existingStream)
            return
        }

        // Maintain this new stream in our collection.
        val newStream = streamFactory.create(message.streamId, false, message.getStreamName())
        streamsByIdWriteLock.tryLock(operationTimeoutMillis, TimeUnit.MILLISECONDS)
        streamsById[message.streamId] = newStream
        streamsByIdWriteLock.unlock()
    }

    /**
     * Fired when a `close` message is received.
     * @param message the close message.
     */
    private fun onCloseEvent(message: MultiplexData) {
        val stream = this.streamsById[message.streamId]
        if (stream == null) {
            LOGGER.warn("Received a CLOSE message for a non-existent stream with id=${message.streamId}")
            return
        }

        // If we initiate the stream we should receive a CloseReceiver, and vice-versa
        if (stream.isCorrectMessageFlagDirection(message)) {
            if (stream.markOtherSideAsClosed()) {
                LOGGER.info("Both sides of the stream have been closed")
                markForRemoval(stream)
            }
        } else {
            LOGGER.warn("Received a CLOSE message with an incorrect direction: $stream | $message")
            closeStreamInError(stream)
        }
    }

    /**
     * Fired when a `reset` message is received.
     * @param message the reset message.
     */
    private fun onResetEvent(message: MultiplexData) {
        val stream = this.streamsById[message.streamId]
        if (stream == null) {
            LOGGER.warn("Received a RESET message for a non-existent stream with id=${message.streamId}")
            resetUnmanagedStreamForMessage(message)
            return
        }

        // If we initiate the stream we should receive a ResetReceiver, and vice-versa
        if (stream.isCorrectMessageFlagDirection(message)) {
            LOGGER.info("Resetting the stream: $stream")
        } else {
            LOGGER.warn("Received a RESET message with an incorrect direction: $stream | $message")
        }

        stream.markAsReset()
        markForRemoval(stream)
    }

    /**
     * Called when a stream is to be reset (closed) as a result of an error.
     */
    private fun resetStreamInError(stream: MultiplexStream) {
        LOGGER.debug("Resetting the stream in error: $stream")
        stream.reset()
        markForRemoval(stream)
    }

    /**
     * Called when an unmanaged stream is to be reset (closed) as a result of an error.
     * @param message the message whose stream is to be reset.
     */
    private fun resetUnmanagedStreamForMessage(message: MultiplexData) {
        LOGGER.debug("Resetting the (unmanaged) stream for message: $message")
        streamFactory.create(message.streamId, false).reset()
    }

    /**
     * Called when a stream is to be closed as a result of an error.
     */
    private fun closeStreamInError(stream: MultiplexStream) {
        LOGGER.debug("Closing the stream in error: $stream")
        markForRemoval(stream)
        stream.reset()
    }

    /**
     * Closes the stream completely.
     * @param stream the stream to be closed.
     */
    fun closeBothEnds(stream: MultiplexStream) {
        // Because the CLOSE flag only closes one side, to close both sides we have to use the RESET flag.
        stream.reset()
    }

    /**
     * TODO: implement!
     * Marks the given stream for removal from our managed set of streams.
     * <br />This allows us to handle the case where data is received for a stream
     * after we have requested a reset, and discard the data.
     */
    private fun markForRemoval(stream: MultiplexStream) {
        // TODO: we should put streams into a TIMED_WAIT where we don't discard
        // them right away, but after a period of time.
        this.streamsById.remove(stream.id)
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(MultiplexSession.javaClass)!!
    }
}
