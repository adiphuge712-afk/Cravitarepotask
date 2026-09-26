package com.example.demo.sse;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.entity.Performancelog;
import com.example.demo.entity.Workdirl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Routing for the live performance-log feed. Every case here mirrors an
 * AccessGuard boundary on purpose: an admin sees everything, a coach only
 * their own squad, an athlete only themselves.
 *
 * <p>This intentionally does not assert that a completed emitter is removed
 * from its bucket - that was tried here first and turned out to be
 * untestable at this level. An {@code SseEmitter} only invokes its
 * {@code onCompletion}/{@code onTimeout}/{@code onError} callbacks once
 * Spring's async request machinery has {@code initialize()}d it inside a
 * real dispatched request; calling {@code .complete()} on one built with
 * {@code new SseEmitter()} in a bare unit test does nothing; the callback
 * chain has no handler yet to run through. Verifying real cleanup requires an
 * actual async HTTP request-response cycle - see
 * PerformanceEventSseIntegrationTest for that.
 */
class PerformanceEventBroadcasterImplTest {

	private final PerformanceEventBroadcasterImpl broadcaster = new PerformanceEventBroadcasterImpl();

	private static Performancelog logFor(long athleteId, Long coachId, long workId) {
		Athelet athlete = new Athelet();
		athlete.setAthid(athleteId);
		if (coachId != null) {
			Coach coach = new Coach();
			coach.setCoachid(coachId);
			athlete.setCoachid(coach);
		}

		Workdirl work = new Workdirl();
		work.setWorkid(workId);

		Performancelog log = new Performancelog();
		log.setLogid(99L);
		log.setAthid(athlete);
		log.setWorkid(work);
		log.setCompletestatus("Completed");
		return log;
	}

	@Test
	@DisplayName("positive: publishing with no subscribers of any kind does not throw")
	void publishWithNoSubscribers() {
		assertThatCode(() -> broadcaster.publish(logFor(1L, 2L, 3L))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("positive: publishing for an athlete with no coach assigned does not throw")
	void publishForUnassignedAthlete() {
		assertThatCode(() -> broadcaster.publish(logFor(1L, null, 3L))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("positive: subscribing registers the emitter in that athlete's own bucket")
	void subscribingRegistersInTheRightBucket() {
		broadcaster.subscribeAthlete(1L);
		assertThat(broadcaster.athleteSubscriberCount(1L)).isEqualTo(1);
		assertThat(broadcaster.athleteSubscriberCount(2L)).isEqualTo(0);
	}

	@Test
	@DisplayName("positive: multiple subscribers of the same kind all get registered independently")
	void multipleSubscribersOfSameKindAreIndependent() {
		broadcaster.subscribeCoach(2L);
		broadcaster.subscribeCoach(2L);
		assertThat(broadcaster.coachSubscriberCount(2L)).isEqualTo(2);
	}
}
