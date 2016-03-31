package com.netty.handler;

import com.netty.pojo.Configuration;
import com.netty.pojo.Node;
import com.netty.process.UpdateClient;
import com.netty.proto.Message.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
public class HeartBeatClientHandler extends SimpleChannelInboundHandler<Packet> {
	Configuration configuration;
	public HeartBeatClientHandler(Configuration configuration){
		this.configuration = configuration;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception{
			System.out.println("HeartBeat Reply from:- "+msg.getHost()+":"+msg.getPort());	
			
			System.out.println("Message Commmit Index:"+msg.getCommitIndex());
			System.out.println("Configuration Commmit Index:"+configuration.getLastCommitIndex());
			if(msg.getCommitIndex()<configuration.getLastCommitIndex()-1)
			{
				if(!isUpdating(msg.getHost(), msg.getPort()))
				{
					configuration.getUpdateExecutor().execute(new UpdateClient(configuration, msg));
					configuration.getUpdateNode().add(new Node(msg.getHost(), msg.getPort()));
				}
			}		
	}
	
	public boolean isUpdating(String host,int port)
	{
		for(Node node: configuration.getUpdateNode())
		{
			if(node.getHost().equals(host) && node.getPort()==node.getPort())
				return true;
		}
		return false;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
