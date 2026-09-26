package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import com.example.demo.entity.PasswordResetOtp;
import com.example.demo.exception.ValidationException;
import com.example.demo.repository.PasswordResetOtpRepo;

@Service
public class PasswordResetServiceImpl implements PasswordResetService {

	private static final Logger log = LoggerFactory.getLogger(PasswordResetServiceImpl.class);

	static final int OTP_VALID_MINUTES = 10;
	static final int RESET_TOKEN_VALID_MINUTES = 15;
	static final int MAX_ATTEMPTS = 5;
	static final Duration RESEND_COOLDOWN = Duration.ofSeconds(60);

	private static final int MIN_PASSWORD_LENGTH = 6;

	private final PasswordResetOtpRepo otpRepo;
	private final AccountDirectory accounts;
	private final OtpMailer mailer;
	private final SecureRandom random = new SecureRandom();

	public PasswordResetServiceImpl(PasswordResetOtpRepo otpRepo, AccountDirectory accounts, OtpMailer mailer) {
		this.otpRepo = otpRepo;
		this.accounts = accounts;
		this.mailer = mailer;
	}

	@Override
	public void requestOtp(String email) {
		String normalised = normalise(email);
		if (normalised.isBlank()) {
			throw new ValidationException("Email is required");
		}

		// The cooldown is enforced before the existence check so its timing does
		// not differ between real and unknown addresses.
		otpRepo.findTopByEmailOrderByCreatedAtDesc(normalised).ifPresent(last -> {
			if (last.getCreatedAt().plus(RESEND_COOLDOWN).isAfter(Instant.now())) {
				throw new ValidationException("A code was just sent. Please wait a minute before asking for another.");
			}
		});

		if (!accounts.exists(normalised)) {
			// Return normally. Saying "no such account" here would turn this
			// endpoint into a way to enumerate who is registered.
			log.info("Password reset requested for an address with no account; no mail sent.");
			return;
		}

		otpRepo.invalidateOutstanding(normalised);

		String otp = generateOtp();
		PasswordResetOtp record = new PasswordResetOtp();
		record.setEmail(normalised);
		record.setOtpHash(sha256(otp));
		record.setExpiresAt(Instant.now().plus(Duration.ofMinutes(OTP_VALID_MINUTES)));
		otpRepo.save(record);

		String name = accounts.displayName(normalised).orElse("there");
		mailer.sendOtp(normalised, name, otp, OTP_VALID_MINUTES);
	}

	@Override
	public String verifyOtp(String email, String otp) {
		String normalised = normalise(email);
		if (normalised.isBlank() || otp == null || otp.isBlank()) {
			throw new ValidationException("Email and code are both required");
		}

		PasswordResetOtp record = otpRepo.findTopByEmailOrderByCreatedAtDesc(normalised)
				.filter(PasswordResetOtp::isRedeemable)
				.orElseThrow(() -> new ValidationException("That code is no longer valid. Please request a new one."));

		if (record.getAttempts() >= MAX_ATTEMPTS) {
			record.setUsed(true);
			otpRepo.save(record);
			throw new ValidationException("Too many incorrect attempts. Please request a new code.");
		}

		// Constant-time comparison: a byte-by-byte early exit would leak how much
		// of the code was correct.
		boolean matches = MessageDigest.isEqual(
				sha256(otp.trim()).getBytes(StandardCharsets.UTF_8),
				record.getOtpHash().getBytes(StandardCharsets.UTF_8));

		if (!matches) {
			record.setAttempts(record.getAttempts() + 1);
			otpRepo.save(record);
			int left = MAX_ATTEMPTS - record.getAttempts();
			throw new ValidationException(left > 0
					? "Incorrect code. " + left + " attempt" + (left == 1 ? "" : "s") + " remaining."
					: "Too many incorrect attempts. Please request a new code.");
		}

		String token = generateResetToken();
		record.setVerified(true);
		record.setResetToken(token);
		record.setResetTokenExpiresAt(Instant.now().plus(Duration.ofMinutes(RESET_TOKEN_VALID_MINUTES)));
		otpRepo.save(record);

		return token;
	}

	@Override
	public void resetPassword(String email, String resetToken, String newPassword) {
		String normalised = normalise(email);
		if (normalised.isBlank() || resetToken == null || resetToken.isBlank()) {
			throw new ValidationException("Email and reset token are both required");
		}
		if (newPassword == null || newPassword.trim().length() < MIN_PASSWORD_LENGTH) {
			throw new ValidationException("Password must be at least " + MIN_PASSWORD_LENGTH + " characters");
		}

		Optional<PasswordResetOtp> found = otpRepo.findByEmailAndResetToken(normalised, resetToken);
		PasswordResetOtp record = found
				.orElseThrow(() -> new ValidationException("This reset link is not valid. Please start again."));

		if (record.isUsed() || !record.isVerified() || record.isResetTokenExpired()) {
			throw new ValidationException("This reset link has expired or was already used. Please start again.");
		}

		// NOTE: stored as-is, because the rest of the application still compares
		// passwords in plain text (NoOpPasswordEncoder in SecurityConfig).
		// When that is swapped for BCrypt this single line is the only place in
		// the reset flow that has to change.
		if (!accounts.updatePassword(normalised, newPassword.trim())) {
			throw new ValidationException("No account found for that email address");
		}

		record.setUsed(true);
		record.setResetToken(null);
		otpRepo.save(record);
	}

	private String normalise(String email) {
		return email == null ? "" : email.trim().toLowerCase();
	}

	/** Six digits, uniformly distributed, from a cryptographic source. */
	private String generateOtp() {
		return String.format("%06d", random.nextInt(1_000_000));
	}

	private String generateResetToken() {
		byte[] buf = new byte[32];
		random.nextBytes(buf);
		return Base64.getUrlEncoder().withoutPadding().encodeToString(buf);
	}

	private String sha256(String value) {
		try {
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			return HexFormat.of().formatHex(digest.digest(value.getBytes(StandardCharsets.UTF_8)));
		} catch (NoSuchAlgorithmException e) {
			throw new IllegalStateException("SHA-256 unavailable", e);
		}
	}
}
