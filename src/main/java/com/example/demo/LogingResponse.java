package com.example.demo;

public class LogingResponse {
	private String role;
	private String message;
	public LogingResponse(String role, String message) {
		this.role = role;
		this.message = message;
	}
	public String getRole() {
		return role;
	}
	public String getMessage() {
		return message;
	}
	
	
}
