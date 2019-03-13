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
package io.web3j.lib2p2.transport.tcp.util

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.web3j.libp2p.transport.TransportConnection
import io.web3j.libp2p.transport.TransportConnectionListener
import io.web3j.libp2p.transport.tcp.TCPTransportConnection
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.locks.ReentrantLock

/**
 * Implements [TransportConnectionListener] to hold onto an opened connection and expose it to requestors.
 */
class TCPTransportConnectionHolder : TransportConnectionListener {

    /**
     * The address that is being listened to.
     */
    lateinit var listenAddress: Multiaddr

    /**
     * The connection that has been received.
     */
    private var connection: TCPTransportConnection? = null

    /**
     * The lock for using the [conditionEstablished].
     */
    private val lock = ReentrantLock()

    /**
     * The condition flag that indicates that the connection has been established.
     */
    private val conditionEstablished = lock.newCondition()

    /**
     * Fired when a new transport is received on the transport layer.
     * @param connection the transport that was established.
     */
    override fun onNewConnection(connection: TransportConnection) {
        LOGGER.info("New connection established, connection={}", connection)
        this.connection = connection as TCPTransportConnection

        lock.lock()
        conditionEstablished.signal()
        lock.unlock()
    }

    /**
     * Blocks the caller until a connection is available or the timeout expires.
     * @param waitPeriodInSeconds the maximum number of seconds to wait for a connection.
     * @return the connection, or null if one was not established.
     */
    fun waitForConnection(waitPeriodInSeconds: Long): TCPTransportConnection? {
        if (connection != null) {
            return connection
        }

        lock.lock()
        conditionEstablished.await(waitPeriodInSeconds, TimeUnit.SECONDS)
        lock.unlock()
        return this.connection
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(TCPTransportConnectionHolder::class.java)
    }

}