package com.example.demo.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Regression coverage for the role mass-assignment fix: a client that
 * slips a "role" (or any other unexpected) field into the registration
 * JSON must not be able to influence anything, because the request DTOs
 * have no such property to bind it to.
 */
class RegistrationRequestJacksonTest {

	private final ObjectMapper mapper = new ObjectMapper();

	@Test
	void athleteRequest_ignoresAttemptedRoleEscalationField() throws Exception {
		String json = """
				{
				  "name": "Aditya",
				  "email": "aditya@example.com",
				  "password": "secret",
				  "age": 21,
				  "sporttype": "Cricket",
				  "role": "ADMIN"
				}
				""";

		AthleteRegistrationRequest request = mapper.readValue(json, AthleteRegistrationRequest.class);

		assertThat(request.getName()).isEqualTo("Aditya");
		assertThat(request.getEmail()).isEqualTo("aditya@example.com");
		assertThat(request.getPassword()).isEqualTo("secret");
		assertThat(request.getAge()).isEqualTo(21);
		assertThat(request.getSporttype()).isEqualTo("Cricket");
		// No getRole()/setRole() exists on this DTO at all - the "role":"ADMIN"
		// field above is silently dropped during deserialization instead of
		// ever reaching the entity that gets persisted.
	}

	@Test
	void coachRequest_ignoresAttemptedRoleEscalationAndStrayAdminIdField() throws Exception {
		String json = """
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

		CoachRegistrationRequest request = mapper.readValue(json, CoachRegistrationRequest.class);

		assertThat(request.getName()).isEqualTo("Coach Kapil");
		assertThat(request.getEmail()).isEqualTo("coach@example.com");
		assertThat(request.getSpecialization()).isEqualTo("Cricket");
		assertThat(request.getExperience()).isEqualTo(10);
		// The admin the coach is registered under is taken exclusively from the
		// path variable/service call, never from the request body, so a stray
		// "adminid" in the JSON (as the real frontend form happens to send) or
		// a "role" field must not break deserialization or be usable.
	}
}
