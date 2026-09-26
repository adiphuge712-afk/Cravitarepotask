package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.entity.Performancelog;
import com.example.demo.entity.Workdirl;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Coachrepo;
import com.example.demo.repository.Performancerepo;
import com.example.demo.repository.Workdrilrepo;
import com.example.demo.security.AccessGuard;
import com.example.demo.security.jwtutil;
import com.example.demo.security.principal.CoachPrinciple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Reproduces "a coach cannot update an athlete's performance status" against
 * the real database and the real {@link AccessGuard}, not a mocked one, to
 * find out whether that report points at an actual regression or at a UI
 * page (the read-only Performance Log page has no edit action - the edit
 * form lives on the Athlete Work Management page instead).
 *
 * <p>{@code @Transactional} rolls back anything this test writes, so it is
 * safe to run against the real data - see CoachRequestServiceIntegrationTest
 * for the full reasoning. Mail is pinned off for the same reason it is on
 * every {@code @SpringBootTest} in this project: a live SMTP context must
 * never be one exported env var away from sending real email in a test run.
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
class CoachPerformanceUpdateIntegrationTest {

	@Autowired private AccessGuard accessGuard;
	@Autowired private PerformanceService performanceService;
	@Autowired private Coachrepo coachrepo;
	@Autowired private Atheletrepo atheletrepo;
	@Autowired private Workdrilrepo workdrilrepo;
	@Autowired private Performancerepo performancerepo;
	@Autowired private MockMvc mockMvc;
	@Autowired private jwtutil jwtUtil;

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("a coach can update the completion status of an athlete on their own squad")
	void coachCanUpdateOwnAthletesStatus() {
		Coach coach = firstCoachWithAnAssignedAthleteAndAWorkout();
		Athelet athlete = atheletrepo.findByCoachid_Coachid(coach.getCoachid()).get(0);
		Workdirl workout = workdrilrepo.findByPlan_Coachid_Coachid(coach.getCoachid()).get(0);

		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(new CoachPrinciple(coach), null, List.of()));

		// This is exactly what CoachControler.updateperformance does, in order:
		// the object-level guard, then the write via addPerformancedata (not
		// updatePerformancedata - that method only ever takes a bare status
		// string and never persists performancematrix/fatiquelevel, which is
		// what silently dropped those fields on every save through this
		// endpoint before). If either the guard call or the write throws,
		// this is a real backend regression - not a wrong-page UX issue.
		Performancelog data = new Performancelog();
		data.setCompletestatus("Completed");
		assertThatCode(() -> {
			accessGuard.requireAccessToAthlete(athlete.getAthid());
			performanceService.addPerformancedata(data, athlete.getAthid(), workout.getWorkid());
		}).doesNotThrowAnyException();

		Performancelog saved = performancerepo.findByAthid_AthidAndWorkid_Workid(athlete.getAthid(), workout.getWorkid());
		assertThat(saved).isNotNull();
		assertThat(saved.getCompletestatus()).isEqualTo("Completed");
	}

	@Test
	@DisplayName("HTTP: PUT /coach/updatePerformancelog/{aid}/{wid} with a real coach JWT succeeds")
	void realHttpRequestWithCoachTokenSucceeds() throws Exception {
		Coach coach = firstCoachWithAnAssignedAthleteAndAWorkout();
		Athelet athlete = atheletrepo.findByCoachid_Coachid(coach.getCoachid()).get(0);
		Workdirl workout = workdrilrepo.findByPlan_Coachid_Coachid(coach.getCoachid()).get(0);
		String token = jwtUtil.generateTokenfor_Coach(coach);

		// Goes through the real JwtFilter and SecurityConfig's hasRole("COACH")
		// check - unlike the test above, this is the exact path a browser
		// request takes, so it also catches a role-string mismatch or a
		// filter-chain rejection that a direct service call would miss.
		mockMvc.perform(put("/coach/updatePerformancelog/" + athlete.getAthid() + "/" + workout.getWorkid())
						.header("Authorization", "Bearer " + token)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completestatus\":\"Completed\"}"))
				.andExpect(status().isOk());
	}

	private Coach firstCoachWithAnAssignedAthleteAndAWorkout() {
		for (Coach c : coachrepo.findAll()) {
			boolean hasAthlete = !atheletrepo.findByCoachid_Coachid(c.getCoachid()).isEmpty();
			boolean hasWorkout = !workdrilrepo.findByPlan_Coachid_Coachid(c.getCoachid()).isEmpty();
			if (hasAthlete && hasWorkout) {
				return c;
			}
		}
		Assumptions.abort("No coach in this database has both an assigned athlete and a workout to test against");
		return null;
	}
}
