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
 * Same regression coverage as {@link MycontrollerRegistrationTest} but for
 * the duplicate registration endpoints exposed under /admin - the
 * mass-assignment bug existed identically in all three controllers, so
 * each copy gets its own regression test.
 */
class AdminControllerRegistrationTest {

	private AdminController controller;
	private RegistrationService registrationService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		controller = new AdminController();
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

		mockMvc.perform(post("/admin/registerathlet")
						.contentType(MediaType.APPLICATION_JSON)
						.content(maliciousPayload))
				.andExpect(status().isAccepted());

		ArgumentCaptor<AthleteRegistrationRequest> captor = ArgumentCaptor.forClass(AthleteRegistrationRequest.class);
		verify(registrationService).registerAthlete(captor.capture());
		assertThat(captor.getValue().getEmail()).isEqualTo("aditya@example.com");
	}

	@Test
	void registerCoach_forwardsAdminIdFromPathAndSanitizedBody() throws Exception {
		when(registrationService.registerCoach(any(CoachRegistrationRequest.class), eq(7L)))
				.thenReturn(new Coach());

		String payloadWithStrayFields = """
				{
				  "name": "Coach Kapil",
				  "age": 40,
				  "email": "coach@example.com",
				  "password": "secret",
				  "specialization": "Cricket",
				  "experience": 10,
				  "role": "ADMIN",
				  "adminid": 999
				}
				""";

		mockMvc.perform(post("/admin/registercoach/7")
						.contentType(MediaType.APPLICATION_JSON)
						.content(payloadWithStrayFields))
				.andExpect(status().isAccepted());

		verify(registrationService).registerCoach(any(CoachRegistrationRequest.class), eq(7L));
	}

	@Test
	void registerAthlete_returns409_whenTheEmailIsAlreadyRegistered() throws Exception {
		when(registrationService.registerAthlete(any(AthleteRegistrationRequest.class)))
				.thenThrow(new DuplicateResourceException("Email already registered"));

		mockMvc.perform(post("/admin/registerathlet")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"taken@example.com\",\"password\":\"x\"}"))
				.andExpect(status().isConflict());
	}

	@Test
	void registerCoach_returns404_whenTheAdminIdIsUnknown() throws Exception {
		when(registrationService.registerCoach(any(CoachRegistrationRequest.class), eq(404L)))
				.thenThrow(new ResourceNotFoundException("Admin", 404L));

		mockMvc.perform(post("/admin/registercoach/404")
						.contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"coach@example.com\",\"password\":\"x\"}"))
				.andExpect(status().isNotFound());
	}
}
