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
 * The various header flags that are acceptable for a multiplex message.
 * @param value the byte value of the flag.
 * @param initiator whether this flag is sent by the stream initiator.
 */
enum class HeaderFlag(val value: Byte, val initiator: Boolean) {

    NewStream(0x00.toByte(), true),
    MessageReceiver(0x01.toByte(), false),
    MessageInitiator(0x02.toByte(), true),
    CloseReceiver(0x03.toByte(), false),
    CloseInitiator(0x04.toByte(), true),
    ResetReceiver(0x05.toByte(), false),
    ResetInitiator(0x06.toByte(), true)

}