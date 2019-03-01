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

import java.time.Duration

/**
 * A muxed stream represents a line of communication over a RawConnection, using a specific protocol.
 */
interface MuxedStream {

    /**
     * Reset closes both ends of the stream. Use this to tell the remote
     * side to hang up and go away.
     */
    fun reset(): Unit

    /**
     * Sets the deadline for the muxed stream.
     * @param ttl the deadline/TTL.
     */
    fun setDeadline(ttl: Duration)

    /**
     * @return the bytes from the stream..
     */
    fun read(): ByteArray

    /**
     * Writes the given bytes to the stream.
     * @param byteArray the byte array to be written out.
     * @return the number of bytes written.
     */
    fun write(byteArray: ByteArray): Long

    /**
     * Closes the stream.
     * @return true if the stream was successfully closed.
     */
    fun close(): Boolean
}
