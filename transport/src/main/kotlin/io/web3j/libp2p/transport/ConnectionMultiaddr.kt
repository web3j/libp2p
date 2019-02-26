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
package io.web3j.libp2p.transport

import io.ipfs.multiformats.multiaddr.Multiaddr

/**
 * Provides access to multiaddr addresses for endpoints.
 */
interface ConnectionMultiaddr {

    /**
     * @return the local multiaddr associated with this connection.
     */
    fun getLocalMultiaddr(): Multiaddr

    /**
     * @return the remote peer's multiaddr associated with this connection.
     */
    fun getRemoteMultiaddr(): Multiaddr
}
