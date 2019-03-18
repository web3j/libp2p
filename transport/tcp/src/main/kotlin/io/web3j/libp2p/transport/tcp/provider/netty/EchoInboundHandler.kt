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

class EchoInboundHandler : ChannelInboundHandlerAdapter() {

    // 1st: /multistream/1.0.0
    override fun channelRead(ctx: ChannelHandlerContext, msg: Any?) {
        LOGGER.info("----- channelRead: $msg")
        // TODO: handle multistream/1.0.0
        super.channelRead(ctx, msg)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        LOGGER.info("----- channelReadComplete")
//        val x = "ipfs/Qma3GsJmB47xYuyahPZPSadh1avvxfyYQwk8R3UnFrQ6aP"
//
//        ctx.channel().write("/$x/multistream/1.0.0\n").sync()
        super.channelReadComplete(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext?, cause: Throwable) {
        LOGGER.warn("----- exceptionCaught: ${cause.message}", cause)
        super.exceptionCaught(ctx, cause)
    }

//    fun messageReceived(
//        ctx: ChannelHandlerContext, e: MessageEvent
//    ) {
//        // Send back the received message to the remote peer.
//        transferredBytes.addAndGet((e.getMessage() as ChannelBuffer).readableBytes())
//        e.getChannel().write(e.getMessage())
//    }
//
//    fun exceptionCaught(
//        ctx: ChannelHandlerContext, e: ExceptionEvent
//    ) {
//        // Close the connection when an exception is raised.
//        logger.log(
//            Level.WARNING,
//            "Unexpected exception from downstream.",
//            e.getCause()
//        )
//        e.getChannel().close()
//    }

    companion object {
        val LOGGER = LoggerFactory.getLogger(EchoInboundHandler::class.java.name)!!
    }
}
