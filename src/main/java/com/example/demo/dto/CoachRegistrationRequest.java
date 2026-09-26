package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Inbound payload for coach registration (performed by an admin, or by
 * another coach where permitted). Deliberately has no "role" field: role
 * is a server-assigned trust decision, never something a client can hand
 * us on the wire.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class CoachRegistrationRequest {

	private String name;
	private long age;
	private String email;
	private String password;
	private String specialization;
	private long experience;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getAge() {
		return age;
	}

	public void setAge(long age) {
		this.age = age;
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

	public String getSpecialization() {
		return specialization;
	}

	public void setSpecialization(String specialization) {
		this.specialization = specialization;
	}

	public long getExperience() {
		return experience;
	}

	public void setExperience(long experience) {
		this.experience = experience;
	}
}
