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
import org.slf4j.LoggerFactory
import spipe.pb.Spipe

class EchoInboundHandler : ChannelInboundHandlerAdapter() {

    var inTheMiddleOfAStream = false
    // 1st: /multistream/1.0.0
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        LOGGER.info("----- channelRead: $msg")
        msg as ByteArray
        val stringRead = String(msg)

        LOGGER.info("READ STRING: \n'$stringRead'")

        if (msg.size > 0 && !stringRead.startsWith("/")) {

            try {
                val propose = Spipe.Propose.parseFrom(msg)
                LOGGER.debug("GOT PROPOPSE: {}", propose)
            } catch (e: Exception) {
                LOGGER.error("NOT A PROPOSE OBJECT")
            }

            try {
                val exchange = Spipe.Exchange.parseFrom(msg)
                LOGGER.debug("GOT EXCHANGE: {}", exchange)
            } catch (e: Exception) {
                LOGGER.error("NOT A EXCHANGE OBJECT")
            }
        }

        // TODO: handle multistream/1.0.0
        super.channelRead(ctx, msg)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        LOGGER.warn("----- exceptionCaught: ${cause.message}", cause)
        super.exceptionCaught(ctx, cause)
    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(EchoInboundHandler::class.java.name)!!
    }
}
