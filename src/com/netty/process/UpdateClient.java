package com.netty.process;

import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import com.netty.hibernate.HibernateUtil;
import com.netty.pojo.Configuration;
import com.netty.pojo.Node;
import com.netty.pojo.NodeLog;
import com.netty.proto.Message;
import com.netty.proto.Message.Packet;

public class UpdateClient implements Runnable{
	
	Configuration configuration;
	Packet packet;
	public UpdateClient(Configuration configuration,Packet packet){
		this.configuration=configuration;
		this.packet=packet;
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		int commitIndex=packet.getCommitIndex();		
		try {
			SessionFactory sessionFactory = HibernateUtil.getSessionFactory();
			Session session = sessionFactory.openSession();
			List<NodeLog> list = session.createQuery("from NodeLog N where N.commitIndex>"+commitIndex+" ORDER BY N.commitIndex ASC" ).list();
			Packet.Builder pack=configuration.getPacket();
			Node dest=new Node(packet.getHost(), packet.getPort());
			for(NodeLog log : list){        	
				pack.setCommitIndex(log.getCommitIndex());
				Message.Packet.Details.Builder details =Message.Packet.Details.newBuilder();
				details.setCourseDescription(log.getCourse_description());
				details.setCourseId(log.getCourse_id());
				details.setCourseName(log.getCourse_name());
				details.setUserId(log.getUserId());
				details.setUsername(log.getUsername());
				pack.setDetails(details);
				pack.setMessageType(Message.Packet.Type.UPDATE);
				pack.setTermId(log.getTermId());
				new DataClient(configuration, pack, dest);
			}
			removefromUpdatingNodes(packet.getHost(), packet.getPort());
		} catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
			removefromUpdatingNodes(packet.getHost(), packet.getPort());
		}
	}
	
	
	
	public void removefromUpdatingNodes(String host, int port)
	{
		for(Node node:configuration.getNodes())
		{
			if(node.getHost().equals(host) && node.getPort()==port)
			{
				configuration.getNodes().remove(node);
				return;
			}
		}
	}
	
}
