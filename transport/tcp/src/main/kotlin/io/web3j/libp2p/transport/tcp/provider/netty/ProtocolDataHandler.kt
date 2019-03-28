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
import io.netty.channel.ChannelInboundHandlerAdapter
import spipe.pb.Spipe

class ProtocolDataHandler : ChannelInboundHandlerAdapter() {

    override fun channelRead(ctx: ChannelHandlerContext?, msg: Any?) {
        msg as ByteArray
        if (msg[0] == '/'.toByte()) {
            println("RECEIVED: " + String(msg))
        } else {
            println("RECEIVED NON STRING: " + msg.size)
        }
        super.channelRead(ctx, msg)
    }

    private fun tryParse(byteArray: ByteArray): Unit {
        try {
            val propose = Spipe.Propose.parseFrom(byteArray)
            AccumulatorInboundHandler.LOGGER.debug("GOT PROPOSE: {}", propose)
        } catch (e: Exception) {
            AccumulatorInboundHandler.LOGGER.error("NOT A PROPOSE OBJECT");
        }

        try {
            val exchange = Spipe.Exchange.parseFrom(byteArray)
            AccumulatorInboundHandler.LOGGER.debug("GOT EXCHANGE: {}", exchange)
        } catch (e: Exception) {
            AccumulatorInboundHandler.LOGGER.error("NOT A EXCHANGE OBJECT");
        }

    }

}