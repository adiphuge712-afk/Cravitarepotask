package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Inbound payload for the forgot-password flow. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ForgotPasswordRequest {

	private String email;

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }
}
