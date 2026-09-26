package com.example.demo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

/**
 * Sends the code over SMTP. Active when {@code app.mail.enabled=true}.
 */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "true")
public class SmtpOtpMailer implements OtpMailer {

	private final JavaMailSender mailSender;
	private final String from;
	private final String appName;

	public SmtpOtpMailer(JavaMailSender mailSender,
			@Value("${app.mail.from}") String from,
			@Value("${app.name:Cravita Sports Academy}") String appName) {
		this.mailSender = mailSender;
		this.from = from;
		this.appName = appName;
	}

	@Override
	public void sendOtp(String to, String name, String otp, int validMinutes) {
		SimpleMailMessage message = new SimpleMailMessage();
		message.setFrom(from);
		message.setTo(to);
		message.setSubject(appName + " - your password reset code");
		message.setText("""
				Hi %s,

				Use this code to reset your %s password:

				    %s

				It expires in %d minutes and can only be used once.

				If you did not ask to reset your password you can ignore this
				message - your current password still works and nothing has changed.

				- %s
				""".formatted(name, appName, otp, validMinutes, appName));

		// Any failure propagates: the service turns it into a 502 so the user is
		// told delivery failed, rather than being left waiting for a mail that
		// was never sent.
		mailSender.send(message);
	}
}
