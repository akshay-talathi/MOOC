package com.netty.pojo;

public class Election {

	int termId;
	Node votedFor;

	public int getTermId() {
		return termId;
	}

	public void setTermId(int termId) {
		this.termId = termId;
	}

	public Node getVotedFor() {
		return votedFor;
	}

	public void setVotedFor(Node votedFor) {
		this.votedFor = votedFor;
	}
}
