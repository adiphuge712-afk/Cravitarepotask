package com.example.demo.controller;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.exception.GlobalExceptionHandler;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.security.jwtutil;
import com.example.demo.service.AuthenticationService;
import com.example.demo.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The single login endpoint that replaced three identical per-role copies.
 * One test class now covers what previously would have needed three.
 */
class AuthControllerLoginTest {

	private AuthenticationManager authenticationManager;
	private AuthenticationService authenticationService;
	private jwtutil jwt;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		authenticationManager = Mockito.mock(AuthenticationManager.class);
		authenticationService = Mockito.mock(AuthenticationService.class);
		jwt = Mockito.mock(jwtutil.class);
		AuthController controller = new AuthController(
				authenticationManager, authenticationService, jwt,
				Mockito.mock(TokenBlacklistService.class));
		mockMvc = MockMvcBuilders.standaloneSetup(controller)
				.setControllerAdvice(new GlobalExceptionHandler())
				.build();
	}

	private String body(String email, String password, String role) {
		return "{\"email\":\"" + email + "\",\"password\":\"" + password + "\",\"role\":\"" + role + "\"}";
	}

	@Test
	@DisplayName("positive: Admin login returns the admin plus a token")
	void adminLogin() throws Exception {
		Admin admin = new Admin();
		admin.setEmail("admin@example.com");
		when(authenticationService.addminsign("admin@example.com", "pw")).thenReturn(admin);
		when(jwt.generateTokenfor_Admin(admin)).thenReturn("admin-token");

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("admin@example.com", "pw", "Admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("admin-token"))
				.andExpect(jsonPath("$.admin.email").value("admin@example.com"));
	}

	@Test
	@DisplayName("positive: Coach login returns the coach plus a token")
	void coachLogin() throws Exception {
		Coach coach = new Coach();
		coach.setEmail("coach@example.com");
		when(authenticationService.coachsign("coach@example.com", "pw")).thenReturn(coach);
		when(jwt.generateTokenfor_Coach(coach)).thenReturn("coach-token");

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("coach@example.com", "pw", "Coach")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("coach-token"))
				.andExpect(jsonPath("$.coach.email").value("coach@example.com"));
	}

	@Test
	@DisplayName("positive: Athelet login returns the athlete plus a token")
	void athleteLogin() throws Exception {
		Athelet athlete = new Athelet();
		athlete.setEmail("athlete@example.com");
		when(authenticationService.Athlethsign("athlete@example.com", "pw")).thenReturn(athlete);
		when(jwt.generateTokenforathelet(athlete)).thenReturn("athlete-token");

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("athlete@example.com", "pw", "Athelet")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.token").value("athlete-token"))
				.andExpect(jsonPath("$.athelet.email").value("athlete@example.com"));
	}

	@Test
	@DisplayName("SECURITY: the password is never echoed back in the login response")
	void passwordNeverReturned() throws Exception {
		Admin admin = new Admin();
		admin.setEmail("admin@example.com");
		admin.setPassword("$2a$10$somebcrypthash");
		when(authenticationService.addminsign("admin@example.com", "pw")).thenReturn(admin);
		when(jwt.generateTokenfor_Admin(admin)).thenReturn("admin-token");

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("admin@example.com", "pw", "Admin")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.admin.password").doesNotExist());
	}

	@Test
	@DisplayName("negative: a wrong password is a 401, and no token is ever minted")
	void wrongPasswordIs401() throws Exception {
		when(authenticationManager.authenticate(any())).thenThrow(new BadCredentialsException("bad"));

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("admin@example.com", "wrong", "Admin")))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.message").value("Invalid email, password or role"));

		verify(jwt, never()).generateTokenfor_Admin(any());
	}

	@Test
	@DisplayName("negative: an unknown account for the claimed role is a 401")
	void unknownAccountIs401() throws Exception {
		when(authenticationService.addminsign(any(), any())).thenThrow(new InvalidCredentialsException());

		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("nobody@example.com", "pw", "Admin")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("negative: an unrecognised role is a 401, not a 500")
	void unknownRoleIs401() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content(body("someone@example.com", "pw", "Superuser")))
				.andExpect(status().isUnauthorized());
	}

	@Test
	@DisplayName("REGRESSION: a missing role is a clean 401, not an NPE")
	void nullRoleIs401() throws Exception {
		mockMvc.perform(post("/auth/login").contentType(MediaType.APPLICATION_JSON)
						.content("{\"email\":\"someone@example.com\",\"password\":\"pw\"}"))
				.andExpect(status().isUnauthorized());
	}
}
