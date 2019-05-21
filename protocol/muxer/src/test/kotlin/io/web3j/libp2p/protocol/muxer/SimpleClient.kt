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
import org.junit.jupiter.api.Test
import org.slf4j.LoggerFactory

class SimpleClient {

    @Test
//    @Disabled("WIP")
    fun start() {
        val listenerAddr = TCPUtil.createLoopbackMultiaddr(10333)
        LOGGER.info("Setting up the connection to $listenerAddr ...")

        val connection = TCPTransport().dial(listenerAddr) as TCPTransportConnection

        println("Connection available: $connection")
        println("-- SENDING OUR PROTOCOL FIRST")

//        sendAsync("/multistream/1.0.0\n", connection)
//        sendAsync("/secio/1.0.0\n", connection)

        println("Waiting some more")
        Thread.sleep(60000)
        println("Done")
    }

    fun sendSync(message: String, connection: TCPTransportConnection): String {
        val cf = connection.channel.write(message).sync()

//        cf.addListener { future1 ->
//            if (future1.isSuccess) {
//                println("WRITE COMPLETED")
//            } else {
//                println("WRITE FAILED: " + future1.cause())
//                throw future1.cause()
//            }
//        }.
//
//        connection.channel.flush()
        return "TODO"
    }

    fun sendAsync(message: String, connection: TCPTransportConnection) {
        val cf = connection.channel.write(message)
        cf.addListener { future1 ->
            if (future1.isSuccess) {
                println("WRITE COMPLETED")
            } else {
                println("WRITE FAILED: " + future1.cause())
                throw future1.cause()
            }
        }

        connection.channel.flush()
    }

    fun sendAsync(bytes: ByteArray, connection: TCPTransportConnection) {
        val cf = connection.channel.write(bytes)
        cf.addListener { future1 ->
            if (future1.isSuccess) {
                println("WRITE COMPLETED")
            } else {
                println("WRITE FAILED: " + future1.cause())
                throw future1.cause()
            }
        }

        connection.channel.flush()
    }

    companion object {
        private val LOGGER = LoggerFactory.getLogger(SimpleClient.javaClass)!!
    }
}
