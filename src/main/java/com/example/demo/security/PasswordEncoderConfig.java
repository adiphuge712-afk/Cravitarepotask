package com.example.demo.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * The {@link PasswordEncoder} bean, kept in its own configuration class
 * rather than in {@link SecurityConfig}.
 *
 * <p>{@code AuthenticationServiceImpl} (the app's {@code UserDetailsService})
 * needs a {@code PasswordEncoder} to verify credentials, and
 * {@code SecurityConfig} needs a {@code UserDetailsService} to build its
 * {@code AuthenticationProvider}. If the encoder bean were defined inside
 * {@code SecurityConfig} itself, that would close the loop -
 * {@code SecurityConfig} needing a bean that in turn needs
 * {@code SecurityConfig} to finish constructing - and Spring fails to start
 * with a {@code BeanCurrentlyInCreationException}. Splitting the encoder out
 * removes the cycle without changing what gets injected anywhere.
 */
@Configuration
public class PasswordEncoderConfig {

	/**
	 * BCrypt, with Spring's current default work factor. Every place that
	 * writes a password (registration, password reset, the legacy-password
	 * migration) must encode through this bean, and every place that checks
	 * one (login) must go through {@link PasswordEncoder#matches}, never
	 * {@code String.equals} - a raw comparison against a hash can never
	 * succeed, and comparing raw strings defeats the hash entirely.
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
