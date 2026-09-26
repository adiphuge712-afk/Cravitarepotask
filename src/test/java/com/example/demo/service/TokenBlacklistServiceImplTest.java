package com.example.demo.service;

import com.example.demo.entity.RevokedToken;
import com.example.demo.repository.RevokedTokenRepo;
import com.example.demo.security.jwtutil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the piece that makes logout actually take effect on an otherwise
 * stateless JWT: revoking a specific token before its natural expiry, and
 * being able to answer "has this token been revoked?" on every request.
 */
@ExtendWith(MockitoExtension.class)
class TokenBlacklistServiceImplTest {

	private static final String TOKEN = "header.payload.signature";

	@Mock
	private RevokedTokenRepo revokedTokenRepo;
	@Mock
	private jwtutil jwtUtil;

	@InjectMocks
	private TokenBlacklistServiceImpl service;

	@Nested
	@DisplayName("revoke")
	class Revoke {

		@Test
		@DisplayName("positive: stores a hash of the token (never the token itself) with its real expiry")
		void storesHashedTokenWithExpiry() {
			Date expiry = Date.from(Instant.now().plusSeconds(3600));
			when(jwtUtil.extractExpiration(TOKEN)).thenReturn(expiry);
			when(revokedTokenRepo.existsByTokenHash(anyString())).thenReturn(false);

			service.revoke(TOKEN);

			ArgumentCaptor<RevokedToken> captor = ArgumentCaptor.forClass(RevokedToken.class);
			verify(revokedTokenRepo).save(captor.capture());
			RevokedToken saved = captor.getValue();

			assertThat(saved.getTokenHash())
					.isNotEqualTo(TOKEN)
					.hasSize(64); // hex-encoded SHA-256
			assertThat(saved.getExpiresAt()).isEqualTo(expiry.toInstant());
		}

		@Test
		@DisplayName("positive: sweeps rows past their own expiry before inserting the new one")
		void sweepsExpiredRowsFirst() {
			when(jwtUtil.extractExpiration(TOKEN)).thenReturn(new Date());
			when(revokedTokenRepo.existsByTokenHash(anyString())).thenReturn(false);

			service.revoke(TOKEN);

			verify(revokedTokenRepo).deleteExpiredBefore(any(Instant.class));
		}

		@Test
		@DisplayName("positive: revoking the same token twice is a no-op the second time, not an error")
		void idempotentOnRepeatCalls() {
			// Already-revoked short-circuits before extractExpiration is ever
			// called, so no stub for it is needed here.
			when(revokedTokenRepo.existsByTokenHash(anyString())).thenReturn(true);

			assertThatCode(() -> service.revoke(TOKEN)).doesNotThrowAnyException();

			verify(revokedTokenRepo, never()).save(any(RevokedToken.class));
		}

		@Test
		@DisplayName("positive: a concurrent revoke of the same token (unique constraint race) does not surface as an error")
		void concurrentRevokeRaceIsSwallowed() {
			when(jwtUtil.extractExpiration(TOKEN)).thenReturn(new Date());
			when(revokedTokenRepo.existsByTokenHash(anyString())).thenReturn(false);
			doThrow(new DataIntegrityViolationException("duplicate"))
					.when(revokedTokenRepo).save(any(RevokedToken.class));

			assertThatCode(() -> service.revoke(TOKEN)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("isRevoked")
	class IsRevoked {

		@Test
		@DisplayName("positive: true when a row with this token's hash exists")
		void trueWhenRevoked() {
			when(revokedTokenRepo.existsByTokenHash(anyString())).thenReturn(true);

			assertThat(service.isRevoked(TOKEN)).isTrue();
		}

		@Test
		@DisplayName("negative: false when no such row exists")
		void falseWhenNotRevoked() {
			when(revokedTokenRepo.existsByTokenHash(anyString())).thenReturn(false);

			assertThat(service.isRevoked(TOKEN)).isFalse();
		}

		@Test
		@DisplayName("positive: the same token always hashes to the same lookup key")
		void hashIsDeterministic() {
			service.isRevoked(TOKEN);
			service.isRevoked(TOKEN);

			ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
			verify(revokedTokenRepo, times(2)).existsByTokenHash(captor.capture());
			assertThat(captor.getAllValues().get(0)).isEqualTo(captor.getAllValues().get(1));
		}
	}
}
