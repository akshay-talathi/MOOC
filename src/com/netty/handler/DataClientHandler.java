package com.netty.handler;
import com.netty.pojo.Configuration;
import com.netty.proto.Message.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Handles a client-side channel.
 */
public class DataClientHandler extends SimpleChannelInboundHandler<Packet> {
	
	Configuration configuration;
	
	public DataClientHandler(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
		System.out.println(msg.getClientResponse());
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
