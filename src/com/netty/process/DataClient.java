package com.netty.process;
import com.netty.initializer.DataClientInitializer;
import com.netty.pojo.Configuration;
import com.netty.pojo.Node;
import com.netty.proto.Message.Packet;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class DataClient {
	Configuration configuration;
	Packet.Builder packet;
	Node leader;
	public DataClient(Configuration configuration,Packet.Builder packet,Node leader) {
		// TODO Auto-generated constructor stub
		this.configuration = configuration;
		this.packet=packet;
		this.leader=leader;
		sendData();
	}
	
	public void sendData() {
		try {
					ChannelFuture lastWriteFuture = null;
					refreshChannels();			
					if(leader==null)
					{
						for (Channel ch : configuration.getDataChannel()) {
							try {
								lastWriteFuture = ch.writeAndFlush(packet);
							} catch (Exception e) {
								// TODO: handle exception
								// e.printStackTrace();
							}
						}
					}
					else
					{		
						for(int i=0;i<configuration.getNodes().size();i++)
						{
							if(configuration.getNodes().get(i).getHost().equals(leader.getHost()) && configuration.getNodes().get(i).getPort()==leader.getPort())
							{
								lastWriteFuture=configuration.getDataChannel().get(i).writeAndFlush(packet);
							}
						}
					}
			// Wait until all messages are flushed before closing the channel.
			if (lastWriteFuture != null)
				lastWriteFuture.sync();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public void refreshChannels() {
		EventLoopGroup group = new NioEventLoopGroup();
		try {
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE)
					.build();
			for (int i = 0; i < configuration.getNodes().size(); i++) {
		//		if (configuration.getDataChannel().get(i) != null)
			//		continue;
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class)
						.handler(new DataClientInitializer(sslCtx, configuration));
				// Start the connection attempt.
				Channel channel = null;
				try {
					channel = b.connect(configuration.getNodes().get(i).getHost(),
							configuration.getNodes().get(i).getPort()).sync().channel();
				} catch (Exception e) {
				}
				configuration.getDataChannel().set(i, channel);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
