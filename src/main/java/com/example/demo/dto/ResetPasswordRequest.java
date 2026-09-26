package com.example.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Inbound payload for the forgot-password flow. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class ResetPasswordRequest {

	private String email;
	private String resetToken;
	private String newPassword;

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getResetToken() { return resetToken; }
	public void setResetToken(String resetToken) { this.resetToken = resetToken; }

	public String getNewPassword() { return newPassword; }
	public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
