/*
 * Copyright 2019 BLK Technologies Limited. (web3labs.com)
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
package io.web3j.libp2p.net

import io.ipfs.multiformats.multiaddr.Multiaddr
import io.web3j.libp2p.crypto.PrivKey
import io.web3j.libp2p.crypto.PubKey
import kotlin.coroutines.CoroutineContext
import io.web3j.libp2p.protocol.ID as PROTOCOL_ID
import io.web3j.libp2p.peer.ID as PEER_ID
import io.web3j.streammux.Stream as streammux_Stream

/**
 * MessageSizeMax is a soft (recommended) maximum for network messages.
 * One can write more, as the interface is a stream. But it is useful
 * to bunch it up into multiple read/writes when the whole message is
 * a single, large serialized object.
 */
val MESSAGE_SIZE_MAX = 1 shl 22 // 4 MB

/**
 * Stream represents a bidirectional channel between two agents in
 * the IPFS network. "agent" is as granular as desired, potentially
 * being a "request -> reply" pair, or whole protocols.
 * Streams are backed by stream-muxer streams underneath the hood.
 */
interface Stream : streammux_Stream {

    fun protocol(): PROTOCOL_ID

    fun setProtocol(id: PROTOCOL_ID)

    /**
     * Stat returns metadata pertaining to this stream.
     */
    fun stat(): Stat

    /**
     * Conn returns the connection this stream is part of.
     */
    fun conn(): Conn
}

/**
 * Direction represents which peer in a stream initiated a connection.
 */
enum class Direction(val direction: Int) {

    /**
     * DirUnknown is the default direction.
     */
    DIR_UNKNOWN(0),

    /**
     * DirInbound is for when the remote peer initiated a connection.
     */
    DIR_INBOUND(1),

    /**
     * DirOutbound is for when the local peer initiated a connection.
     */
    DIR_OUTBOUND(2)
}

/**
 * Stat stores metadata pertaining to a given Stream/Conn
 */
data class Stat(val direction: Direction, val extra: Map<Any, Any>)

/**
 * StreamHandler is the type of function used to listen for
 * streams opened by the remote side.
 */
data class StreamHandler(val stream: Stream)

/**
 * ConnSecurity is the interface that one can mix into a connection interface to
 * give it the security methods.
 */
interface ConnSecurity {

    /**
     * LocalPeer returns our peer ID.
     */
    fun localPeer(): PEER_ID

    /**
     * LocalPrivateKey returns our private key.
     */
    fun localPrivateKey(): PrivKey

    /**
     * RemotePeer returns the peer ID of the remote peer.
     */
    fun remotePeer(): PEER_ID

    /**
     * RemotePublicKey returns the public key of the remote peer.
     */
    fun remotePublicKey(): PubKey
}

/**
 * ConnMultiaddrs is an interface mixin for connection types that provide multiaddr
 * addresses for the endpoints
 */
interface ConnMultiaddrs {

    /**
     * LocalMultiaddr returns the local Multiaddr associated
     * with this connection
     */
    fun localMultiaddr(): Multiaddr

    /**
     * RemoteMultiaddr returns the remote Multiaddr associated
     * with this connection
     */
    fun remoteMultiaddr(): Multiaddr
}

/**
 * Conn is a connection to a remote peer. It multiplexes streams.
 * Usually there is no need to use a Conn directly, but it may
 * be useful to get information about the peer on the other side:
 * stream.Conn().RemotePeer()
 */
interface Conn : ConnSecurity, ConnMultiaddrs {

    /**
     * NewStreams constructs a new Stream over this connection
     */
    fun newStreams(): Stream

    /**
     * GetStreams returns all open streams over this connection
     */
    fun getStreams(): Array<Stream>

    /**
     * Stat stores metadata pertaining to this Conn
     */
    fun stat(): Stat
}

/**
 * ConnHandler is the type of function used to listen for
 * connections opened by the remote side.
 */
data class ConnHandler(val conn: Conn)

/**
 * Network is the interface used to connect to the outside world.
 * It dials and listens for connections. it uses a Swarm to pool
 * connnections (see swarm pkg, and peerstream.Swarm). Connections
 * are encrypted with a TLS-like protocol.
 */
interface Network : Dialer {

    /**
     * SetStreamHandler sets the handler for new streams opened by the
     * remote side. This operation is thread safe
     */
    fun setStreamHandler(streamHandler: StreamHandler)

    /**
     * SetConnHandler sets the handler for new connections opened by the
     * remote side. This operation is threadsafe.
     */
    fun setConnHandler(connHandler: ConnHandler)

    /**
     * NewStream returns a new stream to given peer p.
     * If there is no connection to p, attempts to create one.
     */
    fun newStream(context: CoroutineContext, id: PEER_ID): Stream

    /**
     * Listen tells the network to start listening on given multiaddrs
     */
    fun listen(vararg multiaddr: Multiaddr)

    /**
     * ListenAddresses returns a list of addresses at which this network listens.
     */
    fun listenAddresses(): Array<Multiaddr>

    /**
     * InterfaceListenAddresses returns a list of addresses at which this network
     * listens. It expands "any interface" addresses (/ip4/0.0.0.0, /ip6/::) to
     * use the known local interfaces.
     */
    fun interfaceListenAddresses(multiaddrs: Array<Multiaddr>)

    /**
     * Process returns the network's Process
     */
    fun process(): Process
}

/**
 * Dialer represents a service that can dial out to peers
 * (this is usually just a Network, but other services may not need the whole
 * stack, and thus it becomes easier to mock)
 */
interface Dialer {

    /**
     * PeerStore returns the internal peerstore
     * This is useful to tell the dailer about a new address for a peer.
     * Or use one of the public keys found out over the network.
     */
    fun peerstore()

    /**
     * LocalPeer returns the local peer associated with this network
     */
    fun localPeer(): PEER_ID

    /**
     * DialPeer establishes a connection to a given peer
     */
    fun dialPeer(context: CoroutineContext, id: PEER_ID): Conn

    /**
     * ClosePeer closes the connection to a given peer
     */
    fun closePeer(id: PEER_ID)

    /**
     * Connectedness returns a state signaling connection capabilities
     */
    fun connectedness(id: PEER_ID): Connectedness

    /**
     * Peers returns the peers connected
     */
    fun peers(): Array<PEER_ID>

    /**
     * Conns returns the connections in this Network
     */
    fun conns(): Array<Conn>

    /**
     * ConnsToPeer returns the connections in this Network for given peer.
     */
    fun connsToPeer(peer: PEER_ID): Array<Conn>

    /**
     * Notify/StopNotify registers and unregisters a notifiee for signals
     */
    fun notify(notifiee: Notifiee)

    fun stopNotify(notifiee: Notifiee)
}

/**
 * Connectedness signals the capacity for a connection with a given node.
 * It is used to signal to services and other peers whether a node is reachable.
 */
enum class Connectedness(val value: Int) {

    /**
     * NotConnected means no connection to peer, and no extra information (default)
     */
    NOT_CONNECTED(0),

    /**
     * Connected means has an open, live connection to peer
     */
    CONNECTED(1),

    /**
     * CanConnect means recently connected to peer, terminated gracefully
     */
    CAN_CONNECT(2),

    /**
     * CannotConnect means recently attempted connecting but failed to connect.
     * (should signal "made effort, failed")
     */
    CANNOT_CONNECT(3)
}

/**
 * Notifiee is an interface for an object wishing to receive
 * notifications from a Network
 */
interface Notifiee {

    /**
     * Listen called when network starts listening on addr
     */
    fun listen(network: Network, multiaddr: Multiaddr)

    /**
     * ListenClose called when network stops listening on an addr
     */
    fun listenClose(network: Network, multiaddr: Multiaddr)

    /**
     * Connected called when a connection opened
     */
    fun connected(network: Network, conn: Conn)

    /**
     * Disconnected called when a connection closed
     */
    fun disconnected(network: Network, conn: Conn)

    /**
     * OpenedStream called when a stream opened
     */
    fun openedStream(network: Network, stream: Stream)

    /**
     * ClosedStream called when a stream closed
     */
    fun closedStream(network: Network, stream: Stream)

    // TODO
    /**
     * PeerConnected called when a peer connected
     * fun peerConnected(network: Network, id: PEER_ID)
     */

    /**
     * PeerDisconnected called when a peer disconnected
     * fun peerDisconnected(network: Network, id: PEER_ID)
     */
}
