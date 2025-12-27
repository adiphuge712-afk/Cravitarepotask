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
@Table(name="performancelog")
public class Performancelog {
	
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long logid;
	@Column
	private LocalDate date ;
	@Column
	private String performancematrix;
	@Column
	private String fatiquelevel;
	
	@ManyToOne
	@JoinColumn(name = "athid")
	private Athelet athid;
	@ManyToOne
	@JoinColumn(name = "workid")
	private Workdirl workid;
	public long getLogid() {
		return logid;
	}
	public void setLogid(long logid) {
		this.logid = logid;
	}
	public LocalDate getDate() {
		return date;
	}
	public void setDate(LocalDate date) {
		this.date = date;
	}
	public String getPerformancematrix() {
		return performancematrix;
	}
	public void setPerformancematrix(String performancematrix) {
		this.performancematrix = performancematrix;
	}
	public String getFatiquelevel() {
		return fatiquelevel;
	}
	public void setFatiquelevel(String fatiquelevel) {
		this.fatiquelevel = fatiquelevel;
	}
	public Athelet getAthid() {
		return athid;
	}
	public void setAthid(Athelet athid) {
		this.athid = athid;
	}
	public Workdirl getWorkid() {
		return workid;
	}
	public void setWorkid(Workdirl workid) {
		this.workid = workid;
	}
	
	
	
}
