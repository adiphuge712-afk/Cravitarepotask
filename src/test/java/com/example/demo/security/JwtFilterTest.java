package com.example.demo.security;

import com.example.demo.service.TokenBlacklistService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * The branch that makes logout stick: a token that is well-formed and
 * unexpired must still be rejected once it has been revoked, instead of
 * silently authenticating requests until its 24-hour expiry arrives anyway.
 */
@ExtendWith(MockitoExtension.class)
class JwtFilterTest {

	private static final String TOKEN = "a.b.c";
	private static final String EMAIL = "athlete@example.com";

	@Mock private jwtutil jwtService;
	@Mock private UserDetailsService userDetailsService;
	@Mock private TokenBlacklistService tokenBlacklistService;
	@Mock private SecurityErrorWriter errorWriter;

	@InjectMocks
	private JwtFilter filter;

	private final HttpServletRequest request = mock(HttpServletRequest.class);
	private final HttpServletResponse response = mock(HttpServletResponse.class);
	private final FilterChain chain = mock(FilterChain.class);

	@AfterEach
	void clearSecurityContext() {
		SecurityContextHolder.clearContext();
	}

	private void bearerToken(String token) {
		when(request.getHeader("Authorization")).thenReturn("Bearer " + token);
	}

	@Test
	@DisplayName("positive: no Authorization header - request passes through untouched")
	void noHeaderPassesThrough() throws Exception {
		when(request.getHeader("Authorization")).thenReturn(null);

		filter.doFilterInternal(request, response, chain);

		verify(chain).doFilter(request, response);
		verify(tokenBlacklistService, never()).isRevoked(any());
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("REGRESSION: a well-formed, unexpired, but revoked token is rejected - this is the whole point of the blacklist")
	void revokedTokenIsRejected() throws Exception {
		bearerToken(TOKEN);
		when(jwtService.extractEmail(TOKEN)).thenReturn(EMAIL);
		when(tokenBlacklistService.isRevoked(TOKEN)).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		verify(errorWriter).write(eq(request), eq(response), eq(HttpStatus.UNAUTHORIZED),
				eq("You have been logged out, please log in again"));
		verify(chain, never()).doFilter(any(), any());
		verify(userDetailsService, never()).loadUserByUsername(any());
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNull();
	}

	@Test
	@DisplayName("positive: a valid, non-revoked token authenticates the request")
	void validNonRevokedTokenAuthenticates() throws Exception {
		bearerToken(TOKEN);
		when(jwtService.extractEmail(TOKEN)).thenReturn(EMAIL);
		when(tokenBlacklistService.isRevoked(TOKEN)).thenReturn(false);

		UserDetails principal = User.withUsername(EMAIL).password("irrelevant")
				.authorities(List.<GrantedAuthority>of()).build();
		when(userDetailsService.loadUserByUsername(EMAIL)).thenReturn(principal);
		when(jwtService.validateToken(TOKEN, principal)).thenReturn(true);

		filter.doFilterInternal(request, response, chain);

		verify(chain).doFilter(request, response);
		verify(errorWriter, never()).write(any(), any(), any(), any());
		assertThat(SecurityContextHolder.getContext().getAuthentication()).isNotNull();
		assertThat(SecurityContextHolder.getContext().getAuthentication().getPrincipal()).isEqualTo(principal);
	}

	@Test
	@DisplayName("negative: an expired token is rejected before the blacklist is even checked")
	void expiredTokenRejectedEarly() throws Exception {
		bearerToken(TOKEN);
		when(jwtService.extractEmail(TOKEN)).thenThrow(new ExpiredJwtException(null, null, "expired"));

		filter.doFilterInternal(request, response, chain);

		verify(errorWriter).write(eq(request), eq(response), eq(HttpStatus.UNAUTHORIZED),
				eq("Session expired, please log in again"));
		verify(tokenBlacklistService, never()).isRevoked(any());
		verify(chain, never()).doFilter(any(), any());
	}

	@Test
	@DisplayName("negative: a malformed token is rejected before the blacklist is even checked")
	void malformedTokenRejectedEarly() throws Exception {
		bearerToken("not-a-real-jwt");
		when(jwtService.extractEmail("not-a-real-jwt")).thenThrow(new RuntimeException("malformed"));

		filter.doFilterInternal(request, response, chain);

		verify(errorWriter).write(eq(request), eq(response), eq(HttpStatus.UNAUTHORIZED),
				eq("Invalid authentication token"));
		verify(tokenBlacklistService, never()).isRevoked(any());
		verify(chain, never()).doFilter(any(), any());
	}
}
