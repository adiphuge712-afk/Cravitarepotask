package com.example.demo.service;

import java.util.Optional;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Adminrepo;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Coachrepo;

@Service
public class AccountDirectoryImpl implements AccountDirectory {

	private final Atheletrepo athr;
	private final Adminrepo adr;
	private final Coachrepo chr;
	private final PasswordEncoder passwordEncoder;

	public AccountDirectoryImpl(Atheletrepo athr, Adminrepo adr, Coachrepo chr, PasswordEncoder passwordEncoder) {
		this.athr = athr;
		this.adr = adr;
		this.chr = chr;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public boolean exists(String email) {
		if (email == null || email.isBlank()) return false;
		return athr.findByEmail(email).isPresent()
				|| adr.findByEmail(email).isPresent()
				|| chr.findByEmail(email).isPresent();
	}

	@Override
	public Optional<String> displayName(String email) {
		if (email == null || email.isBlank()) return Optional.empty();

		Optional<String> athlete = athr.findByEmail(email).map(Athelet::getName);
		if (athlete.isPresent()) return athlete;

		Optional<String> admin = adr.findByEmail(email).map(Admin::getName);
		if (admin.isPresent()) return admin;

		return chr.findByEmail(email).map(Coach::getName);
	}

	@Override
	public boolean updatePassword(String email, String newPassword) {
		if (email == null || email.isBlank()) return false;

		String encoded = passwordEncoder.encode(newPassword);

		// Same precedence as AuthenticationServiceImpl.loadUserByUsername, so a
		// reset always lands on the account that would actually be signed in.
		Optional<Athelet> athlete = athr.findByEmail(email);
		if (athlete.isPresent()) {
			Athelet a = athlete.get();
			a.setPassword(encoded);
			athr.save(a);
			return true;
		}

		Optional<Admin> admin = adr.findByEmail(email);
		if (admin.isPresent()) {
			Admin a = admin.get();
			a.setPassword(encoded);
			adr.save(a);
			return true;
		}

		Optional<Coach> coach = chr.findByEmail(email);
		if (coach.isPresent()) {
			Coach c = coach.get();
			c.setPassword(encoded);
			chr.save(c);
			return true;
		}

		return false;
	}
}
