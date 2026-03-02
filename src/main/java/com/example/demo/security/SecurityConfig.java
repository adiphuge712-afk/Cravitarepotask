package com.example.demo.security;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.example.demo.JwtUtil.JwtFilter;

@Configuration
public class SecurityConfig {
	@Autowired
	private UserDetailsService userdetailservise;
	@Autowired
	private JwtFilter jwtfilter;
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
       return http
//            .csrf(csrf -> csrf.disable())
//            .authorizeHttpRequests(auth -> auth
//                .anyRequest().permitAll()
//            );
//
//        return http.build();
        .csrf(csrf -> csrf.disable())
        .cors(Customizer.withDefaults())
        .authorizeHttpRequests(auth -> auth.requestMatchers("/athelet/registerathlet","/athelet/login","/admin/registerathlet","/admin/login","/coach/registerathlet","/coach/login").permitAll()
        		.requestMatchers("/athelet/**").hasRole("ATHELET")
        		.requestMatchers("/admin/**").hasRole("ADMIN")
        		.requestMatchers("/coach/**").hasRole("COACH")
        .anyRequest().authenticated())
        .httpBasic(Customizer.withDefaults())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        .addFilterBefore(jwtfilter, UsernamePasswordAuthenticationFilter.class)
        .build();
    }
    @Bean
    public AuthenticationProvider authanticationprovider() {
    	DaoAuthenticationProvider provider =new DaoAuthenticationProvider(userdetailservise);
    	provider.setPasswordEncoder(NoOpPasswordEncoder.getInstance());
    	return provider;
    }
    @Bean
    public AuthenticationManager authenticationmanager(AuthenticationConfiguration config) {
    	return config.getAuthenticationManager();
    	
    }
}