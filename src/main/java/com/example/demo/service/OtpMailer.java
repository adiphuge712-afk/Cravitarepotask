package com.example.demo.service;

/**
 * Delivers a one-time code to the account holder.
 *
 * <p>An interface rather than a direct JavaMailSender call so the delivery
 * channel is swappable: SMTP in a deployed environment, a log line locally
 * where no mail server exists, and a stub in tests.
 */
public interface OtpMailer {

	/**
	 * @param to            recipient address
	 * @param name          account holder's name, for the greeting
	 * @param otp           the plain code - this is the only place it is handled in the clear
	 * @param validMinutes  how long the code lasts, so the message can say so
	 */
	void sendOtp(String to, String name, String otp, int validMinutes);
}
