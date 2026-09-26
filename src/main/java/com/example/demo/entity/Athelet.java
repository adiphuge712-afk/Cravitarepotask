package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
@Entity
@Table(name="athelet")
public class Athelet {

	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long athid;
	@Column
	private String name;
	@Column(unique=true)
	private String email;
	// WRITE_ONLY: a password may be sent in, but is never serialized out - not
	// into a login response, not into a listing, and not into the JWT claims.
	@JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
	@Column
	private String password;
	@Column
	private long age;
	@Column
	private String sporttype;
	@Column
	private String role;
	
	public Athelet() {
		super();
		this.role = "ATHELET";
	}
	
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}

	@ManyToOne
	
	@JoinColumn(name = "coachid")
	private Coach coachid;
	public long getAthid() {
		return athid;
	}
	public void setAthid(long athid) {
		this.athid = athid;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
	public long getAge() {
		return age;
	}
	public void setAge(long age) {
		this.age = age;
	}
	public String getSporttype() {
		return sporttype;
	}
	public void setSporttype(String sporttype) {
		this.sporttype = sporttype;
	}
	public Coach getCoachid() {
		return coachid;
	}
	public void setCoachid(Coach coachid) {
		this.coachid = coachid;
	}
	
}
