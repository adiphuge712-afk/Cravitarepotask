package com.example.demo.sse;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * See PerformanceEventBroadcasterImplTest for why a completed emitter's
 * removal is not asserted here directly (it needs a real dispatched async
 * request, not a bare unit test) - that path is exercised end to end for
 * this broadcaster too, once through EmitterRegistry, which both broadcasters
 * now share.
 */
class CoachRequestEventBroadcasterImplTest {

	private final CoachRequestEventBroadcasterImpl broadcaster = new CoachRequestEventBroadcasterImpl();

	@Test
	@DisplayName("positive: subscribing registers the emitter in that athlete's own bucket")
	void subscribingRegistersInTheRightBucket() {
		broadcaster.subscribeAthlete(1L);
		assertThat(broadcaster.athleteSubscriberCount(1L)).isEqualTo(1);
		assertThat(broadcaster.athleteSubscriberCount(2L)).isEqualTo(0);
	}

	@Test
	@DisplayName("positive: publishing a submission with no subscribers of any kind does not throw")
	void publishSubmittedWithNoSubscribers() {
		assertThatCode(() -> broadcaster.publishRequestSubmitted(1L)).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("positive: publishing an assignment with no subscribers of any kind does not throw")
	void publishAssignedWithNoSubscribers() {
		assertThatCode(() -> broadcaster.publishCoachAssigned(1L)).doesNotThrowAnyException();
	}
}
