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
package io.web3j.libp2p.net

import io.ipfs.multiformats.multiaddr.Protocol
import io.web3j.streammux.BasicStream

/**
 * A bi-directional channel between two nodes in the network. <p />
 * A [NetworkStream] is an extension of [BasicStream] that provides an identifying
 * protocol for that stream.
 */
interface NetworkStream : BasicStream {

    /**
     * The protocol that this stream runs on.
     */
    val protocol: Protocol
}
