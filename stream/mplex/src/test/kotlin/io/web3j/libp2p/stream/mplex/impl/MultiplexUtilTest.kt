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
package io.web3j.libp2p.stream.mplex.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

/**
 * A suite of tests for [MultiplexUtil].
 */
class MultiplexUtilTest {

    @Test
    fun simpleReflexiveNewStreamWithNameTest() {
        runFullCircleAndAssert(23408923848089283.toLong(), MultiplexUtil.FLAG_CLOSE_INITIATOR, ByteArray(0))
        runFullCircleAndAssert(
            73.toLong(),
            MultiplexUtil.FLAG_NEW_STREAM,
            "this is A new Stream's NAME".toByteArray()
        )
        runFullCircleAndAssert(10.toLong(), MultiplexUtil.FLAG_RESET_INITIATOR, "FooBarDoe".toByteArray())
    }

    /**
     * Performs a simple test to compose the protocol data from the given parameters, and then to
     * parse that data and assert extracted values match the input parameters.
     * @param streamId the ID of the stream.
     * @param flags the flags.
     * @param streamData the stream data.
     */
    private fun runFullCircleAndAssert(streamId: Long, flags: Byte, streamData: ByteArray) {
        // Convert to a byte array.
        val bytes = MultiplexUtil.composeProtocolData(streamId, flags, streamData)

        // Parse the byte array and assert the parsed parameters.
        val triple: Triple<Long, Byte, ByteArray> = MultiplexUtil.readProtocolData(bytes)
        assertEquals(streamId, triple.first, "Incorrect stream ID")
        assertEquals(flags, triple.second, "Incorrect flag")
        assertTrue(streamData.contentEquals(triple.third), "Incorrect stream data")
    }
}
