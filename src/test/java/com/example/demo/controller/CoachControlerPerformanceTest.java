package com.example.demo.controller;

import com.example.demo.entity.Performancelog;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.security.AccessGuard;
import com.example.demo.service.PerformanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Recording an athlete's results is a coach action, so these endpoints now
 * live only on CoachControler - they used to be duplicated onto the athlete
 * and admin controllers as well, which is what made cross-prefix access
 * possible in the first place.
 *
 * <p>The id-order assertions are the regression guard: the paths read
 * {aid}/{wid} while the service takes (data, athid, workid), and those two
 * once crossed over, writing results against the wrong athlete.
 */
class CoachControlerPerformanceTest {

	private static final long WORK_ID = 22L;
	private static final long ATHLETE_ID = 11L;

	private PerformanceService performanceService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		CoachControler controller = new CoachControler();
		performanceService = Mockito.mock(PerformanceService.class);
		ReflectionTestUtils.setField(controller, "performanceService", performanceService);
		ReflectionTestUtils.setField(controller, "accessGuard", Mockito.mock(AccessGuard.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	@DisplayName("REGRESSION: PUT /updatePerformancelog/{aid}/{wid} passes athlete id and work id unswapped")
	void updateWithBodyPassesIdsInTheCorrectSlots() throws Exception {
		mockMvc.perform(put("/coach/updatePerformancelog/" + ATHLETE_ID + "/" + WORK_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completestatus\":\"DONE\"}"))
				.andExpect(status().isOk());

		verify(performanceService).addPerformancedata(any(Performancelog.class), eq(ATHLETE_ID), eq(WORK_ID));
	}

	@Test
	@DisplayName("REGRESSION: updating a log actually persists performancematrix and fatiquelevel, not just completestatus")
	void updatePersistsTheFullBodyNotJustStatus() throws Exception {
		// This is exactly the bug that shipped: the controller used to extract
		// only completestatus from the request body and call a service method
		// that never touched the other two fields, so they silently stayed
		// null (shown as "N/A") on every save through this endpoint.
		mockMvc.perform(put("/coach/updatePerformancelog/" + ATHLETE_ID + "/" + WORK_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completestatus\":\"DONE\",\"performancematrix\":\"High\",\"fatiquelevel\":\"Low\"}"))
				.andExpect(status().isOk());

		ArgumentCaptor<Performancelog> captor = ArgumentCaptor.forClass(Performancelog.class);
		verify(performanceService).addPerformancedata(captor.capture(), eq(ATHLETE_ID), eq(WORK_ID));

		Performancelog sent = captor.getValue();
		assertThat(sent.getCompletestatus()).isEqualTo("DONE");
		assertThat(sent.getPerformancematrix()).isEqualTo("High");
		assertThat(sent.getFatiquelevel()).isEqualTo("Low");
	}

	@Test
	@DisplayName("positive: POST /addDataPerformancelog/{id}/{wid} passes athlete id then work id")
	void addPerformancePassesIdsInTheCorrectSlots() throws Exception {
		mockMvc.perform(post("/coach/addDataPerformancelog/" + ATHLETE_ID + "/" + WORK_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completestatus\":\"DONE\"}"))
				.andExpect(status().isCreated());

		verify(performanceService).addPerformancedata(any(Performancelog.class), eq(ATHLETE_ID), eq(WORK_ID));
	}

	@Test
	@DisplayName("negative: an unknown athlete surfaces as 404, not 201")
	void unknownAthleteIs404() throws Exception {
		doThrow(new ResourceNotFoundException("Athlete", ATHLETE_ID))
				.when(performanceService).addPerformancedata(any(Performancelog.class), eq(ATHLETE_ID), eq(WORK_ID));

		mockMvc.perform(post("/coach/addDataPerformancelog/" + ATHLETE_ID + "/" + WORK_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completestatus\":\"DONE\"}"))
				.andExpect(status().isNotFound());
	}

	@Test
	@DisplayName("negative: an unknown workout on update surfaces as 404")
	void unknownWorkoutIs404() throws Exception {
		doThrow(new ResourceNotFoundException("Workdril", WORK_ID))
				.when(performanceService).addPerformancedata(any(Performancelog.class), eq(ATHLETE_ID), eq(WORK_ID));

		mockMvc.perform(put("/coach/updatePerformancelog/" + ATHLETE_ID + "/" + WORK_ID)
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"completestatus\":\"DONE\"}"))
				.andExpect(status().isNotFound());
	}
}
