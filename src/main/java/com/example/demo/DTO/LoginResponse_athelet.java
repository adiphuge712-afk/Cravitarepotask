package com.example.demo.DTO;

import com.example.demo.Athelet;

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
