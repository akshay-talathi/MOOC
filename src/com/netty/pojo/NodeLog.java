package com.netty.pojo;

import java.io.Serializable;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Transient;

@SuppressWarnings("serial")
@Entity
@Table(name="NodeLog")
public class NodeLog implements Serializable{
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	Integer id;
	int termId;
	int commitIndex;
	@Transient
	boolean ack = false;
	@Transient
	int acks;
	int userId;
	String username;
	String course_id;
	String course_name;
	String course_description;
	@Transient
	String leader;
	@Transient
	boolean isCommitted;

	public int getAcks() {
		return acks;
	}

	public void setAcks(int acks) {
		this.acks = acks;
	}
	
	public boolean isAck() {
		return ack;
	}

	public void setAck(boolean ack) {
		this.ack = ack;
	}
	
	public int getUserId() {
		return userId;
	}

	public void setUserId(int userId) {
		this.userId = userId;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getCourse_id() {
		return course_id;
	}

	public void setCourse_id(String course_id) {
		this.course_id = course_id;
	}

	public String getCourse_name() {
		return course_name;
	}

	public void setCourse_name(String course_name) {
		this.course_name = course_name;
	}

	public String getCourse_description() {
		return course_description;
	}

	public void setCourse_description(String course_description) {
		this.course_description = course_description;
	}

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