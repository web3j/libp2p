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

import io.web3j.streammux.MuxedStream

/**
 * This is an equivalent definition to multistream-select, which allows one peer to communicate with
 * a receiver's multiselect module for protocol selection.
 * <br />
 * [@see https://github.com/multiformats/go-multistream/blob/master/client.go]
 */
interface MultiselectClient {

    /**
     * Performs the initial multistream handshake, informing the muxer of the protocol
     * that will be used to communicate on the stream.
     * @param stream the stream to agree the protocol for communication for.
     * @param protocol the protocol to select.
     * @return the selected protocol for communication over the stream; null if the muxer
     * on the other end cannot handle this protocol.
     * @throws [MultistreamSelectException] if an error was encountered.
     */
    fun selectProtocolOrFail(stream: MuxedStream, protocol: String): String?

    /**
     * Goes through the given protocols and returns the first protocol that is agreed
     * with the other peer for use over the the stream.
     * @param stream the stream to agree the protocol for communication for.
     * @param protocols the protocols to try.
     * @return the selected protocol for communication over the stream; null if one was not agreed upon.
     * @throws [MultistreamSelectException] if an error was encountered.
     */
    fun selectOneOf(stream: MuxedStream, vararg protocols: String): String?
}
