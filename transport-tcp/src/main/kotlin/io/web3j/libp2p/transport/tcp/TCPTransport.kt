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
package io.web3j.libp2p.transport.tcp

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.ipfs.multiformats.multiaddr.Protocol
import io.web3j.libp2p.transport.Transport
import io.web3j.libp2p.transport.TransportConnection
import io.web3j.libp2p.transport.TransportConnectionListener
import io.web3j.libp2p.transport.tcp.provider.netty.NettyClientInfo
import io.web3j.libp2p.transport.tcp.provider.netty.NettyFacade
import io.web3j.libp2p.transport.tcp.provider.netty.NettyServerInfo
import io.web3j.libp2p.transport.tcp.util.TCPUtil
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress


/**
 * An implementation of the [Transport] interface using TCP.
 */
class TCPTransport : Transport {

    /**
     * Dials (using the transport) to the peer on the given address.
     * @param multiaddr the destination address peer to be dialed.
     * @return the established transport.
     */
    override fun dial(multiaddr: Multiaddr): TransportConnection {
        val isIpv4 = multiaddr.getProtocols().contains(Protocol.IP4)
        val isIpv6 = multiaddr.getProtocols().contains(Protocol.IP6)

        return when {
            isIpv4 || isIpv6 -> {
                val clientInfo: NettyClientInfo = NettyFacade.dial(TCPUtil.toSocketAddress(multiaddr))
                TCPTransportConnection.build(clientInfo, this)
            }
            else -> throw TCPTransportException(
                "Only IPv4 and IPv6 protocols are supported",
                TCPErrorCodes.UNSUPPORTED_PROTOCOL
            )
        }
    }

    /**
     * Checks whether the given address can be dialed with this transport implementation. <br />
     * @param multiaddr the address to be checked.
     * @return true if the address can be dialed; a result of <code>true</code> does not guarantee that the
     * <code>dial</code> call will succeed.
     */
    override fun canDial(multiaddr: Multiaddr): Boolean {
        return multiaddr.getProtocols().find { it == Protocol.TCP } != null
    }

    /**
     * Registers a listener on the transport instance at the given address.
     * @param multiaddr the multiaddr to listen on.
     * @param listener a callback interface that is notified when a new transport is received.
     * @return the multiaddr that the listener was started for.
     */
    override fun listen(multiaddr: Multiaddr, listener: TransportConnectionListener): Multiaddr {
        LOGGER.debug("Listening for connections on {}", multiaddr)
        val serverInfo: NettyServerInfo = NettyFacade.startListener(TCPUtil.toSocketAddress(multiaddr), listener, this)
        val socketAddress = serverInfo.serverChannel.localAddress()
        return TCPUtil.createMultiaddr(socketAddress as InetSocketAddress)!!
    }

    /**
     * @return the set of protocols handled by this transport instance.
     */
    override fun getProtocols(): Array<Protocol> = arrayOf(Protocol.TCP)

    companion object {
        val LOGGER = LoggerFactory.getLogger(TCPTransport::class.java)!!
    }
}
