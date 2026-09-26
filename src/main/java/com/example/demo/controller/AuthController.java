package com.example.demo.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse_admin;
import com.example.demo.dto.LoginResponse_athelet;
import com.example.demo.dto.LoginResponse_coach;
import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.security.jwtutil;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.TokenBlacklistService;

/**
 * Authentication actions that are the same whichever role is involved.
 *
 * <p>Signing in and signing out were previously copy-pasted into all three
 * role controllers ({@code /athelet/login}, {@code /admin/login},
 * {@code /coach/login} were byte-identical methods). Neither operation is
 * actually role-specific: login is told which role to check <em>in the
 * request body</em>, and logout just revokes whichever token was presented.
 * Keeping one copy here means a change to how sessions start or end is made
 * once, not three times in three files that can drift apart.
 */
@RestController
@RequestMapping("/auth")
public class AuthController {

	private final AuthenticationManager authenticationManager;
	private final AuthenticationService authenticationService;
	private final jwtutil jwt;
	private final TokenBlacklistService tokenBlacklistService;

	public AuthController(AuthenticationManager authenticationManager,
			AuthenticationService authenticationService,
			jwtutil jwt,
			TokenBlacklistService tokenBlacklistService) {
		this.authenticationManager = authenticationManager;
		this.authenticationService = authenticationService;
		this.jwt = jwt;
		this.tokenBlacklistService = tokenBlacklistService;
	}

	/**
	 * Signs in as whichever role the request names.
	 *
	 * <p>Credentials are verified twice on purpose, and both checks matter:
	 * {@code authenticationManager.authenticate} runs the password through
	 * Spring Security's configured {@code PasswordEncoder} (BCrypt), while
	 * the role-specific lookup afterwards is what fetches the actual entity
	 * to embed in the response, and confirms the account really is of the
	 * role that was claimed - signing in as "Admin" with a coach's
	 * credentials must not succeed.
	 */
	@PostMapping("/login")
	public ResponseEntity<?> login(@RequestBody LoginRequest request) {
		String role = request.getRole();

		// Null-safe on purpose: a request with no role at all used to NPE its
		// way into a generic failure instead of a clear 401.
		if ("Admin".equals(role)) {
			authenticate(request);
			Admin admin = authenticationService.addminsign(request.getEmail(), request.getPassword());
			LoginResponse_admin response = new LoginResponse_admin();
			response.setToken(jwt.generateTokenfor_Admin(admin));
			response.setAdmin(admin);
			return ResponseEntity.ok(response);
		}

		if ("Coach".equals(role)) {
			authenticate(request);
			Coach coach = authenticationService.coachsign(request.getEmail(), request.getPassword());
			LoginResponse_coach response = new LoginResponse_coach();
			response.setToken(jwt.generateTokenfor_Coach(coach));
			response.setCoach(coach);
			return ResponseEntity.ok(response);
		}

		if ("Athelet".equals(role)) {
			authenticate(request);
			Athelet athlete = authenticationService.Athlethsign(request.getEmail(), request.getPassword());
			LoginResponse_athelet response = new LoginResponse_athelet();
			response.setToken(jwt.generateTokenforathelet(athlete));
			response.setAthelet(athlete);
			return ResponseEntity.ok(response);
		}

		throw new InvalidCredentialsException();
	}

	private void authenticate(LoginRequest request) {
		// Throws AuthenticationException on a bad password, which
		// GlobalExceptionHandler turns into a 401 carrying the same message as
		// an unknown email - so neither answer reveals which one was wrong.
		authenticationManager.authenticate(
				new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
	}

	/**
	 * Revokes the presented token immediately, rather than letting it stay
	 * valid (to anyone holding a copy) until its natural expiry.
	 *
	 * <p>Not in SecurityConfig's permitAll block on purpose: revoking a token
	 * requires presenting one, so this falls through to the default
	 * "any authenticated role" rule.
	 */
	@PostMapping("/logout")
	public ResponseEntity<Map<String, String>> logout(@RequestHeader("Authorization") String authHeader) {
		if (authHeader != null && authHeader.startsWith("Bearer ")) {
			tokenBlacklistService.revoke(authHeader.substring(7));
		}
		return ResponseEntity.ok(Map.of("message", "Logged out"));
	}
}
