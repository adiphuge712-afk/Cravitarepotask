package com.example.demo.security;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.demo.service.TokenBlacklistService;

import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class JwtFilter extends OncePerRequestFilter {

	@Autowired
	private jwtutil jwtService;
	@Autowired
	private UserDetailsService userDetailsService;
	@Autowired
	private TokenBlacklistService tokenBlacklistService;
	@Autowired
	private SecurityErrorWriter errorWriter;

	@Override
	protected void doFilterInternal(HttpServletRequest request,
			HttpServletResponse response,
			FilterChain filterChain)
			throws ServletException, IOException {

		final String authHeader = request.getHeader("Authorization");
		final String jwt;
		final String username;

		if (authHeader == null || !authHeader.startsWith("Bearer ")) {
			filterChain.doFilter(request, response);
			return;
		}

		jwt = authHeader.substring(7);

		try {
			username = jwtService.extractEmail(jwt);
		} catch (ExpiredJwtException e) {
			errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Session expired, please log in again");
			return;
		} catch (Exception e) {
			errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "Invalid authentication token");
			return;
		}

		// A token can be perfectly well-formed and unexpired and still be
		// unusable: this is what makes logout actually take effect immediately
		// instead of the token quietly working until its 24-hour expiry anyway.
		if (tokenBlacklistService.isRevoked(jwt)) {
			errorWriter.write(request, response, HttpStatus.UNAUTHORIZED, "You have been logged out, please log in again");
			return;
		}

		if (username != null &&
				SecurityContextHolder.getContext().getAuthentication() == null) {

			UserDetails userDetails =
					userDetailsService.loadUserByUsername(username);

			if (jwtService.validateToken(jwt, userDetails)) {

				UsernamePasswordAuthenticationToken authToken =
						new UsernamePasswordAuthenticationToken(
								userDetails,
								null,
								userDetails.getAuthorities()
						);

				authToken.setDetails(
						new WebAuthenticationDetailsSource().buildDetails(request));

				SecurityContextHolder.getContext()
						.setAuthentication(authToken);
			}
		}

		filterChain.doFilter(request, response);
	}
}
