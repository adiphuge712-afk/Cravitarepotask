package com.example.demo.sse;

import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.entity.Workdirl;

/**
 * Live push for a coach's schedule of work drills. Scoped by coach id, not
 * athlete id: a drill belongs to a coach's training plan and is shared by
 * every athlete on that coach's squad, so one channel per coach is both
 * correct and simpler than tracking it per athlete.
 */
public interface WorkoutEventBroadcaster {

	/**
	 * An athlete's own assigned coach's schedule, or a coach's own schedule
	 * (so their other open tabs pick up a drill they just added). Both use the
	 * same coach id, so this is a single subscribe method for both callers.
	 */
	SseEmitter subscribe(long coachId);

	/** Every drill added by any coach, academy-wide - for an admin's dashboard. */
	SseEmitter subscribeAdmin();

	/**
	 * Adopts an emitter created by a combined multi-topic endpoint instead of
	 * opening a dedicated connection for this topic alone.
	 */
	void register(long coachId, SseEmitter emitter);

	/** Same as {@link #register}, for an admin's dashboard. */
	void registerAdmin(SseEmitter emitter);

	/** Called after a coach adds a new work drill. */
	void publish(Workdirl workout);
}
