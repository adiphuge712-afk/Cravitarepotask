package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Inbound payload for self-service athlete registration.
 * Deliberately has no "role" field: role is a server-assigned trust
 * decision, never something a client can hand us on the wire.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AthleteRegistrationRequest {

	private String name;
	private String email;
	private String password;
	private long age;
	private String sporttype;

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
}
