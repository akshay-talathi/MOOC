package com.netty.handler;

import com.netty.hibernate.HibernateUtil;
import com.netty.pojo.Configuration;
import com.netty.pojo.NodeLog;
import com.netty.process.DataClient;
import com.netty.proto.Message.Packet;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

/**
 * Handles a client-side channel.
 */
public class ReplicationClientHandler extends SimpleChannelInboundHandler<Packet> {
	
	Configuration configuration;
	SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	Session session = sessionFactory.openSession();

	public ReplicationClientHandler(Configuration configuration) {
		this.configuration = configuration;
		session.beginTransaction();
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet msg) throws Exception {
			System.out.println("Inside Channel Handler of Rep;ication");
				for(NodeLog nodeLog:configuration.getNodeLog())
				{
					System.out.println("Inside Channel Handler of Rep;ication -- nodelog for "+msg.getCommitIndex()+"/"+nodeLog.getCommitIndex());
					if(msg.getCommitIndex()==nodeLog.getCommitIndex())
					{
						System.out.println("Inside Channel Handler of Rep;ication -- if same commit");
						nodeLog.setAcks(nodeLog.getAcks()+1);
						if(nodeLog.getAcks()>=(configuration.getNodes().size()/2)+1 && !nodeLog.isAck()) {
							System.out.println("Inside Channel Handler of Rep;ication -- if acks > and not ack");
							nodeLog.setAck(true);
							nodeLog.setCommitted(true);
							System.out.println("Log Committed:");
							System.out.println("TermId:"+nodeLog.getTermId()+"||"+"Commit Index:"+nodeLog.getCommitIndex()+"||Leader:"+nodeLog.getLeader()+"||Data:"+nodeLog.getCourse_name());
							configuration.setLastCommitIndex(nodeLog.getCommitIndex());
							Packet.Builder packet=Packet.newBuilder(configuration.getPacket().build());
							packet.setMessageType(Packet.Type.COMMITREQUEST);
							packet.setCommitIndex(nodeLog.getCommitIndex());
							session.save(nodeLog);
							session.getTransaction().commit();
							new DataClient(configuration, packet,null);

						}
					}
				}

		}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
