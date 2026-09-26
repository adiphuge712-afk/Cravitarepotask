package com.example.demo.controller;

import com.example.demo.security.AccessGuard;
import com.example.demo.service.PerformanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The athlete-owned performance endpoints - an athlete may mark their own
 * drill complete, and a coach may also record it independently on
 * CoachControler's /coach/updatePerformancelog/{aid}/{wid}. Both paths write
 * through the same PerformanceService.updatePerformancedata.
 *
 * <p>The id-order assertion matters: the path reads {aid}/{wid} while the
 * service takes (data, athid, workid), and those once crossed over.
 */
class MycontrollerPerformanceTest {

	private static final long WORK_ID = 22L;
	private static final long ATHLETE_ID = 11L;

	private PerformanceService performanceService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		Mycontroller controller = new Mycontroller();
		performanceService = Mockito.mock(PerformanceService.class);
		ReflectionTestUtils.setField(controller, "performanceService", performanceService);
		// Authorization is covered by AccessGuardImplTest; this exercises the
		// endpoint behaviour, so the guard is a permissive stub here.
		ReflectionTestUtils.setField(controller, "accessGuard", Mockito.mock(AccessGuard.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	@DisplayName("REGRESSION: PUT /updatePerformancelogs/{aid}/{wid} passes athlete id and work id unswapped")
	void athleteSelfCompletePassesIdsInTheCorrectSlots() throws Exception {
		mockMvc.perform(put("/athelet/updatePerformancelogs/" + ATHLETE_ID + "/" + WORK_ID)
						.param("data", "COMPLETE"))
				.andExpect(status().isCreated());

		verify(performanceService).updatePerformancedata("COMPLETE", ATHLETE_ID, WORK_ID);
	}

	@Test
	@DisplayName("positive: reading an athlete's own logs delegates with that athlete's id")
	void readByAthleteId() throws Exception {
		when(performanceService.getPerformancelogByAtheletId(ATHLETE_ID)).thenReturn(List.of());

		mockMvc.perform(get("/athelet/viewDataPerformancelogAthid/" + ATHLETE_ID))
				.andExpect(status().isOk());

		verify(performanceService).getPerformancelogByAtheletId(ATHLETE_ID);
	}
}
