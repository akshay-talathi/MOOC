package com.netty.intercluster;

import com.netty.hibernate.HibernateUtil;
import com.netty.pojo.Configuration;
import com.netty.pojo.NodeLog;
import com.netty.proto.App;
import com.netty.proto.Message.Packet;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

/**
 * Handles a client-side channel.
 */
public class InterClusterHandler extends SimpleChannelInboundHandler<App.Request> {

	Configuration configuration;
	SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	Session session = sessionFactory.openSession();

	public InterClusterHandler(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public void channelRead0(ChannelHandlerContext ctx, App.Request message) throws Exception {

		System.out.println("GOT THE CLIENT MESSAGE");
		App.Request.Builder response = App.Request.newBuilder(message);
		App.ClientMessage.Builder clientMessage = App.ClientMessage.newBuilder(message.getBody().getClientMessage());
		App.Payload.Builder payload = App.Payload.newBuilder(message.getBody());

			Integer courseId = message.getBody().getClientMessage().getDetails().getCourseId();
			session.beginTransaction();
			List<NodeLog> nodeLogList = session.createQuery("From NodeLog n where n.id=1").list();
			if(!nodeLogList.isEmpty()) {
				System.out.println(nodeLogList.get(0).getTermId());
				App.Details.Builder details = App.Details.newBuilder();
				details.setUserId(nodeLogList.get(0).getUserId());
				details.setUsername(nodeLogList.get(0).getUsername());
				details.setCourseDescription(nodeLogList.get(0).getCourse_description());
				details.setCourseName(nodeLogList.get(0).getCourse_name());
				clientMessage.setDetails(details);
				payload.setClientMessage(clientMessage);
				response.setBody(payload);
				ctx.writeAndFlush(response);
			}
		session.close();


	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
