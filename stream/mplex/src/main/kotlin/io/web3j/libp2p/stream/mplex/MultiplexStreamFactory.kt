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
package io.web3j.libp2p.stream.mplex

/**
 * A factory interface that allows a [MultiplexSession] to plug-in various
 * implementations of [MultiplexStream].
 */
interface MultiplexStreamFactory {

    /**
     * Creates a new stream with the given parameters.
     * @param streamId the ID of the stream.
     * @param initiator whether this peer is the initiator.
     * @param name an optional name for the stream.
     */
    fun create(streamId: ULong, initiator: Boolean, name: String? = null): MultiplexStream
}