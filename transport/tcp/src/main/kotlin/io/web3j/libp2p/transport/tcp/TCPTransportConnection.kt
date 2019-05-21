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
import io.netty.channel.Channel
import io.netty.channel.socket.SocketChannel
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import io.web3j.libp2p.peer.PeerID
import io.web3j.libp2p.peer.PeerInfo
import io.web3j.libp2p.transport.ConnectionStatus
import io.web3j.libp2p.transport.Transport
import io.web3j.libp2p.transport.TransportConnection
import io.web3j.libp2p.transport.tcp.provider.netty.NettyClientInfo
import io.web3j.libp2p.transport.tcp.util.TCPUtil
import org.slf4j.LoggerFactory
import java.net.InetSocketAddress

/**
 * A [TransportConnection] implementation geared to work with the TCP transport library.
 * @param channel the channel that underpins this connection.
 * @param transport the [Transport] instance that created this connection.
 */
class TCPTransportConnection(val channel: Channel, private val transport: TCPTransport) : TransportConnection {

    /**
     * The local address of this peer's connection.
     */
    private val localAddr: Multiaddr? = TCPUtil.createMultiaddr(channel.localAddress() as InetSocketAddress)

    /**f
     * The remote address of the other end.
     */
    private val remoteAddr: Multiaddr? = TCPUtil.createMultiaddr(channel.remoteAddress() as InetSocketAddress)

    /**
     * @return the Transport instance that this connection belongs to.
     */
    override fun getTransport(): Transport = transport

    override fun getPeerInfo(): PeerInfo {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * @return this user's peer ID.
     */
    override fun getLocalPeerID(): PeerID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getLocalPrivateKey(): PrivKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRemotePublicKey(): PubKey {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getRemotePeerID(): PeerID {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * @return the local multiaddr associated with this connection.
     */
    override fun getLocalMultiaddr(): Multiaddr {
        return localAddr ?: throw TCPTransportException(
            "No channel is available",
            TCPErrorCodes.OBJECT_NOT_AVAILABLE
        )
    }

    /**
     * @return the remote peer's multiaddr associated with this connection.
     */
    override fun getRemoteMultiaddr(): Multiaddr {
        return remoteAddr ?: throw TCPTransportException(
            "No channel is available",
            TCPErrorCodes.OBJECT_NOT_AVAILABLE
        )
    }

    /**
     * Disconnects this connection.
     */
    override fun disconnect() {
        if (this.channel.isOpen) {
            LOGGER.debug("Disconnecting ...")
            this.channel.disconnect().sync()
        }
    }

    /**
     * @return the status of the connection.
     */
    override fun getStatus(): ConnectionStatus {
        return if (this.channel.isOpen) {
            ConnectionStatus.OPEN
        } else {
            ConnectionStatus.CLOSED
        }
    }

    companion object {

        val LOGGER = LoggerFactory.getLogger(TCPTransportConnection::class.java)!!

        /**
         * Constructs an instance of a [TCPTransportConnection] with the given socket.
         * @param socketChannel the channel that underpins the connection.
         * @param tcpTransport the TCP transport instance that this connection was created with.
         * @return the created [Transport] connection.
         */
        fun build(socketChannel: SocketChannel, tcpTransport: TCPTransport): TCPTransportConnection {
            return TCPTransportConnection(socketChannel, tcpTransport)
        }

        /**
         * Constructs an instance of a [TCPTransportConnection] with the given socket.
         * @param socketChannel the channel that underpins the connection.
         * @param tcpTransport the TCP transport instance that this connection was created with.
         * @return the created [Transport] connection.
         */
        fun build(clientInfo: NettyClientInfo, tcpTransport: TCPTransport): TCPTransportConnection {
            return TCPTransportConnection(clientInfo.clientChannel, tcpTransport)
        }
    }
}
