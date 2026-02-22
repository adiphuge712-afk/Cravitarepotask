package com.example.demo.DTO;

import com.example.demo.Admin;

public class LoginResponse_admin {

	private Admin admin;
	public Admin getAdmin() {
		return admin;
	}
	public void setAdmin(Admin admin) {
		this.admin = admin;
	}
	public String getToken() {
		return token;
	}
	public void setToken(String token) {
		this.token = token;
	}
	private String token;
}
