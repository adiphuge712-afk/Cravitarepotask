package com.example.demo.controller;

import com.example.demo.service.TokenBlacklistService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * The one shared logout endpoint, replacing what used to be three identical
 * (and non-functional - they only invalidated an HttpSession that STATELESS
 * auth never used) copies across the role controllers.
 */
class AuthControllerTest {

	private TokenBlacklistService tokenBlacklistService;
	private MockMvc mockMvc;

	@BeforeEach
	void setUp() {
		tokenBlacklistService = Mockito.mock(TokenBlacklistService.class);
		AuthController controller = new AuthController(
				Mockito.mock(org.springframework.security.authentication.AuthenticationManager.class),
				Mockito.mock(com.example.demo.service.AuthenticationService.class),
				Mockito.mock(com.example.demo.security.jwtutil.class),
				tokenBlacklistService);
		mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
	}

	@Test
	void logout_revokesExactlyTheBearerTokenPresented() throws Exception {
		mockMvc.perform(post("/auth/logout").header("Authorization", "Bearer abc.def.ghi"))
				.andExpect(status().isOk());

		verify(tokenBlacklistService).revoke(eq("abc.def.ghi"));
	}

	@Test
	void logout_withoutABearerPrefix_doesNotRevokeAnything() throws Exception {
		mockMvc.perform(post("/auth/logout").header("Authorization", "not-a-bearer-token"))
				.andExpect(status().isOk());

		verify(tokenBlacklistService, never()).revoke(org.mockito.ArgumentMatchers.anyString());
	}
}
