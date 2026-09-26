package com.example.demo.controller;

import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.dto.AthleteRegistrationRequest;
import com.example.demo.dto.CoachRegistrationRequest;
import com.example.demo.exception.DuplicateResourceException;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.security.AccessGuard;
import com.example.demo.service.RegistrationService;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Regression test for the role mass-assignment fix on the /athelet
 * registration endpoints. Uses a standalone MockMvc (no Spring context,
 * no datasource needed) with the RegistrationService dependency mocked.
 */
class MycontrollerRegistrationTest {

	private Mycontroller controller;
	private RegistrationService registrationService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		controller = new Mycontroller();
		registrationService = Mockito.mock(RegistrationService.class);
		ReflectionTestUtils.setField(controller, "registrationService", registrationService);
		// Authorization is covered by AccessGuardImplTest; these tests exercise
		// the endpoint behaviour, so the guard is a permissive stub here.
		ReflectionTestUtils.setField(controller, "accessGuard", Mockito.mock(AccessGuard.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	@Test
	void registerAthlete_ignoresRoleFieldAndForwardsSanitizedRequest() throws Exception {
		when(registrationService.registerAthlete(any(AthleteRegistrationRequest.class)))
				.thenReturn(new Athelet());

		String maliciousPayload = """
				{
				  "name": "Aditya",
				  "email": "aditya@example.com",
				  "password": "secret",
				  "age": 21,
				  "sporttype": "Cricket",
				  "role": "ADMIN"
				}
				""";

		mockMvc.perform(post("/athelet/registerathlet")
						.contentType(MediaType.APPLICATION_JSON)
						.content(maliciousPayload))
				.andExpect(status().isAccepted());

		ArgumentCaptor<AthleteRegistrationRequest> captor = ArgumentCaptor.forClass(AthleteRegistrationRequest.class);
		verify(registrationService).registerAthlete(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("aditya@example.com");
		// There is no getRole() on the captured DTO to even assert against -
		// the "role":"ADMIN" in the payload above had nowhere to land.
	}


	@Test
	void registerAthlete_returns409_whenTheEmailIsAlreadyRegistered() throws Exception {
		when(registrationService.registerAthlete(any(AthleteRegistrationRequest.class)))
				.thenThrow(new DuplicateResourceException("Email already registered"));

		mockMvc.perform(post("/athelet/registerathlet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"taken@example.com\",\"password\":\"x\"}"))
				.andExpect(status().isConflict());
	}


	@Test
	void registerAthlete_returns400_onMalformedJson() throws Exception {
		mockMvc.perform(post("/athelet/registerathlet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{ this is not json"))
				.andExpect(status().isBadRequest());
	}
}
