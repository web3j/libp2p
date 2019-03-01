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

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

/**
 * This is more of a multi-layered test rather than an integration test.
 */
class SimpleIntegrationTest {

    @Test
    @Disabled
    fun testEchoMessagesAcrossTwoLocalNodes() {

        // Create node1 on: /ip4/127.0.0.1/tcp/0
        // Create node2 on: /ip4/127.0.0.1/tcp/0
        // Create a StreamHandler that will listen on: "/echo/1.0.0" and write back onto the stream: "ACK: " + <input_message>
        // node2.setStreamHandler("/echo/1.0.0", stream_handler)
        // Register a connection listener on node1
        // Trigger node2 to connect to node1
        // In the listener we want to get each message and return it with the "ACK: " + input_message mentioned above

        TODO("implement this!")
    }
}
