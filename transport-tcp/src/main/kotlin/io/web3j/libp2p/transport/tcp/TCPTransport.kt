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

/**
 * An implementation of the [Transport] interface using TCP.
 */
class TCPTransport : Transport {

    /**
     * Dials (using the transport) to the peer on the given address.
     * @param multiaddr the destination address peer to be dialed.
     * @param options the dial options.
     * @return the established transport.
     */
    override fun dial(multiaddr: Multiaddr, options: Transport.TransportDialOptions?): TransportConnection {

        val isIpv4 = multiaddr.getProtocols().contains(Protocol.IP4)
        val isIpv6 = multiaddr.getProtocols().contains(Protocol.IP6)

        return if (isIpv4) {
            dialIpv4(multiaddr, options)
        } else if (isIpv6) {
            return dialIpv6(multiaddr, options)
        } else {
            throw TCPTransportException(
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
     * Registers a listener on the transport instance.
     * @param listener a callback interface that is notified when a new transport is received.
     */
    override fun registerListener(listener: TransportConnectionListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    /**
     * @return the set of protocols handled by this transport instance.
     */
    override fun getProtocols(): Array<Protocol> = arrayOf(Protocol.TCP)

    /**
     * Helper function that dials the given IPv6 address.
     * @param multiaddr the address to be dialed containing an IPv6 portion.
     * @param options the dial options.
     * @return the established transport.
     */
    private fun dialIpv6(multiaddr: Multiaddr, options: Transport.TransportDialOptions?): TransportConnection {
        // TODO: implement!
        throw TCPTransportException(TCPErrorCodes.UNIMPLEMENTED)
    }

    /**
     * Helper function that dials the given IPv6 address.
     * @param multiaddr the address to be dialed containing an IPv6 portion.
     * @param options the dial options.
     * @return the established transport.
     */
    private fun dialIpv4(multiaddr: Multiaddr, options: Transport.TransportDialOptions?): TransportConnection {
        val host = multiaddr.valueForProtocol(Protocol.IP4.code)

        TODO("COntinue here")
//        var d manet.Dialer
//        return d.DialContext(
//            ctx, raddr
//                    TODO ("NOT IMPLEMENTED YET")
    }

}
