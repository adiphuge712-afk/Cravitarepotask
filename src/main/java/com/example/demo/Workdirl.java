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
@Table(name="workdiril")
public class Workdirl {

	
	
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long workid;
	@Column
	private String workname ;
	@Column
	private long duration;
	@Column
	private String intencity;
	
	@ManyToOne
	@JoinColumn(name = "planid")
	private Traningplan plan;

	public long getWorkid() {
		return workid;
	}

	public void setWorkid(long workid) {
		this.workid = workid;
	}

	public String getWorkname() {
		return workname;
	}

	public void setWorkname(String workname) {
		this.workname = workname;
	}

	public long getDuration() {
		return duration;
	}

	public void setDuration(long duration) {
		this.duration = duration;
	}

	public String getIntencity() {
		return intencity;
	}

	public void setIntencity(String intencity) {
		this.intencity = intencity;
	}

	public Traningplan getPlan() {
		return plan;
	}

	public void setPlan(Traningplan plan) {
		this.plan = plan;
	}
	
	
	
}
