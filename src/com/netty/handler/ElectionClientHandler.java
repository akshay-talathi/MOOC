package com.netty.handler;

import com.netty.pojo.Configuration;
import com.netty.proto.Message.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
public class ElectionClientHandler extends SimpleChannelInboundHandler<Packet> {
	
	Configuration configuration;
	boolean majority=false;

	public ElectionClientHandler(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
		
		System.out.println("Vote "+msg.getVote() +"by: "+msg.getHost()+":"+msg.getPort());
		if(msg.getVote()==Packet.Vote.GRANTED)
				configuration.setVotesGranted(configuration.getVotesGranted()+1);
		if(msg.getVote()==Packet.Vote.DENIED)
			 configuration.setVotesGranted(configuration.getVotesDenied()+1);
		
		if(configuration.getVotesGranted()>=(configuration.getNodes().size()/2)+1 && !configuration.getState().equals(Configuration.State.LEADER))
		{
				majority=true;
				System.out.println("Achieved Majority!");
//				configuration.setCurrentTerm(configuration.getCurrentTerm()+1);
//				configuration.setLastVotedTerm(configuration.getCurrentTerm()+1);
				configuration.setCurrentTerm(msg.getTermId());
				configuration.setLastVotedTerm(msg.getTermId());
				configuration.setState(Configuration.State.LEADER);
				//Update Leader
				configuration.getLeader().setHost(configuration.getHost());
				configuration.getLeader().setPort(configuration.getPort());
				return;
		}
		if(configuration.getVotesGranted()+configuration.getVotesDenied()>=configuration.getNodes().size())
			return;
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
