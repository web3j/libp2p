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

import java.util.concurrent.atomic.AtomicBoolean

/**
 * Thread-safe status holder for a [MultiplexStream]
 */
class MultiplexStreamStatus {

    /**
     * Indicates if the stream is closed locally.
     */
    private var closedLocally = AtomicBoolean(false)

    /**
     * Indicates if the stream is closed remotely.
     */
    private var closedRemotely = AtomicBoolean(false)

    /**
     * Indicates if the stream is reset (both ends closed).
     */
    private var isReset = AtomicBoolean(false)

    /**
     * Marks the stream as locally closed, so writing cannot be performed.
     */
    fun markClosedLocally(): Unit {
        closedLocally.set(true)
    }

    /**
     * Marks the stream as closed on the other end, so data will never be read.
     */
    fun markClosedRemotely(): Unit {
        closedRemotely.set(true)
    }

    /**
     * @return true if the stream can be written to.
     */
    fun canWrite(): Boolean {
        return !isReset.get() && !closedLocally.get()
    }

    /**
     * @return true if the stream can be read from.
     */
    fun canRead(): Boolean {
        // We will allow dirty reads fow now.
        return !isReset.get() && !closedRemotely.get()
    }

    /**
     * @return true if the stream is closed locally.
     */
    fun isClosedLocally(): Boolean = closedLocally.get()

    /**
     * Marks the stream as reset.
     */
    fun markAsReset(): Unit = isReset.set(true)

}