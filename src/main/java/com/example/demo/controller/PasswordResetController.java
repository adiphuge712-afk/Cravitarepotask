package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.ForgotPasswordRequest;
import com.example.demo.dto.ResetPasswordRequest;
import com.example.demo.dto.VerifyOtpRequest;
import com.example.demo.service.PasswordResetService;

/**
 * The forgot-password flow: request a code, verify it, set a new password.
 *
 * <p>All three endpoints are public - the caller cannot be authenticated,
 * that is the entire point - so they are listed in SecurityConfig's permitAll
 * block. Errors are raised as exceptions and mapped to status codes by
 * GlobalExceptionHandler, the same as every other controller.
 */
@RestController
@RequestMapping("/auth")
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	public PasswordResetController(PasswordResetService passwordResetService) {
		this.passwordResetService = passwordResetService;
	}

	/**
	 * Step 1. Always answers the same way whether or not the address is
	 * registered, so this cannot be used to find out who has an account.
	 */
	@PostMapping("/forgot-password")
	public ResponseEntity<Map<String, String>> forgotPassword(@RequestBody ForgotPasswordRequest request) {
		passwordResetService.requestOtp(request.getEmail());
		return ResponseEntity.ok(Map.of(
				"message", "If that email is registered, a 6-digit code is on its way. It expires in 10 minutes."));
	}

	/** Step 2. Exchanges a correct code for a short-lived reset token. */
	@PostMapping("/verify-otp")
	public ResponseEntity<Map<String, String>> verifyOtp(@RequestBody VerifyOtpRequest request) {
		String resetToken = passwordResetService.verifyOtp(request.getEmail(), request.getOtp());
		return ResponseEntity.ok(Map.of(
				"message", "Code verified. You can now choose a new password.",
				"resetToken", resetToken));
	}

	/** Step 3. Consumes the reset token and sets the new password. */
	@PostMapping("/reset-password")
	public ResponseEntity<Map<String, String>> resetPassword(@RequestBody ResetPasswordRequest request) {
		passwordResetService.resetPassword(
				request.getEmail(), request.getResetToken(), request.getNewPassword());
		return ResponseEntity.ok(Map.of("message", "Your password has been updated. You can sign in with it now."));
	}
}
