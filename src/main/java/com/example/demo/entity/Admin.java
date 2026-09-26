package com.example.demo.entity;

import com.fasterxml.jackson.annotation.JsonProperty;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


@Entity
@Table(name="admin")
public class Admin {
	@Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
	private long adminid;
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
	private String role;
	
	
	public Admin() {
		super();
		this.role = "ADMIN";
	}
	public String getRole() {
		return role;
	}
	public void setRole(String role) {
		this.role = role;
	}
	public long getAdminid() {
		return adminid;
	}
	public void setAdminid(long adminid) {
		this.adminid = adminid;
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
	
	
	
	
}
