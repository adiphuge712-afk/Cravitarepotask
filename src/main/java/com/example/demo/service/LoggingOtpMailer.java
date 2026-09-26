package com.example.demo.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Local-development fallback: writes the code to the application log instead
 * of sending mail. Active when {@code app.mail.enabled} is false or absent,
 * so the reset flow is testable on a machine with no SMTP server.
 *
 * <p>This must never be the active mailer in a deployed environment - it
 * prints a live credential to the logs, which is exactly what the hashing in
 * {@code PasswordResetOtp} exists to avoid. It warns loudly for that reason.
 */
@Service
@ConditionalOnProperty(name = "app.mail.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingOtpMailer implements OtpMailer {

	private static final Logger log = LoggerFactory.getLogger(LoggingOtpMailer.class);

	@Override
	public void sendOtp(String to, String name, String otp, int validMinutes) {
		log.warn("""

				=========================================================
				 MAIL DISABLED - password reset code not actually sent
				 to      : {}
				 name    : {}
				 code    : {}
				 expires : {} minutes
				 Set app.mail.enabled=true plus the spring.mail.* settings
				 to deliver this over SMTP instead.
				=========================================================
				""", to, name, otp, validMinutes);
	}
}
