package com.netty.process;

import com.netty.initializer.HeartBeatClientInitializer;
import com.netty.pojo.Configuration;
import com.netty.proto.Message;
import com.netty.proto.Message.Packet.Type;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;

public class HeartBeatClient {
	Configuration configuration;

	public HeartBeatClient(Configuration configuration) {
		// TODO Auto-generated constructor stub
		this.configuration = configuration;
	}

	public void sendHeartBeats() {
		try {
			ChannelFuture lastWriteFuture = null;
			Message.Packet.Builder packet = Message.Packet.newBuilder(configuration.getPacket().build());
			packet.setMessageType(Type.HEARTBEAT);
			packet.setTermId(configuration.getCurrentTerm());
			// Send Heart Beats While In Leader State
			while (configuration.getState() == Configuration.State.LEADER) {
				System.out.println("Sending HeartBeat from:- "+configuration.getHost()+":"+configuration.getPort() );
				try {
					Thread.sleep(1000);
					refreshChannels();
					for (Channel ch : configuration.getHeartBeatChannel()) {
						try {
							System.out.println(ch.remoteAddress());
							lastWriteFuture = ch.writeAndFlush(packet);
						} catch (Exception e) {
							// TODO: handle exception
//							 e.printStackTrace();
						}
					}

				} catch (Exception e) {
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
				
				System.out.println("Size:"+configuration.getHeartBeatChannel().size());
			//	if (configuration.getHeartBeatChannel().get(i) != null)
				//	continue;
				
				//System.out.println("Node: "+configuration.getNodes().get(i).getHost());
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class)
						.handler(new HeartBeatClientInitializer(sslCtx, configuration));
				// Start the connection attempt.
				Channel channel = null;
				try {
					channel = b.connect(configuration.getNodes().get(i).getHost(),
							configuration.getNodes().get(i).getPort()).sync().channel();
				} catch (Exception e) {
				}
				System.out.println("Adding:"+configuration.getNodes().get(i).getHost()+":"+configuration.getNodes().get(i).getPort());
				configuration.getHeartBeatChannel().set(i, channel);
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
	}

}
