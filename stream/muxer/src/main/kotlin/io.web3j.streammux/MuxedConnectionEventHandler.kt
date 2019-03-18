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
package io.web3j.streammux

/**
 * Provides a facility to receive events on a [MuxedConnection]
 */
interface MuxedConnectionEventHandler {

    /**
     * Fired when a new stream has been accepted by the connection.
     * @param acceptedStream the new stream that was accepted.
     */
    fun onStreamAccepted(acceptedStream: MuxedStream): Unit
}
