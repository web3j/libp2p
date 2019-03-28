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
package io.web3j.libp2p.transport.tcp.provider.netty

import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelOutboundHandlerAdapter
import io.netty.channel.ChannelPromise
import io.web3j.libp2p.shared.conversion.Varint

class EchoOutboundHandler : ChannelOutboundHandlerAdapter() {

    override fun write(ctx: ChannelHandlerContext, msg: Any, promise: ChannelPromise) {

        if (msg is String) {
            val protoLengthByteArray = Varint.toVarint(msg.length.toULong())

            val newArray = ByteArray(protoLengthByteArray.size + msg.length)
            System.arraycopy(protoLengthByteArray, 0, newArray, 0, protoLengthByteArray.size)
            System.arraycopy(msg.toByteArray(), 0, newArray, protoLengthByteArray.size, msg.length)
            super.write(ctx, newArray, promise)
        } else {
            super.write(ctx, msg, promise)
        }
    }
}
