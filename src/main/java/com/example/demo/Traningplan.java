package com.example.demo;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name="traingplan")
public class Traningplan {
	
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long planid;
	@Column
	private String planname;
	@Column
	private String plantype;
	@Column
	private LocalDate startdate;
	@Column
	private LocalDate enddate;
	@ManyToOne
	@JoinColumn(name = "coachid")
	private Coach coachid;
	
	
	public long getPlanid() {
		return planid;
	}
	public void setPlanid(long planid) {
		this.planid = planid;
	}
	public String getPlanname() {
		return planname;
	}
	public void setPlanname(String planname) {
		this.planname = planname;
	}
	public String getPlantype() {
		return plantype;
	}
	public void setPlantype(String plantype) {
		this.plantype = plantype;
	}
	public LocalDate getStartdate() {
		return startdate;
	}
	public void setStartdate(LocalDate startdate) {
		this.startdate = startdate;
	}
	public LocalDate getEnddate() {
		return enddate;
	}
	public void setEnddate(LocalDate enddate) {
		this.enddate = enddate;
	}
	public Coach getCoachid() {
		return coachid;
	}
	public void setCoachid(Coach coachid) {
		this.coachid = coachid;
	}
	
	
	

}
