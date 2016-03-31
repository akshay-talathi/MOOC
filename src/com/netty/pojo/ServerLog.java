package com.netty.pojo;

public class ServerLog {
	
	int termId;
	int commitIndex;
	int acknowledgements;
	String data;
	String leader;
	boolean isCommitted;
	public int getTermId() {
		return termId;
	}
	public void setTermId(int termId) {
		this.termId = termId;
	}
	public int getCommitIndex() {
		return commitIndex;
	}
	public void setCommitIndex(int commitIndex) {
		this.commitIndex = commitIndex;
	}
	public int getAcknowledgements() {
		return acknowledgements;
	}
	public void setAcknowledgements(int acknowledgements) {
		this.acknowledgements = acknowledgements;
	}
	public String getData() {
		return data;
	}
	public void setData(String data) {
		this.data = data;
	}
	public boolean isCommitted() {
		return isCommitted;
	}
	public void setCommitted(boolean isCommitted) {
		this.isCommitted = isCommitted;
	}
	public String getLeader() {
		return leader;
	}
	public void setLeader(String leader) {
		this.leader = leader;
	}
}
