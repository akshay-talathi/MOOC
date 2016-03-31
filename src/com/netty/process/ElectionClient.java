package com.netty.process;
import com.netty.initializer.ElectionClientInitializer;
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

public class ElectionClient {
	Configuration configuration;
	public ElectionClient(Configuration configuration) {
		// TODO Auto-generated constructor stub
		this.configuration=configuration;
		this.configuration.setVotesGranted(1);
		this.configuration.setVotesDenied(0);
	}
	
	public void requestVotes() {
		try {			
			ChannelFuture lastWriteFuture = null;
			Message.Packet.Builder packet = Message.Packet.newBuilder();
			packet.setHost(configuration.getHost());
			packet.setPort(configuration.getPort());
			packet.setMessageType(Type.ELECTION);
			packet.setTermId(configuration.getCurrentTerm()+1);
			refreshChannels();
			//Send Heart Beats While In Leader State
			for(Channel ch :configuration.getElectionChannel())
			{
				try {
					lastWriteFuture=ch.writeAndFlush(packet);
				} catch (Exception e) {
					// TODO: handle exception
				}
			}
			// Wait until all messages are flushed before closing the channel.
			if (lastWriteFuture != null) {
				lastWriteFuture.sync();
			}
		}
		catch(Exception e)
		{
			e.printStackTrace();
		}		
	}
	
	public void refreshChannels()
	{
		EventLoopGroup group = new NioEventLoopGroup();
		try 
		{
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			for(int i=0;i<configuration.getNodes().size();i++)
			{
				
		//		if(configuration.getHeartBeatChannel().get(i)!=null)
			//		continue;
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class).handler(new ElectionClientInitializer(sslCtx,configuration));
				// Start the connection attempt.
				Channel channel=null;
				try
				{
					channel=b.connect(configuration.getNodes().get(i).getHost(), configuration.getNodes().get(i).getPort()).sync().channel();
				}
				catch(Exception e)
				{}
				configuration.getElectionChannel().set(i, channel);
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
	

	}
	
}
