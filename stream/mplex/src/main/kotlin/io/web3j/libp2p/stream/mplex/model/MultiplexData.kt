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
package io.web3j.libp2p.stream.mplex.model

/**
 * Represents a message that is sent across between peers in the mplex protocol.
 */
data class MultiplexData(val streamId: ULong, val flags: HeaderFlag, val data: ByteArray) {

    /**
     * @return the stream name; null if one is  not available in this message.
     */
    fun getStreamName(): String? {
        return if (flags == HeaderFlag.NewStream) String(data) else null
    }

    override fun toString(): String {
        // TODO: clean this up to re-use parts.
        return if (flags == HeaderFlag.NewStream) {
            "data[streamId=$streamId,streamName=${getStreamName()},flags=$flags,data.size=${data.size}]"
        } else {
            "data[streamId=$streamId,flags=$flags,data.size=${data.size}]"
        }
    }
}
