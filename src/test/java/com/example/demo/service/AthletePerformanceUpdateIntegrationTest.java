package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Workdirl;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Workdrilrepo;
import com.example.demo.security.jwtutil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Mirrors CoachPerformanceUpdateIntegrationTest for the other side of the
 * same flow: an athlete marking their own drill complete via
 * PUT /athelet/updatePerformancelogs/{aid}/{wid}. Goes through the real
 * JwtFilter and SecurityConfig's hasRole("ATHELET") check with a real
 * account, not a mocked principal, so a role-string mismatch or filter-chain
 * rejection would show up here.
 *
 * <p>{@code @Transactional} rolls back anything this test writes - see
 * CoachRequestServiceIntegrationTest for the full reasoning. Mail is pinned
 * off for the same reason every {@code @SpringBootTest} in this project
 * pins it off.
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
class AthletePerformanceUpdateIntegrationTest {

	@Autowired private Atheletrepo atheletrepo;
	@Autowired private Workdrilrepo workdrilrepo;
	@Autowired private MockMvc mockMvc;
	@Autowired private jwtutil jwtUtil;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("HTTP: PUT /athelet/updatePerformancelogs/{aid}/{wid} with a real athlete JWT succeeds")
	void realHttpRequestWithAthleteTokenSucceeds() throws Exception {
		Athelet athlete = atheletrepo.findAll().stream().findFirst()
				.orElseGet(() -> { Assumptions.abort("No athlete in this database to test against"); return null; });
		Workdirl workout = workdrilrepo.findAll().stream().findFirst()
				.orElseGet(() -> { Assumptions.abort("No work drill in this database to test against"); return null; });

		String token = jwtUtil.generateTokenforathelet(athlete);

		mockMvc.perform(put("/athelet/updatePerformancelogs/" + athlete.getAthid() + "/" + workout.getWorkid())
						.param("data", "Completed")
						.header("Authorization", "Bearer " + token))
				.andExpect(status().isCreated());
	}
}
