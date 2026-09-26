package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Athelet;

/** Athlete lookups and coach assignment. */
public interface AthleteService {

	List<Athelet> getAllAthelet();

	Optional<Athelet> getAllAthelet(long id);

	List<Athelet> getAllatheletbycouchid(long coachId);

	/**
	 * Assigns a coach to an athlete.
	 *
	 * @throws RuntimeException if either the coach or the athlete does not exist
	 */
	void Assigendid(long aid, long cid);
}
