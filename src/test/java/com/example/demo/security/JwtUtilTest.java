package com.example.demo.security;

import com.example.demo.entity.Athelet;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JwtUtilTest {

	private static final String SECRET = "unit-test-only-hs256-signing-key-must-be-32-bytes-plus";

	private jwtutil jwtUtil;

	@BeforeEach
	void setUp() {
		jwtUtil = new jwtutil();
		ReflectionTestUtils.setField(jwtUtil, "secret", SECRET);
	}

	@Test
	void generateToken_roundTripsSubjectEmail() {
		String token = jwtUtil.generateToken("athlete@example.com");

		assertThat(jwtUtil.extractEmail(token)).isEqualTo("athlete@example.com");
	}

	@Test
	void validateToken_true_forMatchingUserAndUnexpiredToken() {
		String token = jwtUtil.generateToken("coach@example.com");
		UserDetails user = User.withUsername("coach@example.com")
				.password("irrelevant")
				.authorities("ROLE_COACH")
				.build();

		assertThat(jwtUtil.validateToken(token, user)).isTrue();
	}

	@Test
	void validateToken_false_whenUsernameDoesNotMatchTokenSubject() {
		String token = jwtUtil.generateToken("coach@example.com");
		UserDetails someoneElse = User.withUsername("someone-else@example.com")
				.password("irrelevant")
				.authorities("ROLE_COACH")
				.build();

		assertThat(jwtUtil.validateToken(token, someoneElse)).isFalse();
	}

	@Test
	void extractEmail_throwsExpiredJwtException_forAnExpiredToken() {
		Key signingKey = Keys.hmacShaKeyFor(SECRET.getBytes());
		String expiredToken = Jwts.builder()
				.setSubject("expired@example.com")
				.setIssuedAt(new Date(System.currentTimeMillis() - 2000))
				.setExpiration(new Date(System.currentTimeMillis() - 1000))
				.signWith(signingKey, SignatureAlgorithm.HS256)
				.compact();

		assertThatThrownBy(() -> jwtUtil.extractEmail(expiredToken))
				.isInstanceOf(ExpiredJwtException.class);
	}

	@Test
	void extractEmail_rejectsATokenSignedWithADifferentSecret() {
		Key foreignKey = Keys.hmacShaKeyFor("a-totally-different-key-of-at-least-32-bytes-long".getBytes());
		String foreignToken = Jwts.builder()
				.setSubject("attacker@example.com")
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis() + 60_000))
				.signWith(foreignKey, SignatureAlgorithm.HS256)
				.compact();

		// A token we did not sign must never resolve to a username.
		assertThatThrownBy(() -> jwtUtil.extractEmail(foreignToken))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void extractEmail_rejectsATamperedPayload() {
		String token = jwtUtil.generateToken("victim@example.com");
		String[] parts = token.split("\\.");
		String forgedPayload = Base64.getUrlEncoder().withoutPadding()
				.encodeToString("{\"sub\":\"attacker@example.com\"}".getBytes(StandardCharsets.UTF_8));
		String tampered = parts[0] + "." + forgedPayload + "." + parts[2];

		// The signature no longer matches the swapped payload.
		assertThatThrownBy(() -> jwtUtil.extractEmail(tampered))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void extractEmail_rejectsAMalformedToken() {
		assertThatThrownBy(() -> jwtUtil.extractEmail("this-is-not-a-jwt"))
				.isInstanceOf(JwtException.class);
	}

	@Test
	void extractEmail_rejectsAnEmptyToken() {
		assertThatThrownBy(() -> jwtUtil.extractEmail(""))
				.isInstanceOf(RuntimeException.class);
	}

	@Test
	void validateToken_false_forAnExpiredTokenBelongingToTheRightUser() {
		Key signingKey = Keys.hmacShaKeyFor(SECRET.getBytes());
		String expiredToken = Jwts.builder()
				.setSubject("coach@example.com")
				.setIssuedAt(new Date(System.currentTimeMillis() - 2000))
				.setExpiration(new Date(System.currentTimeMillis() - 1000))
				.signWith(signingKey, SignatureAlgorithm.HS256)
				.compact();
		UserDetails user = User.withUsername("coach@example.com")
				.password("irrelevant")
				.authorities("ROLE_COACH")
				.build();

		// jjwt throws rather than returning false - the filter relies on this to
		// answer 401 "Token Expired", so pin the behaviour down either way.
		assertThatThrownBy(() -> jwtUtil.validateToken(expiredToken, user))
				.isInstanceOf(ExpiredJwtException.class);
	}

	@Test
	void generateTokenforathelet_setsTheAthletesEmailAsSubject() {
		Athelet athlete = new Athelet();
		athlete.setEmail("athlete@example.com");
		athlete.setPassword("secret");

		String token = jwtUtil.generateTokenforathelet(athlete);

		assertThat(jwtUtil.extractEmail(token)).isEqualTo("athlete@example.com");
	}

	@Test
	@Disabled("Documents a known open issue: the whole entity - including the "
			+ "plaintext password - is packed into the 'user' claim, and a JWT "
			+ "payload is only base64-encoded, not encrypted. Enable this test "
			+ "once the password is stripped from the claims.")
	void generateTokenforathelet_mustNotLeakThePasswordIntoTheTokenPayload() {
		Athelet athlete = new Athelet();
		athlete.setEmail("athlete@example.com");
		athlete.setPassword("super-secret-password");

		String token = jwtUtil.generateTokenforathelet(athlete);
		String payload = new String(Base64.getUrlDecoder().decode(token.split("\\.")[1]), StandardCharsets.UTF_8);

		assertThat(payload).doesNotContain("super-secret-password");
	}
}
