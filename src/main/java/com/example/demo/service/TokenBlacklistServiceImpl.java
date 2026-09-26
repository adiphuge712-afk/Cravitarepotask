package com.example.demo.service;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.example.demo.entity.RevokedToken;
import com.example.demo.repository.RevokedTokenRepo;
import com.example.demo.security.jwtutil;

@Service
public class TokenBlacklistServiceImpl implements TokenBlacklistService {

	private final RevokedTokenRepo revokedTokenRepo;
	private final jwtutil jwtUtil;

	public TokenBlacklistServiceImpl(RevokedTokenRepo revokedTokenRepo, jwtutil jwtUtil) {
		this.revokedTokenRepo = revokedTokenRepo;
		this.jwtUtil = jwtUtil;
	}

	@Override
	public void revoke(String rawToken) {
		String hash = sha256(rawToken);

		// Sweep anything already past its own expiry first - those rows can
		// never match a still-presentable token again, so there is no reason
		// to let this table grow forever.
		revokedTokenRepo.deleteExpiredBefore(Instant.now());

		if (revokedTokenRepo.existsByTokenHash(hash)) {
			return; // already revoked - logging out twice is not an error
		}

		RevokedToken revoked = new RevokedToken();
		revoked.setTokenHash(hash);
		revoked.setExpiresAt(jwtUtil.extractExpiration(rawToken).toInstant());

		try {
			revokedTokenRepo.save(revoked);
		} catch (DataIntegrityViolationException alreadyRevokedConcurrently) {
			// Another request revoked the same token in the tiny window between
			// the existence check above and this save - the outcome is the
			// same either way, so this is not a failure.
		}
	}

	@Override
	public boolean isRevoked(String rawToken) {
		return revokedTokenRepo.existsByTokenHash(sha256(rawToken));
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
