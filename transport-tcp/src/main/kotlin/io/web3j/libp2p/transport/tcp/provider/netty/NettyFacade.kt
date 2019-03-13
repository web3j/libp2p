package io.web3j.libp2p.transport.tcp.provider.netty

import io.netty.bootstrap.Bootstrap
import io.netty.bootstrap.ServerBootstrap
import io.netty.channel.*
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
                .childHandler(SimpleChannelHandler(listener, tcpTransport))
                .option(ChannelOption.SO_BACKLOG, 128)
                .childOption(ChannelOption.SO_KEEPALIVE, true)

            // Bind immediately.
            val serverChannelFuture: ChannelFuture = server.bind(socketAddress).sync();
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
            b.handler(object : ChannelInitializer<SocketChannel>() {

                @Throws(Exception::class)
                public override fun initChannel(ch: SocketChannel) {
                    LOGGER.debug("Client connection established to $socketAddress, channel=$ch")
//                    ch.pipeline().addLast(
//                        RequestDataEncoder(),
//                        ResponseDataDecoder(), ClientHandler()
//                    )
                }
            })

            val channelFuture: ChannelFuture = b.connect(socketAddress).sync()
            val clientChannel: Channel = channelFuture.channel()

            LOGGER.debug("Client has been started")
            started = true

            // Set up return object.
            return NettyClientInfo(clientChannel)
        } finally {
            if (!started) {
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