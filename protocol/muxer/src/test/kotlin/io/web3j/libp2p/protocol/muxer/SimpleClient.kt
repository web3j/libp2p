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
package io.web3j.libp2p.protocol.muxer

import io.web3j.libp2p.transport.tcp.TCPTransport
import io.web3j.libp2p.transport.tcp.TCPTransportConnection
import io.web3j.libp2p.transport.tcp.util.TCPUtil
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

class SimpleClient {

    @Test
    @Disabled("WIP")
    fun start() {
        val listenerAddr = TCPUtil.createLoopbackMultiaddr(10333)

        println("Creating connection ...")
        val connection = TCPTransport().dial(listenerAddr) as TCPTransportConnection

        println("Connection available: $connection")

        Thread.sleep(1000)
//        val x = "ipfs/Qma3GsJmB47xYuyahPZPSadh1avvxfyYQwk8R3UnFrQ6aP"
        val proto = "/multistream/1.0.0"
//

//        ctx.channel().write("/$x/multistream/1.0.0\n").sync()

        connection.channel.write("$proto\n").sync()

        Thread.sleep(60000)
    }
}
