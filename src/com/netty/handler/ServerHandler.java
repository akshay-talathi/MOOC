package com.netty.handler;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.netty.hibernate.HibernateUtil;
import com.netty.pojo.Configuration;
import com.netty.pojo.NodeLog;
import com.netty.process.DataClient;
import com.netty.process.ElectionManager;
import com.netty.process.ReplicationClient;
import com.netty.proto.Message;
import com.netty.proto.Message.Packet;
import com.netty.proto.Message.Packet.Type;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class ServerHandler extends SimpleChannelInboundHandler<Packet> {
	Configuration configuration;
	SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
	Session session = sessionFactory.openSession();

	public ServerHandler(Configuration configuration) {
		this.configuration = configuration;
		session.beginTransaction();
	}

	
	
	@Override
	public void channelRead0(ChannelHandlerContext ctx, Packet message) throws Exception {
		try {
			if (message.getMessageType() == Packet.Type.HEARTBEAT) 
					processHeartBeat(ctx, message);
			
			if (message.getMessageType() == Packet.Type.DATA) 
					processData(ctx, message);
		
			if (message.getMessageType() == Packet.Type.UPDATE) 
					processUpdate(ctx, message);
			
			if (message.getMessageType() == Packet.Type.CLIENTREQUEST) 
					processClientRequest(ctx, message);
		

			if (message.getMessageType() == Packet.Type.COMMITREQUEST) 
					processCommitRequest(ctx, message);
			
			if (message.getMessageType() == Packet.Type.ELECTION) 
					processElection(ctx,message);	
			
			
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public void processCommitRequest(ChannelHandlerContext ctx, Packet message)
	{
		for(NodeLog nodeLog:configuration.getNodeLog())
		{
			if(nodeLog.getCommitIndex()==message.getCommitIndex())
			{
				nodeLog.setCommitted(true);
				session.save(nodeLog);
				session.getTransaction().commit();
				System.out.println("Log Committed:");
				System.out.println("TermId:"+nodeLog.getTermId()+"||"+"Commit Index:"+nodeLog.getCommitIndex()+"||Leader:"+nodeLog.getLeader()+"||Data:"+nodeLog.getCourse_description());
				configuration.setLastCommitIndex(nodeLog.getCommitIndex());
			}
		}
	}

	public void processHeartBeat(ChannelHandlerContext ctx, Packet message)
	{
		System.out.println("Current Term:"+configuration.getCurrentTerm());
			if(message.getTermId()>configuration.getCurrentTerm())
			{
				configuration.setState(Configuration.State.FOLLOWER);
				configuration.setCurrentTerm(message.getTermId());
				//Update Leader
				configuration.getLeader().setHost(message.getHost());
				configuration.getLeader().setPort(message.getPort());
			}
			if(configuration.getState()!=Configuration.State.LEADER)
			{
				configuration.restartExecutor();
				configuration.getExecutor().execute(new ElectionManager(configuration));
			}
			try 
			{
				synchronized(configuration) {
					configuration.setState(Configuration.State.FOLLOWER);
				}
				Message.Packet.Builder packet = Message.Packet.newBuilder(configuration.getPacket().build());
				packet.setMessageType(Packet.Type.HEARTBEAT);
				packet.setCommitIndex(configuration.getLastCommitIndex());
				ctx.writeAndFlush(packet);
				System.out.println("HeartBeat Received From :"+message.getHost()+":"+message.getPort()+"! Restarted Election Manager");
				if(!configuration.getNodeLog().isEmpty()) {
					System.out.println("*********************NODELOG INFO **************************************************");
					for(NodeLog nodeLog:configuration.getNodeLog()) {
						System.out.println("Commit Index "+nodeLog.getCommitIndex()+" "+"Course Name "+nodeLog.getCourse_name());
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
	}
	
	public void processElection(ChannelHandlerContext ctx, Packet message)
	{
		if(configuration.getState()!=Configuration.State.LEADER)
		{
			configuration.restartExecutor();
			configuration.getExecutor().execute(new ElectionManager(configuration));
			System.out.println("Restarted Election Manager Due to Vote Request!");
		}
			
		try {
			Packet.Builder packet=Message.Packet.newBuilder(configuration.getPacket().build());
			if(message.getTermId()>configuration.getCurrentTerm() && configuration.getState()==Configuration.State.FOLLOWER && configuration.getLastVotedTerm()<message.getTermId())
			{
				packet.setVote(Packet.Vote.GRANTED);
				configuration.setLastVotedTerm(message.getTermId());
				packet.setTermId(message.getTermId());
			}
			else
			{
				System.out.println("Current Term:"+configuration.getCurrentTerm());
				System.out.println("Message Term:"+message.getTermId());
				System.out.println("Last Voted Term:"+configuration.getLastVotedTerm());
				System.out.println("State:"+configuration.getState());
				packet.setVote(Packet.Vote.DENIED);
			}
				ctx.writeAndFlush(packet);
		} catch (Exception e) {
			// TODO: handle exception
		}	
	}
	
	public void processData(ChannelHandlerContext ctx, Packet message)
	{
		System.out.println("*****************************RECEIVED DATA FROM LEADER****************************");
			NodeLog nodeLog=new NodeLog();
			nodeLog.setTermId(message.getTermId());
			nodeLog.setLeader(message.getHost()+":"+message.getPort());
			nodeLog.setCommitIndex(message.getCommitIndex());
			nodeLog.setUserId(message.getDetails().getUserId());
			nodeLog.setUsername(message.getDetails().getUsername());
			nodeLog.setCourse_id(message.getDetails().getCourseId());
			nodeLog.setCourse_description(message.getDetails().getCourseDescription());
			nodeLog.setCourse_name(message.getDetails().getCourseName());
			configuration.getNodeLog().add(nodeLog);
			ctx.writeAndFlush(configuration.getPacket().setCommitIndex(message.getCommitIndex()));

	}
	
	public void processUpdate(ChannelHandlerContext ctx, Packet message)
	{
		try {
				System.out.println("Processing Update......");
				NodeLog log= new  NodeLog();
				log.setCommitIndex(message.getCommitIndex());
				log.setTermId(message.getTermId());
				log.setCourse_description(message.getDetails().getCourseDescription());
				log.setCourse_id(message.getDetails().getCourseId());
				log.setCourse_name(message.getDetails().getCourseName());
				log.setCommitted(true);
				log.setAck(true);
				log.setLeader(message.getHost()+":"+message.getPort());
				log.setUsername(message.getDetails().getUsername());
				log.setUserId(message.getDetails().getUserId());
				session.save(log);
				session.getTransaction().commit();
				configuration.setLastCommitIndex(message.getCommitIndex());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}

	}
	@SuppressWarnings("unchecked")
	public void processClientRequest(ChannelHandlerContext ctx, Packet message)
	{
		Packet.Builder packet=Packet.newBuilder(configuration.getPacket().build());
		System.out.println("Request from "+message.getHost()+":"+message.getPort()+" Recorded by:- "+configuration.getHost()+":"+configuration.getPort()+"\tNode State: "+configuration.getState());

		if(message.getFunctionalities().equals(Packet.Functionalities.ADDCOURSETOUSER) || message.getFunctionalities().equals(Packet.Functionalities.ADDUSER) || message.getFunctionalities().equals(Packet.Functionalities.ADDCOURSE)) {
			packet.setClientResponse("Request from "+message.getHost()+":"+message.getPort()+" Recorded by:- "+configuration.getHost()+":"+configuration.getPort()+"\tNode State: "+configuration.getState());
			configuration.getDetails().add(message.getDetails());
			if(configuration.getState()==Configuration.State.LEADER)
			{
				new ReplicationClient(configuration, Packet.newBuilder(message));
			}
			else
			{
				packet.setMessageType(Type.CLIENTREQUEST);
				packet.setDetails(message.getDetails());
				packet.setTermId(configuration.getCurrentTerm());
				packet.setFunctionalities(message.getFunctionalities());
				new DataClient(configuration, packet, configuration.getLeader());
			}
		} else if(message.getFunctionalities().equals(Packet.Functionalities.GETCOURSEDESCRIPTION)) {
			String courseId = message.getDetails().getCourseId();
			List<NodeLog> nodeLogList = session.createQuery("From NodeLog n where n.course_id= '"+courseId+"'").list();
			if(!nodeLogList.isEmpty())
				packet.setClientResponse("CourseId: "+courseId+"Course Name: "+nodeLogList.get(0).getCourse_name()+" Course Description: "+nodeLogList.get(0).getCourse_description());
			else
				packet.setClientResponse("No data found");
		} else if(message.getFunctionalities().equals(Packet.Functionalities.GETUSER)) {
			Integer userId = message.getDetails().getUserId();
			System.out.println(userId);
			List<NodeLog> nodeLogList = session.createQuery("From NodeLog n where n.userId= "+userId).list();
			System.out.println("Username is "+nodeLogList.get(0).getUsername());
			if(!nodeLogList.isEmpty())
				packet.setClientResponse("UserId: "+userId+"User Name: "+nodeLogList.get(0).getUsername());
			else
				packet.setClientResponse("No data found");
		}
		try{
			//send response to client
			ctx.writeAndFlush(packet);
		}
		catch(Exception e){}
		}
	
	
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		cause.printStackTrace();
		ctx.close();
	}
}
