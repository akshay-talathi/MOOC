package com.netty.pojo;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="NOdLog")
public class NodLog {
	@Id @GeneratedValue(strategy=GenerationType.IDENTITY)
	Integer id;
	@Column(name="termId")
	int termId;
	@Column(name="commitIndex")
	int commitIndex;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
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
	
}
