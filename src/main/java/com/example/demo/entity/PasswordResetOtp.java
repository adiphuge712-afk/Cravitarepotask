package com.example.demo.entity;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;

/**
 * One password-reset attempt.
 *
 * <p>The code itself is never stored: only a SHA-256 hash of it, so that a
 * dump of this table cannot be replayed to take over accounts. The row also
 * carries its own expiry, an attempt counter and single-use flags, which is
 * what stops a code being brute-forced or replayed.
 */
@Entity
@Table(name = "password_reset_otp", indexes = @Index(name = "idx_pwreset_email", columnList = "email"))
public class PasswordResetOtp {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false)
	private String email;

	/** SHA-256 of the 6-digit code. Never the code itself. */
	@Column(nullable = false)
	private String otpHash;

	@Column(nullable = false)
	private Instant expiresAt;

	@Column(nullable = false)
	private Instant createdAt;

	/** Wrong guesses so far; the row is burned once this hits the cap. */
	@Column(nullable = false)
	private int attempts;

	/** Set once the correct code has been presented. */
	@Column(nullable = false)
	private boolean verified;

	/** Set once the password has actually been changed - blocks replay. */
	@Column(nullable = false)
	private boolean used;

	/**
	 * Issued on successful verification so the final step does not have to
	 * resend the code. Short-lived and single-use, like the code itself.
	 */
	@Column
	private String resetToken;

	@Column
	private Instant resetTokenExpiresAt;

	public PasswordResetOtp() {
		this.createdAt = Instant.now();
	}

	public boolean isExpired() {
		return Instant.now().isAfter(expiresAt);
	}

	public boolean isResetTokenExpired() {
		return resetTokenExpiresAt == null || Instant.now().isAfter(resetTokenExpiresAt);
	}

	/** A row that can still be used to verify a code. */
	public boolean isRedeemable() {
		return !used && !verified && !isExpired();
	}

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }

	public String getEmail() { return email; }
	public void setEmail(String email) { this.email = email; }

	public String getOtpHash() { return otpHash; }
	public void setOtpHash(String otpHash) { this.otpHash = otpHash; }

	public Instant getExpiresAt() { return expiresAt; }
	public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }

	public Instant getCreatedAt() { return createdAt; }
	public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }

	public int getAttempts() { return attempts; }
	public void setAttempts(int attempts) { this.attempts = attempts; }

	public boolean isVerified() { return verified; }
	public void setVerified(boolean verified) { this.verified = verified; }

	public boolean isUsed() { return used; }
	public void setUsed(boolean used) { this.used = used; }

	public String getResetToken() { return resetToken; }
	public void setResetToken(String resetToken) { this.resetToken = resetToken; }

	public Instant getResetTokenExpiresAt() { return resetTokenExpiresAt; }
	public void setResetTokenExpiresAt(Instant resetTokenExpiresAt) { this.resetTokenExpiresAt = resetTokenExpiresAt; }
}
