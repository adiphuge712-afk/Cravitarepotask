package com.example.demo.sse;

import java.util.List;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.entity.Coach;
import com.example.demo.entity.Performancelog;

@Component
public class PerformanceEventBroadcasterImpl implements PerformanceEventBroadcaster {

	// EmitterRegistry keys by id; admin has no id of its own, so every admin
	// subscription shares this one fixed key. No real coach/athlete id is
	// ever negative (both are DB-generated, starting at 1), so this can never
	// collide with a real one.
	private static final long ADMIN_KEY = -1L;

	private final EmitterRegistry admin = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);
	private final EmitterRegistry byCoach = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);
	private final EmitterRegistry byAthlete = new EmitterRegistry(EmitterRegistry.DEFAULT_TIMEOUT_MS);

	@Override
	public SseEmitter subscribeAdmin() {
		return admin.subscribe(ADMIN_KEY);
	}

	@Override
	public SseEmitter subscribeCoach(long coachId) {
		return byCoach.subscribe(coachId);
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
	public void registerCoach(long coachId, SseEmitter emitter) {
		byCoach.register(coachId, emitter);
	}

	@Override
	public void registerAdmin(SseEmitter emitter) {
		admin.register(ADMIN_KEY, emitter);
	}

	@Override
	public void publish(Performancelog log) {
		long athleteId = log.getAthid().getAthid();
		Coach coach = log.getAthid().getCoachid();

		PerformanceEvent event = new PerformanceEvent(
				log.getLogid(), athleteId, log.getWorkid().getWorkid(), log.getCompletestatus());

		admin.sendTo(ADMIN_KEY, "performance-updated", event);
		if (coach != null) {
			byCoach.sendTo(coach.getCoachid(), "performance-updated", event);
		}
		byAthlete.sendTo(athleteId, "performance-updated", event);
	}

	/** Package-private: lets the test verify a closed emitter is actually removed, not just silently skipped. */
	int athleteSubscriberCount(long athleteId) {
		return byAthlete.subscriberCount(athleteId);
	}

	/**
	 * Package-private: hands the test the actual live emitter a real request
	 * subscribed, so it can call {@code .complete()} directly. MockMvc's
	 * {@code MvcResult.getAsyncResult()} cannot be used for this - it blocks
	 * until the async processing it is asking about has already finished,
	 * which for a still-open SseEmitter never happens on its own.
	 */
	List<SseEmitter> athleteEmittersFor(long athleteId) {
		return byAthlete.subscribersFor(athleteId);
	}

	/** Package-private: same as {@link #athleteSubscriberCount}, for the coach bucket. */
	int coachSubscriberCount(long coachId) {
		return byCoach.subscriberCount(coachId);
	}
}
