package com.netty;

import com.netty.initializer.ServerInitializer;
import com.netty.intercluster.InterClusterInitializer;
import com.netty.pojo.Configuration;
import com.netty.process.ElectionManager;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

/**
 * Created by Akshay on 12/7/15.
 */
public class InterClusterServer {



    public static void startServer(final Configuration configuration)
    {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            SelfSignedCertificate ssc = new SelfSignedCertificate();
            SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new InterClusterInitializer(sslCtx, configuration));
            final ChannelFuture cf =  b.bind(configuration.getPort()).sync();
            cf.channel().closeFuture().sync();
        }
        catch(Exception e){
            e.printStackTrace();
        }
        finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
    public static void main(String[] args){

        Configuration configuration = new Configuration();
        configuration.setHost("10.0.8.3");
        configuration.setPort(8888);
        try {
            startServer(configuration);
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
