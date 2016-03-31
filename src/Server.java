import java.util.ArrayList;

import com.netty.initializer.DataClientInitializer;
import com.netty.initializer.ElectionClientInitializer;
import com.netty.initializer.HeartBeatClientInitializer;
import com.netty.initializer.ReplicationClientInitializer;
import com.netty.initializer.ServerInitializer;
import com.netty.pojo.Configuration;
import com.netty.pojo.Node;
import com.netty.process.ElectionManager;
import com.netty.proto.Message;
import com.netty.proto.Message.Packet;

import io.netty.bootstrap.Bootstrap;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;
public final class Server {
	
	public static void addNodes(Configuration configuration) {
		ArrayList<Node> nodes = new ArrayList<Node>();
	  //nodes.add(new Node("10.0.0.2", 8080));
		nodes.add(new Node("10.0.0.4", 8080));
//		nodes.add(new Node("10.0.0.3", 8080));
		configuration.setNodes(nodes);
	}
	
	public static void initConfiguration(Configuration configuration)
	{
		configuration.setHost("10.0.0.1");
		configuration.setPort(8080);
		Message.Packet.Builder packet = Message.Packet.newBuilder();
		packet.setHost(configuration.getHost());
		packet.setPort(configuration.getPort());
		packet.setMessageType(Packet.Type.HEARTBEAT);
		packet.setTermId(0);
		configuration.setPacket(packet);
	}

	public static void addReplicationChannels(Configuration configuration)
	{
		EventLoopGroup group = new NioEventLoopGroup();
		try 
		{
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			for(Node node:configuration.getNodes())
			{
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class).handler(new ReplicationClientInitializer(sslCtx,configuration));
				// Start the connection attempt.
				Channel ch=null;
				try
				{
					ch=b.connect(node.getHost(), node.getPort()).sync().channel();
				}
				catch(Exception e)
				{}
				configuration.getReplicationChannel().add(ch);
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
	}


	public static void addHeartBeatChannels(Configuration configuration)
	{
		EventLoopGroup group = new NioEventLoopGroup();
		try 
		{
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			for(Node node:configuration.getNodes())
			{
				Bootstrap b = new Bootstrap();
			
				b.group(group).channel(NioSocketChannel.class).handler(new HeartBeatClientInitializer(sslCtx,configuration));
				// Start the connection attempt.
				Channel ch=null;
				try
				{
					ch=b.connect(node.getHost(), node.getPort()).sync().channel();
				}
				catch(Exception e)
				{}
				configuration.getHeartBeatChannel().add(ch);
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
	}

	
	
	public static void addElectionChannels(Configuration configuration)
	{	
		EventLoopGroup group = new NioEventLoopGroup();
		try 
		{
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			for(Node node:configuration.getNodes())
			{
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class).handler(new ElectionClientInitializer(sslCtx,configuration));
				// Start the connection attempt.
				Channel ch= null;
				try
				{
					ch = b.connect(node.getHost(), node.getPort()).sync().channel();
				}
				catch(Exception e)
				{}
				configuration.getElectionChannel().add(ch);
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	

	
	
	public static void addDataChannels(Configuration configuration)
	{	
		EventLoopGroup group = new NioEventLoopGroup();
		try 
		{
			SslContext sslCtx = SslContextBuilder.forClient().trustManager(InsecureTrustManagerFactory.INSTANCE).build();
			for(Node node:configuration.getNodes())
			{
				Bootstrap b = new Bootstrap();
				b.group(group).channel(NioSocketChannel.class).handler(new DataClientInitializer(sslCtx,configuration));
				// Start the connection attempt.
				Channel ch= null;
				try
				{
					ch = b.connect(node.getHost(), node.getPort()).sync().channel();
				}
				catch(Exception e)
				{}
				configuration.getDataChannel().add(ch);
			}
		} 
		catch (Exception e) {
			// TODO: handle exception
		}
	}
	
	
	public static void startServer(final Configuration configuration)
	{		
			EventLoopGroup bossGroup = new NioEventLoopGroup(1);
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				SelfSignedCertificate ssc = new SelfSignedCertificate();
				SslContext sslCtx = SslContextBuilder.forServer(ssc.certificate(), ssc.privateKey()).build();
				ServerBootstrap b = new ServerBootstrap();
				b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO)).childHandler(new ServerInitializer(sslCtx, configuration));
				final ChannelFuture cf =  b.bind(configuration.getPort()).sync();
				cf.addListener(new GenericFutureListener<Future<? super Void>>() {

					@Override
					public void operationComplete(Future<? super Void> arg0)
							throws Exception {
						if(cf.isSuccess()){
							System.out.println("Server started");			
							System.out.println("Listening on port "+configuration.getPort());
							configuration.restartExecutor();
							configuration.getExecutor().execute(new ElectionManager(configuration));		
						}	
					}
				});		
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
		try {
			addNodes(configuration);
			initConfiguration(configuration);
			addHeartBeatChannels(configuration);
			addElectionChannels(configuration);
			addReplicationChannels(configuration);
			addDataChannels(configuration);
			
			startServer(configuration);
		} 
		catch(Exception e){
			e.printStackTrace();
		}
	}
}