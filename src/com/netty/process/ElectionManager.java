package com.netty.process;

import java.util.Random;

import com.netty.pojo.Configuration;

public class ElectionManager implements Runnable{

	public Configuration configuration;
	
	public ElectionManager(Configuration configuration) {
		this.configuration=configuration;
	}
	
	@Override
	public void run() {
		try {
			int randomtime=new Random().nextInt(10000-5000)+5000;
			Thread.sleep(randomtime);			
			while(configuration.getState()!=Configuration.State.LEADER)
			{
				configuration.setState(Configuration.State.CANDIDATE);
				startElection();
				if(configuration.getState()!=Configuration.State.LEADER)
				{	
					configuration.setState(Configuration.State.FOLLOWER);
					Thread.sleep(new Random().nextInt(10000-5000)+5000);
				}
			}
			//configuration.setState(Configuration.State.LEADER);
			if(configuration.getState()==Configuration.State.LEADER)
				sendHeartBeats();
			
		
		} catch (Exception e) {
			// TODO: handle exception
		}

	}

	public void startElection()
	{	
		//Write Election Code
		ElectionClient client=new ElectionClient(configuration);
		client.requestVotes();
	}
	
	public void sendHeartBeats ()
	{
		try {
			HeartBeatClient client=new HeartBeatClient(configuration);
			client.sendHeartBeats();
		} catch (Exception e) {
			// TODO: handle exception
		}
				//Write Code To Send HeartBeats
	}
}
