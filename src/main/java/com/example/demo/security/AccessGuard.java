package com.example.demo.security;

/**
 * Object-level authorization: may the <em>current caller</em> touch <em>this
 * particular record</em>?
 *
 * <p>Spring Security only answers the role-level question ("is this caller an
 * ATHELET?"). That is not enough on its own, because almost every endpoint
 * takes the target's id straight from the URL - so being signed in as any
 * athlete was enough to read, and write, every other athlete's data by
 * changing a number in the path. These checks close that gap.
 *
 * <p>Each method throws {@link org.springframework.security.access.AccessDeniedException}
 * (mapped to 403 by {@code GlobalExceptionHandler}) when the caller has no
 * business with that record, and returns quietly when they do.
 */
public interface AccessGuard {

	/**
	 * Admin: any athlete. Coach: only athletes assigned to them.
	 * Athlete: only themselves.
	 */
	void requireAccessToAthlete(long athleteId);

	/**
	 * Admin: any coach. Coach: only themselves.
	 * Athlete: only the coach they are assigned to - an athlete legitimately
	 * reads their own coach's drills and plans.
	 */
	void requireAccessToCoach(long coachId);

	/**
	 * Admin: any plan. Coach: only plans they own. Athlete: never - athletes
	 * read schedules through their coach, they do not edit plans.
	 */
	void requireAccessToPlan(long planId);

	/** Admin: only their own admin record. Everyone else: denied. */
	void requireAccessToAdmin(long adminId);

	/** The signed-in athlete's own id, for endpoints that should never trust a path value. */
	long currentAthleteId();

	/** The signed-in coach's own id, for endpoints that should never trust a path value. */
	long currentCoachId();

	/**
	 * The signed-in athlete's own assigned coach's id - for subscribing to
	 * that coach's schedule, without trusting a path value for either id.
	 *
	 * @throws org.springframework.security.access.AccessDeniedException if the caller is not an
	 *         athlete, or is an athlete with no coach assigned yet
	 */
	long currentAssignedCoachId();
}
