package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;


@Configuration
public class SecurityConfig {

	private final UserDetailsService userdetailservise;
	private final JwtFilter jwtfilter;
	private final RestAuthenticationEntryPoint authenticationEntryPoint;
	private final RestAccessDeniedHandler accessDeniedHandler;

	public SecurityConfig(UserDetailsService userdetailservise, JwtFilter jwtfilter,
			RestAuthenticationEntryPoint authenticationEntryPoint, RestAccessDeniedHandler accessDeniedHandler) {
		this.userdetailservise = userdetailservise;
		this.jwtfilter = jwtfilter;
		this.authenticationEntryPoint = authenticationEntryPoint;
		this.accessDeniedHandler = accessDeniedHandler;
	}

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
		return http
				.csrf(csrf -> csrf.disable())
				.cors(Customizer.withDefaults())
				.authorizeHttpRequests(auth -> auth

						// --- public ---
						// "/error" must be permitted: Spring forwards every failed request
						// through it, and while it was secured EVERY error - even a malformed
						// body on a public endpoint - came back as an empty 401.
						.requestMatchers(
								"/error",
								// Signing in obviously cannot require being signed in.
								// One shared endpoint now, rather than the three
								// identical per-role copies this used to list.
								"/auth/login",
								"/athelet/registerathlet",
								"/admin/registerathlet",
								// Forgot-password: by definition the caller has no
								// working credentials, so these cannot require auth.
								"/auth/forgot-password", "/auth/verify-otp", "/auth/reset-password",
								// The Docker HEALTHCHECK / orchestrator liveness probe hits
								// this with no token at all - only "health" is exposed
								// (see application.properties), so this can't leak anything
								// beyond up/down.
								"/actuator/health")
						.permitAll()

						// --- strict prefix isolation: one prefix, one role ---
						// An ADMIN token works on /admin/** and nowhere else; likewise
						// COACH and ATHELET. There are deliberately no cross-prefix
						// exceptions here.
						//
						// This used to need extra `/*/`-wildcard operation rules,
						// because all three controllers were copies of one another and
						// exposed every endpoint - so without those an athlete could
						// call /athelet/registercoach/{id}. The wildcards fixed that but
						// had the opposite side effect: they also let an admin token
						// into /athelet/** and /coach/**. Each controller now exposes
						// only its own role's endpoints, so plain prefix rules are both
						// sufficient and exact.
						.requestMatchers("/admin/**").hasRole("ADMIN")
						.requestMatchers("/coach/**").hasRole("COACH")
						.requestMatchers("/athelet/**").hasRole("ATHELET")

						// /auth/logout and anything else: a valid token of any role.
						.anyRequest().authenticated())

				// Return JSON for 401/403 instead of an empty body. These fire inside the
				// filter chain, where @RestControllerAdvice cannot reach.
				.exceptionHandling(ex -> ex
						.authenticationEntryPoint(authenticationEntryPoint)
						.accessDeniedHandler(accessDeniedHandler))

				// httpBasic() was removed: the API authenticates with JWT bearer tokens,
				// and leaving Basic enabled added a second credential path plus a
				// competing entry point that produced the empty 401s.
				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
				.addFilterBefore(jwtfilter, UsernamePasswordAuthenticationFilter.class)
				.build();
	}

	// The PasswordEncoder bean lives in PasswordEncoderConfig, not here - see
	// that class for why (this class needs a UserDetailsService, which needs
	// a PasswordEncoder; defining the encoder in this class too would be a
	// dependency cycle).
	@Bean
	public AuthenticationProvider authanticationprovider(PasswordEncoder passwordEncoder) {
		DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userdetailservise);
		provider.setPasswordEncoder(passwordEncoder);
		return provider;
	}

	@Bean
	public AuthenticationManager authenticationmanager(AuthenticationConfiguration config) throws Exception {
		return config.getAuthenticationManager();
	}
}
