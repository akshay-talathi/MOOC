package com.netty.process;
import com.netty.initializer.ReplicationClientInitializer;
import com.netty.pojo.Configuration;
import com.netty.pojo.NodeLog;
import com.netty.proto.Message;
import com.netty.proto.Message.Packet;
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

public class ReplicationClient {
	Configuration configuration;
	Packet.Builder message;
	public ReplicationClient(Configuration configuration,Packet.Builder message) {
		// TODO Auto-generated constructor stub
		this.configuration=configuration;
		this.message=message;
		addLog();
		startReplication();
	}
	
	public void addLog()
	{
		configuration.setLastCommitIndex(configuration.getLastCommitIndex()+1);
//		ServerLog serverLog=new  ServerLog();
//		serverLog.setTermId(configuration.getCurrentTerm());
//		serverLog.setAcknowledgements(0);
//		serverLog.setCommitIndex(configuration.getLastCommitIndex());
//		serverLog.setData(message.getData());
//		serverLog.setLeader(configuration.getHost()+":"+configuration.getPort());
//		configuration.getServerLog().add(serverLog);
		NodeLog nodeLog=new NodeLog();
		nodeLog.setTermId(configuration.getCurrentTerm());
		nodeLog.setCommitIndex(configuration.getLastCommitIndex());
//		serverLog.setData(message.getData());
		nodeLog.setLeader(configuration.getHost()+":"+configuration.getPort());
		nodeLog.setUserId(message.getDetails().getUserId());
		nodeLog.setUsername(message.getDetails().getUsername());
		nodeLog.setCourse_id(message.getDetails().getCourseId());
		nodeLog.setCourse_description(message.getDetails().getCourseDescription());
		nodeLog.setCourse_name(message.getDetails().getCourseName());
		configuration.getNodeLog().add(nodeLog);
	}
	
	public void startReplication() {
		try {
			System.out.println("REPLICATING");
			ChannelFuture lastWriteFuture = null;
			Message.Packet.Builder packet = Message.Packet.newBuilder(configuration.getPacket().build());
			packet.setMessageType(Type.DATA);
			packet.setDetails(message.getDetails());
			packet.setCommitIndex(configuration.getLastCommitIndex());
			packet.setTermId(configuration.getCurrentTerm());
			refreshChannels();
			//Replicate to channels
			for(Channel ch :configuration.getReplicationChannel())
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
			//	if(configuration.getReplicationChannel().get(i)!=null)
				//	continue;
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class).handler(new ReplicationClientInitializer(sslCtx,configuration));
				// Start the connection attempt.
				Channel channel=null;
				try
				{
					channel=b.connect(configuration.getNodes().get(i).getHost(), configuration.getNodes().get(i).getPort()).sync().channel();
				}
				catch(Exception e)
				{}
				configuration.getReplicationChannel().set(i, channel);
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
	}
}
