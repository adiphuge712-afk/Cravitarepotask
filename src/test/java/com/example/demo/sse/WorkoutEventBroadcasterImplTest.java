package com.example.demo.sse;

import com.example.demo.entity.Coach;
import com.example.demo.entity.Traningplan;
import com.example.demo.entity.Workdirl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

class WorkoutEventBroadcasterImplTest {

	private final WorkoutEventBroadcasterImpl broadcaster = new WorkoutEventBroadcasterImpl();

	private static Workdirl workoutFor(long coachId) {
		Coach coach = new Coach();
		coach.setCoachid(coachId);

		Traningplan plan = new Traningplan();
		plan.setCoachid(coach);

		Workdirl work = new Workdirl();
		work.setWorkid(9L);
		work.setWorkname("Sprints");
		work.setPlan(plan);
		return work;
	}

	@Test
	@DisplayName("positive: subscribing registers the emitter in that coach's own bucket")
	void subscribingRegistersInTheRightBucket() {
		broadcaster.subscribe(2L);
		assertThat(broadcaster.subscriberCount(2L)).isEqualTo(1);
		assertThat(broadcaster.subscriberCount(3L)).isEqualTo(0);
	}

	@Test
	@DisplayName("positive: publishing with no subscribers does not throw")
	void publishWithNoSubscribers() {
		assertThatCode(() -> broadcaster.publish(workoutFor(2L))).doesNotThrowAnyException();
	}

	@Test
	@DisplayName("positive: an admin subscription does not throw when a drill is added for any coach")
	void publishReachesAdmin() {
		broadcaster.subscribeAdmin();
		assertThatCode(() -> broadcaster.publish(workoutFor(2L))).doesNotThrowAnyException();
		assertThatCode(() -> broadcaster.publish(workoutFor(9L))).doesNotThrowAnyException();
	}
}
