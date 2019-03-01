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

import io.ipfs.multiformats.multiaddr.Protocol
import io.web3j.libp2p.transport.tcp.util.TCPUtil
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.net.Inet4Address
import java.net.Inet6Address

/**
 * A test suit for [TCPUtil].
 */
class TCPUtilTest {

    /**
     * Tests the conversion of an IPv4 address and port to a Multiaddr structure.
     */
    @Test
    fun ip4AddressToMultiaddrTest(): Unit {
        val ip4Address = Inet4Address.getByAddress(byteArrayOf(127.toByte(), 0.toByte(), 0.toByte(), 1.toByte()))
        val port = 8080
        val addr = TCPUtil.createMultiaddr(ip4Address, port)
        assertEquals("127.0.0.1", addr.valueForProtocol(Protocol.IP4.code), "Incorrect IP")
        assertEquals("8080", addr.valueForProtocol(Protocol.TCP.code), "Incorrect port")
    }

    /**
     * Tests the conversion of an IPv6 address and port to a Multiaddr structure.
     */
    @Test
    fun ip6AddressToMultiaddrTest(): Unit {
        val ip6Address = Inet6Address.getByAddress(
            byteArrayOf(
                32.toByte(),
                1.toByte(),
                219.toByte(),
                8.toByte(),
                10.toByte(),
                11.toByte(),
                18.toByte(),
                240.toByte(),
                0.toByte(),
                0.toByte(),
                0.toByte(),
                0.toByte(),
                0.toByte(),
                0.toByte(),
                0.toByte(),
                1.toByte()
            )
        )

        val port = 8080
        val addr = TCPUtil.createMultiaddr(ip6Address, port)
        assertEquals("2001:db08:a0b:12f0:0:0:0:1", addr.valueForProtocol(Protocol.IP6.code), "Incorrect IP")
        assertEquals("8080", addr.valueForProtocol(Protocol.TCP.code), "Incorrect port")
    }

}