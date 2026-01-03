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
@Table(name="requestcoach")
public class Requestforacoach {
	@Id
	 @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long rqid;
	@Column
	private String request;
	@ManyToOne
	@JoinColumn(name="athid")
	private Athelet athid;
	public long getRqid() {
		return rqid;
	}
	public void setRqid(long rqid) {
		this.rqid = rqid;
	}
	public String getRequest() {
		return request;
	}
	public void setRequest(String request) {
		this.request = request;
	}
	public Athelet getAthid() {
		return athid;
	}
	public void setAthid(Athelet athid) {
		this.athid = athid;
	}
	
	
}
