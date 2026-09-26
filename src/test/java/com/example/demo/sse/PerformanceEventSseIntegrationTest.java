package com.example.demo.sse;

import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.security.jwtutil;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.request;

/**
 * Proves the thing PerformanceEventBroadcasterImplTest deliberately does not:
 * that subscribing and disconnecting through a real HTTP request actually
 * registers and then cleans up the emitter. A bare {@code new SseEmitter()}
 * never invokes its {@code onCompletion} callback (see that test's class
 * comment) - only a real, dispatched async request does, which is what this
 * gets by going through MockMvc against the live application context.
 *
 * <p>Note what this deliberately does NOT do: call
 * {@code MvcResult.getAsyncResult()}. That call blocks until the async
 * processing it asks about has already finished - for a long-lived
 * {@code SseEmitter} that never happens on its own, so calling it here would
 * deadlock the test forever waiting for a completion that only the test
 * itself can trigger. The emitter reference instead comes straight from the
 * broadcaster, which is the same live object the controller subscribed.
 */
@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
		"app.mail.enabled=false",
		"spring.mail.host=localhost",
		"spring.mail.port=1",
		"spring.mail.username=",
		"spring.mail.password="
})
@Transactional
class PerformanceEventSseIntegrationTest {

	@Autowired private MockMvc mockMvc;
	@Autowired private jwtutil jwtUtil;
	@Autowired private Atheletrepo atheletrepo;
	@Autowired private PerformanceEventBroadcasterImpl broadcaster;

	@Test
	void subscribingAndDisconnectingThroughARealRequestCleansUpTheEmitter() throws Exception {
		Athelet athlete = atheletrepo.findAll().stream().findFirst()
				.orElseGet(() -> { Assumptions.abort("No athlete in this database to test against"); return null; });
		String token = jwtUtil.generateTokenforathelet(athlete);

		int before = broadcaster.athleteSubscriberCount(athlete.getAthid());

		MvcResult mvcResult = mockMvc.perform(get("/athelet/sse/performance").header("Authorization", "Bearer " + token))
				.andExpect(request().asyncStarted())
				.andReturn();

		List<SseEmitter> subscribed = broadcaster.athleteEmittersFor(athlete.getAthid());
		assertThat(subscribed).hasSize(before + 1);

		subscribed.get(subscribed.size() - 1).complete();

		// MockAsyncContext.complete() alone does not notify AsyncListeners the
		// way a real servlet container's async dispatch does - MockMvc needs
		// this second, explicit dispatch to actually fire onCompletion.
		mockMvc.perform(asyncDispatch(mvcResult));

		assertThat(broadcaster.athleteSubscriberCount(athlete.getAthid())).isEqualTo(before);
	}
}
