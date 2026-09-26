package com.example.demo.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.entity.Performancelog;

/**
 * Live push for performance-log changes, scoped exactly the way the REST
 * reads already are: an admin sees every change, a coach sees only their own
 * squad's, an athlete sees only their own. This mirrors {@link
 * com.example.demo.security.AccessGuard} on purpose - a broadcast channel
 * that ignored those boundaries would leak one athlete's completion status
 * to every other signed-in athlete, which is exactly the class of bug this
 * project's object-level authorization exists to prevent.
 */
public interface PerformanceEventBroadcaster {

	/** Every performance-log change, for an admin's dashboard. */
	SseEmitter subscribeAdmin();

	/** Only changes affecting this coach's own squad. */
	SseEmitter subscribeCoach(long coachId);

	/** Only changes to this athlete's own logs. */
	SseEmitter subscribeAthlete(long athleteId);

	/**
	 * Adopts an emitter created by a combined multi-topic endpoint instead of
	 * opening a dedicated connection for this topic alone - see
	 * {@link EmitterRegistry#register}.
	 */
	void registerAthlete(long athleteId, SseEmitter emitter);

	/** Same as {@link #registerAthlete}, for a coach's own squad. */
	void registerCoach(long coachId, SseEmitter emitter);

	/** Same as {@link #registerAthlete}, for an admin's dashboard. */
	void registerAdmin(SseEmitter emitter);

	/** Called after a performance log is saved, to notify whoever is allowed to see it. */
	void publish(Performancelog log);
}
