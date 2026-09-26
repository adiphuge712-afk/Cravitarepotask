package com.example.demo.dto;

import com.example.demo.entity.Athelet;

public class LoginResponse_athelet {

	private String token;
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	public Athelet getAthelet() {
		return athelet;
	}
	public void setAthelet(Athelet athelet) {
		this.athelet = athelet;
	}
	private Athelet athelet;
	
}
