package com.example.demo.service;

/**
 * The three steps of the forgot-password flow.
 *
 * <p>Deliberate design point: {@link #requestOtp} never reveals whether the
 * email belongs to an account. It behaves identically either way, so the
 * endpoint cannot be used to discover who has an account here.
 */
public interface PasswordResetService {

	/**
	 * Issues a code and mails it, if - and only if - the address belongs to an
	 * account. Returns normally in both cases.
	 *
	 * @throws com.example.demo.exception.ValidationException if a code was requested too recently
	 */
	void requestOtp(String email);

	/**
	 * Checks a code and, on success, issues a short-lived single-use token for
	 * the final step.
	 *
	 * @return the reset token
	 * @throws com.example.demo.exception.ValidationException if the code is wrong, expired or exhausted
	 */
	String verifyOtp(String email, String otp);

	/**
	 * Sets the new password, consuming the reset token.
	 *
	 * @throws com.example.demo.exception.ValidationException if the token is unknown, expired or spent
	 */
	void resetPassword(String email, String resetToken, String newPassword);
}
