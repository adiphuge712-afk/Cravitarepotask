package com.example.demo.sse;

/**
 * Pushed on either half of the "request a coach" flow: an athlete submitting
 * or updating their request, or an admin assigning a coach. {@code type}
 * lets the frontend tell the two apart - an admin's list reloads on both the
 * same way, but the athlete's own screen reacts differently: a submitted
 * request just means "your status was recorded"; an assignment means their
 * dashboard is now out of date relative to the JWT it holds and they need to
 * refresh to see it.
 */
public record CoachRequestEvent(String type, long athleteId) {

	public static final String REQUEST_SUBMITTED = "request-submitted";
	public static final String COACH_ASSIGNED = "coach-assigned";
}
