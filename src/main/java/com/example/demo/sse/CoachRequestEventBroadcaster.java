package com.example.demo.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Live push for the "request a coach" flow. An admin needs to know the
 * moment a new request comes in (so the Request Coach page's unassigned list
 * stays current without a manual refresh); an athlete needs to know the
 * moment a coach is assigned to them specifically.
 *
 * <p>Note what this cannot do: make an already-signed-in athlete's dashboard
 * show their new coach automatically. That dashboard's notion of "do I have
 * a coach" comes from the JWT issued at login, which is a snapshot - it does
 * not change just because the database row behind it did. Assignment events
 * exist here to tell the athlete to refresh (or re-log in), not to silently
 * rewrite state a live JWT cannot represent.
 */
public interface CoachRequestEventBroadcaster {

	/** Every coach-request submission and assignment, for an admin's dashboard. */
	SseEmitter subscribeAdmin();

	/** Only events for this athlete's own request. */
	SseEmitter subscribeAthlete(long athleteId);

	/**
	 * Adopts an emitter created by a combined multi-topic endpoint instead of
	 * opening a dedicated connection for this topic alone.
	 */
	void registerAthlete(long athleteId, SseEmitter emitter);

	/** Same as {@link #registerAthlete}, for an admin's dashboard. */
	void registerAdmin(SseEmitter emitter);

	/** Called after an athlete submits or updates their request. */
	void publishRequestSubmitted(long athleteId);

	/** Called after an admin assigns a coach to this athlete. */
	void publishCoachAssigned(long athleteId);
}
