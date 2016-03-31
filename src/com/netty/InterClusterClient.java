package com.netty;

import com.netty.initializer.DataClientInitializer;
import com.netty.pojo.Configuration;
import com.netty.proto.Message;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

/**
 * Created by Akshay on 12/7/15.
 */
public class InterClusterClient {
    static final String HOST = "10.0.8.1";
    static final int PORT = Integer.parseInt(String.valueOf(8080));

    public static void main(String[] args) throws Exception {
        // Configure SSL.
        final SslContext sslCtx = SslContextBuilder.forClient()
                .trustManager(InsecureTrustManagerFactory.INSTANCE).build();
        Configuration configuration = new Configuration();


        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group)
                    .channel(NioSocketChannel.class)
                    .handler(new DataClientInitializer(sslCtx, configuration));

            // Start the connection attempt.
            Channel ch = b.connect(HOST, PORT).sync().channel();
            // Read commands from the stdin.
            ChannelFuture lastWriteFuture = null;
            Message.Packet.Builder packet = Message.Packet.newBuilder(configuration.getPacket().build());

            // Sends the received line to the server.
            lastWriteFuture = ch.writeAndFlush(packet);
            lastWriteFuture.channel().closeFuture().sync();


            // Wait until all messages are flushed before closing the channel.
            if (lastWriteFuture != null) {
                lastWriteFuture.sync();
            }
        } finally {
            // The connection is closed automatically on shutdown.
            group.shutdownGracefully();
        }
    }
}
