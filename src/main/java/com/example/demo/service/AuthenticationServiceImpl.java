package com.example.demo.service;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.InvalidCredentialsException;

import com.example.demo.entity.Admin;
import com.example.demo.repository.Adminrepo;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.security.principal.AdminPrinciple;
import com.example.demo.security.principal.AtheletPrincilpals;
import com.example.demo.security.principal.CoachPrinciple;

/**
 * Resolves Spring Security principals and verifies credentials.
 *
 * <p>Extracted from the former {@code Servicefile} god class; the method
 * bodies are unchanged, only their home is.
 */
@Service
public class AuthenticationServiceImpl implements AuthenticationService, UserDetailsService {

	private final Adminrepo adr;
	private final Atheletrepo athr;
	private final Coachrepo chr;
	private final PasswordEncoder passwordEncoder;

	public AuthenticationServiceImpl(Adminrepo adr, Atheletrepo athr, Coachrepo chr, PasswordEncoder passwordEncoder) {
		this.adr = adr;
		this.athr = athr;
		this.chr = chr;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		try {
			Athelet athlete = athr.findByEmail(username)
					.orElseThrow(() -> new UsernameNotFoundException("Athlete not found"));
			return new AtheletPrincilpals(athlete);
		} catch (UsernameNotFoundException e1) {

			try {
				Admin admin = adr.findByEmail(username)
						.orElseThrow(() -> new UsernameNotFoundException("Admin not found"));
				return new AdminPrinciple(admin);
			} catch (UsernameNotFoundException e2) {

				Coach coach = chr.findByEmail(username)
						.orElseThrow(() -> new UsernameNotFoundException("Coach not found"));
				return new CoachPrinciple(coach);
			}
		}
	}

	@Override
	public Admin addminsign(String email, String password) {
		Admin c = adr.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException());
		if (!passwordEncoder.matches(password, c.getPassword())) {
			throw new InvalidCredentialsException();
		}
		return c;
	}

	@Override
	public Athelet Athlethsign(String email, String password) {
		Athelet c = athr.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException());
		if (!passwordEncoder.matches(password, c.getPassword())) {
			throw new InvalidCredentialsException();
		}
		return c;
	}

	@Override
	public Coach coachsign(String email, String password) {
		Coach c = chr.findByEmail(email).orElseThrow(() -> new InvalidCredentialsException());
		if (!passwordEncoder.matches(password, c.getPassword())) {
			throw new InvalidCredentialsException();
		}
		return c;
	}
}
