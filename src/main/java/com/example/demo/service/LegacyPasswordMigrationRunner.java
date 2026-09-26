package com.example.demo.service;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Adminrepo;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Coachrepo;

/**
 * One-time upgrade path from plaintext passwords to BCrypt hashes.
 *
 * <p>Before this change, every account's password was stored exactly as
 * typed. Switching the {@link PasswordEncoder} to BCrypt on its own would
 * lock every existing account out immediately - a raw string can never
 * equal a hash, so login would fail for every row already in the database.
 *
 * <p>This runs once at startup, finds any row whose stored password does not
 * already look like a BCrypt hash, and replaces it with one - computed from
 * the very value that was there, so the same password the account holder
 * already knows keeps working, they just never see the plaintext again.
 *
 * <p>It is safe to run on every boot: a row already migrated is left alone
 * (the format check is what makes that idempotent), so this component can
 * simply stay in place rather than needing to be removed after first use.
 */
@Component
public class LegacyPasswordMigrationRunner implements ApplicationRunner {

	private static final Logger log = LoggerFactory.getLogger(LegacyPasswordMigrationRunner.class);

	// $2a$, $2b$ and $2y$ are the BCrypt identifiers Spring's BCryptPasswordEncoder
	// produces; anything else in this column is a leftover plaintext password.
	private static final Pattern BCRYPT_HASH = Pattern.compile("^\\$2[aby]\\$\\d{2}\\$.{53}$");

	private final Adminrepo adminrepo;
	private final Atheletrepo atheletrepo;
	private final Coachrepo coachrepo;
	private final PasswordEncoder passwordEncoder;

	public LegacyPasswordMigrationRunner(Adminrepo adminrepo, Atheletrepo atheletrepo, Coachrepo coachrepo,
			PasswordEncoder passwordEncoder) {
		this.adminrepo = adminrepo;
		this.atheletrepo = atheletrepo;
		this.coachrepo = coachrepo;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public void run(ApplicationArguments args) {
		int migrated = migrateAdmins() + migrateAthletes() + migrateCoaches();
		if (migrated > 0) {
			log.warn("Migrated {} account(s) from a plaintext password to a BCrypt hash.", migrated);
		}
	}

	private boolean isAlreadyHashed(String storedPassword) {
		return storedPassword != null && BCRYPT_HASH.matcher(storedPassword).matches();
	}

	private int migrateAdmins() {
		List<Admin> toSave = new ArrayList<>();
		for (Admin a : adminrepo.findAll()) {
			if (!isAlreadyHashed(a.getPassword())) {
				a.setPassword(passwordEncoder.encode(a.getPassword()));
				toSave.add(a);
			}
		}
		if (!toSave.isEmpty()) adminrepo.saveAll(toSave);
		return toSave.size();
	}

	private int migrateAthletes() {
		List<Athelet> toSave = new ArrayList<>();
		for (Athelet a : atheletrepo.findAll()) {
			if (!isAlreadyHashed(a.getPassword())) {
				a.setPassword(passwordEncoder.encode(a.getPassword()));
				toSave.add(a);
			}
		}
		if (!toSave.isEmpty()) atheletrepo.saveAll(toSave);
		return toSave.size();
	}

	private int migrateCoaches() {
		List<Coach> toSave = new ArrayList<>();
		for (Coach c : coachrepo.findAll()) {
			if (!isAlreadyHashed(c.getPassword())) {
				c.setPassword(passwordEncoder.encode(c.getPassword()));
				toSave.add(c);
			}
		}
		if (!toSave.isEmpty()) coachrepo.saveAll(toSave);
		return toSave.size();
	}
}
