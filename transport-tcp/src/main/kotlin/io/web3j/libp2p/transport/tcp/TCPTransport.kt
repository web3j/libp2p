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

class TCPTransport : Transport {
    override fun dial(multiaddr: Multiaddr, options: Transport.TransportDialOptions?): TransportConnection {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun canDial(multiaddr: Multiaddr): Boolean {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun registerListener(listener: TransportConnectionListener) {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    override fun getProtocols(): Array<Protocol> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

}
