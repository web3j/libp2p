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
package io.web3j.streammux

import java.lang.Exception
import java.time.LocalTime

/**
 * ResetException is returned when reading or writing on a reset stream.
 */
class ResetException : Exception("stream reset")

/**
 * Stream is a bidirectional io pipe within a connection.
 */
interface Stream {

    /**
     * Reset closes both ends of the stream. Use this to tell the remote
     * side to hang up and go away.
     */
    fun reset()

    fun setDeadline(time: LocalTime)

    fun setReadDeadline(time: LocalTime)

    fun setWriteDeadline(time: LocalTime)
}

/**
 * noopHandler does nothing. Resets streams as soon as they are opened.
 */
var noopHandler = { s: Stream -> s.reset() }

/**
 * Conn is a stream-multiplexing connection to a remote peer.
 */
interface Conn {

    /**
     * IsClosed returns whether a connection is fully closed, so it can
     * be garbage collected.
     */
    fun isClosed(): Boolean

    /**
     * OpenStream creates a new stream.
     */
    fun openStream(): Stream

    /**
     * AcceptStream accepts a stream opened by the other side.
     */
    fun acceptStream(): Stream
}

/**
 * Transport constructs stream-muxer compatible connections.
 */
interface Transport {

    /**
     * NewConn constructs a new connection
     */
    fun newConn(conn: Conn, isServer: Boolean): Conn
}
