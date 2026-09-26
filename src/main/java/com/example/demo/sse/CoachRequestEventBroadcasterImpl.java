package com.example.demo.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Component
public class CoachRequestEventBroadcasterImpl implements CoachRequestEventBroadcaster {

	// See PerformanceEventBroadcasterImpl for why admin uses a fixed sentinel
	// key instead of its own separate list.
	private static final long ADMIN_KEY = -1L;

	private final EmitterRegistry admin = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);
	private final EmitterRegistry byAthlete = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);

	@Override
	public SseEmitter subscribeAdmin() {
		return admin.subscribe(ADMIN_KEY);
	}

	@Override
	public SseEmitter subscribeAthlete(long athleteId) {
		return byAthlete.subscribe(athleteId);
	}

	@Override
	public void registerAthlete(long athleteId, SseEmitter emitter) {
		byAthlete.register(athleteId, emitter);
	}

	@Override
	public void registerAdmin(SseEmitter emitter) {
		admin.register(ADMIN_KEY, emitter);
	}

	@Override
	public void publishRequestSubmitted(long athleteId) {
		publish(CoachRequestEvent.REQUEST_SUBMITTED, athleteId);
	}

	@Override
	public void publishCoachAssigned(long athleteId) {
		publish(CoachRequestEvent.COACH_ASSIGNED, athleteId);
	}

	private void publish(String type, long athleteId) {
		CoachRequestEvent event = new CoachRequestEvent(type, athleteId);
		admin.sendTo(ADMIN_KEY, type, event);
		byAthlete.sendTo(athleteId, type, event);
	}

	/** Package-private: for tests, to verify a closed emitter is actually removed. */
	int athleteSubscriberCount(long athleteId) {
		return byAthlete.subscriberCount(athleteId);
	}
}
