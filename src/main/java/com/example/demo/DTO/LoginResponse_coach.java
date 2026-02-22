package com.example.demo.DTO;

import com.example.demo.Coach;

public class LoginResponse_coach {

	private Coach coach;
	public Coach getCoach() {
		return coach;
	}
	public void setCoach(Coach coach) {
		this.coach = coach;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	private String token;
	
}
