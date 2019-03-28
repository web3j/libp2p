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

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.Channel
import io.netty.channel.ChannelFuture
import io.netty.channel.ChannelHandler
import io.netty.channel.ChannelHandlerContext
import io.netty.channel.ChannelInboundHandlerAdapter
import io.netty.channel.ChannelOption
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.web3j.libp2p.transport.TransportConnectionListener
import io.web3j.libp2p.transport.tcp.TCPTransport
import io.web3j.libp2p.transport.tcp.TCPTransportConnection
import org.slf4j.LoggerFactory
import java.net.SocketAddress

/**
 * A simplified interface for creating connections using the Netty library.
 */
object NettyFacade {

    val LOGGER = LoggerFactory.getLogger(NettyFacade::class.java)!!

    /**
     * Starts the listener on the given socket address.
     * @param socketAddress the socket address
     * @param listener the listener to be notified when a connection is available.
     * @param tcpTransport the TCP transport layer that was used to create this listener.
     * @return the Netty-specific information about the (server) listener.
     */
    fun startListener(
        socketAddress: SocketAddress,
        listener: TransportConnectionListener,
        tcpTransport: TCPTransport
    ): NettyServerInfo {
        LOGGER.info("Starting the listener on $socketAddress")

        var started = false
        val bossGroup = NioEventLoopGroup()
        val workerGroup = NioEventLoopGroup()
        try {
            val server = ServerBootstrap().group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel::class.java)
                .handler(MyChannelInitializer())
                .childHandler(SimpleChannelHandler(listener, tcpTransport))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            // Bind immediately.
            val serverChannelFuture: ChannelFuture = server.bind(socketAddress).sync()
            val serverChannel: Channel = serverChannelFuture.channel()

            LOGGER.debug("Server has been started")
            started = true

            // Set up return object.
            return NettyServerInfo(
                serverChannel.localAddress(),
                serverChannel,
                bossGroup,
                workerGroup
            )
        } finally {
            if (!started) {
                LOGGER.debug("Startup failed, shutting down gracefully ...")
                workerGroup.shutdownGracefully()
                bossGroup.shutdownGracefully()
            }
        }
    }

    /**
     * Dials the peer on the given address.
     * @param socketAddress the address of the peer to be dialed.
     * @return the Netty-specific information about the (client) connection.
     */
    fun dial(socketAddress: SocketAddress): NettyClientInfo {
        val workerGroup = NioEventLoopGroup()
        var started = false

        try {
            val b = Bootstrap()
            b.group(workerGroup)
            b.channel(NioSocketChannel::class.java)
            b.option(ChannelOption.SO_KEEPALIVE, true)
            b.option(ChannelOption.SO_BACKLOG, 128)
            b.handler(MyChannelInitializer())

            val channelFuture: ChannelFuture = b.connect(socketAddress).sync()
            val clientChannel: Channel = channelFuture.channel()

            LOGGER.debug("Client has been started")
            started = true

            // Set up return object.
            return NettyClientInfo(clientChannel)
        } finally {
            if (!started) { // TODO: why does this get triggered?
                LOGGER.debug("Startup failed, shutting down gracefully ...")
                workerGroup.shutdownGracefully()
            }
        }
    }
}

@ChannelHandler.Sharable
private class SimpleChannelHandler(
    private val listener: TransportConnectionListener,
    private val tcpTransport: TCPTransport
) : ChannelInboundHandlerAdapter() {

    // channel associated with event loop
    override fun channelRegistered(ctx: ChannelHandlerContext) {
        NettyFacade.LOGGER.debug("----- channelRegistered: {}", ctx)
        val channel: SocketChannel = ctx.channel() as SocketChannel
        NettyFacade.LOGGER.debug("New channel opened, channel=$channel")
        listener.onNewConnection(TCPTransportConnection.build(channel, tcpTransport))
        super.channelRegistered(ctx)
    }

    // connected to remote peer
    override fun channelActive(ctx: ChannelHandlerContext) {
        NettyFacade.LOGGER.debug("----- channelActive: {}", ctx)
        super.channelActive(ctx)
    }

    // disconnected from remote peer
    override fun channelInactive(ctx: ChannelHandlerContext) {
        NettyFacade.LOGGER.debug("----- channelInactive: {}", ctx)
        ctx.close()
        super.channelInactive(ctx)
    }

    override fun channelUnregistered(ctx: ChannelHandlerContext) {
        NettyFacade.LOGGER.debug("----- channelUnregistered: {}", ctx)
        super.channelUnregistered(ctx)
    }

    override fun exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
        NettyFacade.LOGGER.error("----- exceptionCaught: {}", ctx, cause)
        ctx.close()
        super.exceptionCaught(ctx, cause)
    }

    override fun channelRead(ctx: ChannelHandlerContext, msg: Any) {
        NettyFacade.LOGGER.debug("----- channelRead: {}", msg)
        val responseData = "Received your message of: $msg"
        val future = ctx.writeAndFlush(responseData)
        future.sync()
//        future.addListener(ChannelFutureListener.CLOSE)
        super.channelRead(ctx, msg)
    }

    override fun channelReadComplete(ctx: ChannelHandlerContext) {
        NettyFacade.LOGGER.debug("----- channelReadComplete: {}", ctx)
        super.channelReadComplete(ctx)
    }
}

class NettyClientInfo(val clientChannel: Channel)

class NettyServerInfo(
    val socketAddress: SocketAddress,
    val serverChannel: Channel,
    val bossGroup: NioEventLoopGroup,
    val workerGroup: NioEventLoopGroup
) {

    // TODO: add a listener so we can tell when the server channel is closed unexpectedly?
//    serverChannel.closeFuture().addListener(this)
}
