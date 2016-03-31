package com.netty.pojo;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.netty.proto.Message;
import com.netty.proto.Message.Packet;

import io.netty.channel.Channel;


public class Configuration {
	private final int threads=2;
	public enum State {
		LEADER, FOLLOWER, CANDIDATE
	};
	State state = State.FOLLOWER;
	ArrayList<Node> nodes=new ArrayList<Node>();
	int currentTerm=0;
	ArrayList<Election> election=new ArrayList<Election>();
	String host;
	int port;
    Node leader=new Node();
    int lastHeartBeat;
    ExecutorService executor=Executors.newFixedThreadPool(threads);
    int votesGranted;
    int votesDenied;
    ArrayList<Channel> heartBeatChannel =new ArrayList<>();
    ArrayList<Channel> electionChannel =new ArrayList<>();
    ArrayList<Channel> replicationChannel=new ArrayList<>();
    ArrayList<Channel> dataChannel=new ArrayList<>();
	Packet.Builder packet;
	int lastVotedTerm=0;
	int lastCommitIndex=0;
	ArrayList<Message.Packet.Details> details = new ArrayList<>();
	ArrayList<ServerLog> serverLog=new ArrayList<>();
	ArrayList<NodeLog> nodeLog=new ArrayList<>();
	ArrayList<Node> updateNode=new ArrayList<>();
	ExecutorService updateExecutor=Executors.newFixedThreadPool(10);
	


	public ExecutorService getUpdateExecutor() {
		return updateExecutor;
	}

	public void setUpdateExecutor(ExecutorService updateExecutor) {
		this.updateExecutor = updateExecutor;
	}

	public ArrayList<Node> getUpdateNode() {
		return updateNode;
	}

	public void setUpdateNode(ArrayList<Node> updateNode) {
		this.updateNode = updateNode;
	}

	public ArrayList<Packet.Details> getDetails() {
		return details;
	}

	public void setDetails(ArrayList<Packet.Details> details) {
		this.details = details;
	}


	
	public ArrayList<Channel> getDataChannel() {
		return dataChannel;
	}

	public void setDataChannel(ArrayList<Channel> dataChannel) {
		this.dataChannel = dataChannel;
	}

	public ArrayList<ServerLog> getServerLog() {
		return serverLog;
	}

	public void setServerLog(ArrayList<ServerLog> serverLog) {
		this.serverLog = serverLog;
	}

	public int getLastCommitIndex() {
		return lastCommitIndex;
	}

	public void setLastCommitIndex(int lastCommitIndex) {
		this.lastCommitIndex = lastCommitIndex;
	}

	public ArrayList<Channel> getReplicationChannel() {
		return replicationChannel;
	}

	public void setReplicationChannel(ArrayList<Channel> replicationChannel) {
		this.replicationChannel = replicationChannel;
	}
	
    public ArrayList<NodeLog> getNodeLog() {
		return nodeLog;
	}

	public void setNodeLog(ArrayList<NodeLog> nodeLog) {
		this.nodeLog = nodeLog;
	}

	public int getLastVotedTerm() {
		return lastVotedTerm;
	}

	public void setLastVotedTerm(int lastVotedTerm) {
		this.lastVotedTerm = lastVotedTerm;
	}

	public ArrayList<Channel> getHeartBeatChannel() {
		return heartBeatChannel;
	}

	public void setHeartBeatChannel(ArrayList<Channel> heartBeatChannel) {
		this.heartBeatChannel = heartBeatChannel;
	}
	
    public Packet.Builder getPacket() {
		return packet;
	}

	public void setPacket(Packet.Builder packet) {
		this.packet = packet;
	}

	public ArrayList<Channel> getElectionChannel() {
		return electionChannel;
	}

	public void setElectionChannel(ArrayList<Channel> electionChannel) {
		this.electionChannel = electionChannel;
	}

	public ExecutorService getExecutor() {
		return executor;
	}

	public void setExecutor(ExecutorService executor) {
		this.executor = executor;
	}

	public int getVotesGranted() {
		return votesGranted;
	}

	public void setVotesGranted(int votesGranted) {
		this.votesGranted = votesGranted;
	}

	public int getVotesDenied() {
		return votesDenied;
	}

	public void setVotesDenied(int votesDenied) {
		this.votesDenied = votesDenied;
	}

	public int getLastHeartBeat() {
		return lastHeartBeat;
	}

	public void setLastHeartBeat(int lastHeartBeat) {
		this.lastHeartBeat = lastHeartBeat;
	}

	public int getThreads() {
		return threads;
	}

	public void restartExecutor()
	{
		executor.shutdownNow();
		executor=Executors.newFixedThreadPool(threads);	
	}
	public void stopExecutor()
	{
		executor.shutdownNow();
	}
	public Node getLeader() {
		return leader;
	}

	public void setLeader(Node leader) {
		this.leader = leader;
	}

	public int getCurrentTerm() {
		return currentTerm;
	}

	public void setCurrentTerm(int currentTerm) {
		this.currentTerm = currentTerm;
	}

	public ArrayList<Election> getElection() {
		return election;
	}

	public void setElection(ArrayList<Election> election) {
		this.election = election;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public State getState() {
		return state;
	}

	public void setState(State state) {
		this.state = state;
	}

	public ArrayList<Node> getNodes() {
		return nodes;
	}

	public void setNodes(ArrayList<Node> nodes) {
		this.nodes = nodes;
	}	
}
