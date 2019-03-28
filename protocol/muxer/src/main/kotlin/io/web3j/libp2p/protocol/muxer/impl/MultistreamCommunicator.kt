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
package io.web3j.libp2p.protocol.muxer.impl

import io.web3j.streammux.MuxedStream

object MultistreamCommunicator {

    fun write(stream: MuxedStream, message: String) {

//        # Send our MULTISELECT_PROTOCOL_ID to counterparty
//        await communicator.write(MULTISELECT_PROTOCOL_ID)
// delimWriteBuffered: Go code!

        TODO("IMPLEMENT")
    }

    fun writeAndWait(stream: MuxedStream, message: String) {

//        # Send our MULTISELECT_PROTOCOL_ID to counterparty
//        await communicator.write(MULTISELECT_PROTOCOL_ID)
// delimWriteBuffered: Go code!

        TODO("IMPLEMENT")
    }

    fun readStreamUntilEof(stream: MuxedStream): String? {
//        tok, readErr := ReadNextToken(rwc)
//        writeErr := <-errCh
        TODO("IMPLEMENT")
    }
}
