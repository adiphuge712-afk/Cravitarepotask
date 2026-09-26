package com.example.demo.service;

/**
 * Server-side revocation for otherwise-stateless JWTs.
 *
 * <p>A JWT is normally valid until its own {@code exp} claim says so - there
 * is no server state to consult, which is the point of the format. Logout
 * breaks that assumption on purpose: once a user logs out, the exact token
 * they were holding must stop working immediately, not silently keep
 * authenticating anyone who captured a copy of it until the 24-hour expiry
 * arrives on its own.
 */
public interface TokenBlacklistService {

	/** Marks this exact token as unusable, effective immediately. */
	void revoke(String rawToken);

	/** True if this token was revoked before its natural expiry. */
	boolean isRevoked(String rawToken);
}
