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
 * A JWT that has been logged out before its natural expiry.
 *
 * <p>The token itself is never stored - only a SHA-256 hash of it, same
 * reasoning as {@link PasswordResetOtp}: a leak of this table must not hand
 * out live, usable bearer tokens. {@code expiresAt} mirrors the token's own
 * {@code exp} claim, so a row past that point is provably useless (the
 * token would already be rejected as expired even without this table) and
 * safe to sweep away.
 */
@Entity
@Table(name = "revoked_token", indexes = @Index(name = "idx_revoked_token_hash", columnList = "tokenHash", unique = true))
public class RevokedToken {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private long id;

	@Column(nullable = false, unique = true)
	private String tokenHash;

	@Column(nullable = false)
	private Instant expiresAt;

	public long getId() { return id; }
	public void setId(long id) { this.id = id; }

	public String getTokenHash() { return tokenHash; }
	public void setTokenHash(String tokenHash) { this.tokenHash = tokenHash; }

	public Instant getExpiresAt() { return expiresAt; }
	public void setExpiresAt(Instant expiresAt) { this.expiresAt = expiresAt; }
}
