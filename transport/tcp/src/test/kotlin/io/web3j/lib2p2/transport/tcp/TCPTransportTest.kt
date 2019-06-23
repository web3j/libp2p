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
package io.web3j.lib2p2.transport.tcp

import io.web3j.lib2p2.transport.tcp.util.TCPTransportUtil
import io.web3j.libp2p.transport.ConnectionStatus
import io.web3j.libp2p.transport.tcp.TCPTransport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

/**
 * This is more of a multi-layered test rather than an integration test.
 */
class TCPTransportTest {

    /**
     * Tests spawning a  (server), and connecting another peer to that
     */
    @Test
    @Disabled("WIP")
    fun testSimplePeerConnectivity() {
        // Start the listener.
        val listenerConnectionHolder = TCPTransportUtil.startListener()

        // Establish a client connection to the listener.
        val dialerConnection = TCPTransport().dial(listenerConnectionHolder.listenAddress)

        // A connection should be already available.
        val listenersConnectionToDialer = listenerConnectionHolder.waitForConnection(5)
        assertNotNull(listenersConnectionToDialer, "No connection was made")
        listenersConnectionToDialer!!

        // Disconnect and wait a bit for the change to propagate.
        dialerConnection.disconnect()
        Thread.sleep(200)

        // This should be terminated now.
        assertEquals(
            ConnectionStatus.CLOSED,
            listenersConnectionToDialer.getStatus(),
            "Connection should be terminated"
        )
        assertEquals(ConnectionStatus.CLOSED, dialerConnection.getStatus(), "Client connection should be closed")
    }

    /*

    https://www.slideshare.net/richardhightower/netty-notes-part-3-channel-pipeline-and-eventloops

    Channel = socket
    ChannelHandler = process / transform messages
                    registered with a ChannelInitializer
                    operate on events by processing them and then passing them to the next handler in the chain
    ChannelPipeline = forms chains of ChannelHandlers. Handlers intercept inbound and outbound events through channel


    Channel lifecycle:
     - active = connected to remote peer
     - inactive = disconnected from remote peer
     - unregistered = created
     - regsitered = channel associated with event loop

    Lifecycle events:
        - handlerAdded(): added to ChanneelPipeline
        - handlerRemoved(): removed from ChanelPipeline
        - exceptionCaught(): error during ChannelPipeline processing

    ChannelInboundHandler lifecycle methods for Channel
        - channelRegsitered(): channel married to an event loop
        - channelUnregistered(): channel divorced from event loop
        - channelActive(): connected
        - channelInactive(): disconnected
        - channelReadComplete(): read operation completed
        - channelRead(): data is read on the channel()
        - userEventTriggered(): someone passed a POJO to the event loop

    ChannelOutboundHandler
        -
     */

    // Create node1 on: /ip4/127.0.0.1/tcp/0
    // Create node2 on: /ip4/127.0.0.1/tcp/0
    // Create a StreamHandler that will listen on: "/echo/1.0.0" and write back onto the stream: "ACK: " + <input_message>
    // node2.setStreamHandler("/echo/1.0.0", stream_handler)
    // Register a connection listener on node1
    // Trigger node2 to connect to node1
    // In the listener we want to get each message and return it with the "ACK: " + input_message mentioned above

    companion object {
        val LOGGER = LoggerFactory.getLogger(TCPTransportTest::class.java!!)
    }
}
