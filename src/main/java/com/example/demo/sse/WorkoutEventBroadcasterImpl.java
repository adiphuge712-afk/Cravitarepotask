package com.example.demo.sse;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.entity.Workdirl;

@Component
public class WorkoutEventBroadcasterImpl implements WorkoutEventBroadcaster {

	// See PerformanceEventBroadcasterImpl for why admin uses a fixed sentinel
	// key instead of its own separate list.
	private static final long ADMIN_KEY = -1L;

	private final EmitterRegistry byCoach = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);
	private final EmitterRegistry admin = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);

	@Override
	public SseEmitter subscribe(long coachId) {
		return byCoach.subscribe(coachId);
	}

	@Override
	public SseEmitter subscribeAdmin() {
		return admin.subscribe(ADMIN_KEY);
	}

	@Override
	public void register(long coachId, SseEmitter emitter) {
		byCoach.register(coachId, emitter);
	}

	@Override
	public void registerAdmin(SseEmitter emitter) {
		admin.register(ADMIN_KEY, emitter);
	}

	@Override
	public void publish(Workdirl workout) {
		long coachId = workout.getPlan().getCoachid().getCoachid();
		WorkoutEvent event = new WorkoutEvent(workout.getWorkid(), coachId, workout.getWorkname());
		byCoach.sendTo(coachId, "workout-added", event);
		admin.sendTo(ADMIN_KEY, "workout-added", event);
	}

	/** Package-private: for tests, to verify a closed emitter is actually removed. */
	int subscriberCount(long coachId) {
		return byCoach.subscriberCount(coachId);
	}
}
