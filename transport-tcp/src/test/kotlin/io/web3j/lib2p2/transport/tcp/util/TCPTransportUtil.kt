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

import io.web3j.libp2p.transport.TransportConnection
import io.web3j.libp2p.transport.TransportConnectionListener
import io.web3j.libp2p.transport.tcp.TCPTransport
import io.web3j.libp2p.transport.tcp.TCPTransportConnection
import io.web3j.libp2p.transport.tcp.util.TCPUtil
import org.slf4j.LoggerFactory
import java.net.InetAddress
import java.util.concurrent.Semaphore
import java.util.concurrent.TimeUnit

/**
 * Contains a set of utility functions that can be used in scaffolding tests.
 */
object TCPTransportUtil {

    private val LOGGER = LoggerFactory.getLogger(TCPTransportUtil::class.java)

    /**
     * Starts a TCP transport listener and waits for a connection.
     * @param serverPort the port to listen on.
     * @param serverAddress the server address to listen on; defaults to localhost.
     * @param transport the transport layer to be used to listen for a connection.
     * @param waitTimeoutInSeconds the amount of time (in seconds) to wait for a connection before timing out.
     * @return the connection that was established; null if no connection was established.
     */
    fun startBlockingListener(
        serverPort: Int,
        serverAddress: InetAddress = InetAddress.getLoopbackAddress(),
        transport: TCPTransport = TCPTransport(),
        waitTimeoutInSeconds: Long = 30
    ): TCPTransportConnection? {
        val serverMultiaddr = TCPUtil.createMultiaddr(serverAddress, serverPort)

        var txConnectionOpt: TCPTransportConnection? = null
        val semaphore = Semaphore(1)
        semaphore.acquire()
        transport.listen(serverMultiaddr, object : TransportConnectionListener {
            override fun onNewConnection(connection: TransportConnection) {
                LOGGER.info("New connection established, connection={}", connection)
                txConnectionOpt = connection as TCPTransportConnection
                semaphore.release()
            }
        })


        // Wait for a connection or timeout.
        semaphore.tryAcquire(waitTimeoutInSeconds, TimeUnit.SECONDS)
        return txConnectionOpt
    }


    /**
     * Starts a listener on the given server address and port number.
     * @param serverPort the port to listen on.
     * @param serverAddress the server address to listen on; defaults to localhost.
     * @param transport the transport layer to be used to listen for a connection.
     * @return a connection holder that can be queried to determine when a connection has been established.
     */
    fun startListener(
        serverPort: Int = 0,
        serverAddress: InetAddress = InetAddress.getLoopbackAddress(),
        transport: TCPTransport = TCPTransport()
    ): TCPTransportConnectionHolder {
        val serverMultiaddr = TCPUtil.createMultiaddr(serverAddress, serverPort)
        return TCPTransportConnectionHolder().also {
            val listeningOn = transport.listen(serverMultiaddr, it)
            it.listenAddress = listeningOn
        }
    }
}