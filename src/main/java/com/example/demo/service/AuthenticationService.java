package com.example.demo.service;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;

/**
 * Credential verification for the three account types.
 *
 * <p>Implementations are also expected to serve as Spring Security's
 * {@code UserDetailsService}, so that principal resolution and credential
 * checking stay in one cohesive place instead of being spread across the
 * application.
 */
public interface AuthenticationService {

	/**
	 * @throws RuntimeException if no admin has that email, or the password does not match
	 */
	Admin addminsign(String email, String password);

	/**
	 * @throws RuntimeException if no athlete has that email, or the password does not match
	 */
	Athelet Athlethsign(String email, String password);

	/**
	 * @throws RuntimeException if no coach has that email, or the password does not match
	 */
	Coach coachsign(String email, String password);
}
