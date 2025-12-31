package com.example.demo;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="feedback")
public class Feedback {

	

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long feedid;
	@Column
	private String comment;
	@Column
	private String difficultlevel;
	@ManyToOne
	@JoinColumn(name = "athid")
	private Athelet athid;
	public long getFeedid() {
		return feedid;
	}
	public void setFeedid(long feedid) {
		this.feedid = feedid;
	}
	public String getComment() {
		return comment;
	}
	public void setComment(String comment) {
		this.comment = comment;
	}
	public String getDifficultlevel() {
		return difficultlevel;
	}
	public void setDifficultlevel(String difficultlevel) {
		this.difficultlevel = difficultlevel;
	}
	public Athelet getAthid() {
		return athid;
	}
	public void setAthid(Athelet athid) {
		this.athid = athid;
	}

	
}
