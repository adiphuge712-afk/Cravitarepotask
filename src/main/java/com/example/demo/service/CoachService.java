package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Coach;
import com.example.demo.dto.CoachUpdateRequest;

/** Coach lookups and maintenance. */
public interface CoachService {

	List<Coach> getAllCoach();

	Optional<Coach> getAllCoach(long id);

	/** No-op if no coach has that id. */
	void deletecoachbyid(long id);

	/**
	 * Updates a coach's editable profile fields. Role and password are
	 * deliberately not part of {@link CoachUpdateRequest}, so neither can be
	 * changed from here regardless of what a caller sends.
	 *
	 * @throws RuntimeException if no coach has that id
	 */
	Coach updatecoachbyid(long id, CoachUpdateRequest request);
}
