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

import java.io.IOException

/**
 * An I/O interface that separates the transmission of messages from the
 * logic associated with a [MultiplexStream].
 */
interface MultiplexStreamIO {

    /**
     * Sends the given bytes across to the other peer.
     * @param bytes the bytes to send.
     * @throws IOException if the message could not be sent.
     */
    @Throws(IOException::class)
    fun send(bytes: ByteArray): Unit

    /**
     * Closes the stream because of an error that occurred.
     */
    fun closeInError(): Unit

    /**
     * Closes the stream in a normal fashion.
     */
    fun close(): Unit
}
