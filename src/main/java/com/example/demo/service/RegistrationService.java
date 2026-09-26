package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.dto.AthleteRegistrationRequest;
import com.example.demo.dto.CoachRegistrationRequest;

/**
 * Abstraction for turning a registration request into a persisted,
 * server-trusted account. Controllers depend on this interface rather
 * than on a concrete implementation (DIP), and it is the single place
 * that decides what role a newly registered account gets (SRP) - a
 * request DTO can never carry its own role, by construction.
 */
public interface RegistrationService {

	/**
	 * Persists a new athlete whose role is always server-assigned.
	 *
	 * @throws RuntimeException if the account cannot be persisted (e.g. duplicate email)
	 */
	Athelet registerAthlete(AthleteRegistrationRequest request);

	/**
	 * Persists a new coach, owned by the given admin, whose role is always server-assigned.
	 *
	 * @throws RuntimeException if no admin exists with {@code adminId}, or the account cannot be persisted
	 */
	Coach registerCoach(CoachRegistrationRequest request, long adminId);
}
