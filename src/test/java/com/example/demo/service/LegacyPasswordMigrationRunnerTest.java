package com.example.demo.service;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Adminrepo;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Coachrepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Proves the one-time plaintext-to-BCrypt cutover does what it must: an
 * account that could log in with its plaintext password yesterday can still
 * log in with that exact same password after migration, and an account
 * already migrated is left untouched (so this can run on every boot).
 */
@ExtendWith(MockitoExtension.class)
class LegacyPasswordMigrationRunnerTest {

	@Mock private Adminrepo adminrepo;
	@Mock private Atheletrepo atheletrepo;
	@Mock private Coachrepo coachrepo;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private LegacyPasswordMigrationRunner runner;

	@BeforeEach
	void setUp() {
		runner = new LegacyPasswordMigrationRunner(adminrepo, atheletrepo, coachrepo, passwordEncoder);
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("REGRESSION: a plaintext password is replaced by a hash that still validates the same password")
	void migratesAPlaintextPassword() throws Exception {
		Athelet athlete = new Athelet();
		athlete.setPassword("myOldPlaintextPassword");
		when(atheletrepo.findAll()).thenReturn(List.of(athlete));
		when(adminrepo.findAll()).thenReturn(List.of());
		when(coachrepo.findAll()).thenReturn(List.of());

		runner.run(null);

		ArgumentCaptor<List<Athelet>> captor = ArgumentCaptor.forClass(List.class);
		verify(atheletrepo).saveAll(captor.capture());
		Athelet migrated = captor.getValue().get(0);

		assertThat(migrated.getPassword()).isNotEqualTo("myOldPlaintextPassword");
		assertThat(passwordEncoder.matches("myOldPlaintextPassword", migrated.getPassword())).isTrue();
	}

	@Test
	@DisplayName("positive: an already-hashed password is left alone - no save call at all")
	void leavesAlreadyHashedPasswordsAlone() {
		Admin admin = new Admin();
		String alreadyHashed = passwordEncoder.encode("whatever");
		admin.setPassword(alreadyHashed);
		when(adminrepo.findAll()).thenReturn(List.of(admin));
		when(atheletrepo.findAll()).thenReturn(List.of());
		when(coachrepo.findAll()).thenReturn(List.of());

		runner.run(null);

		verify(adminrepo, never()).saveAll(anyList());
		assertThat(admin.getPassword()).isEqualTo(alreadyHashed);
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("positive: migrates coaches independently of admins and athletes")
	void migratesCoaches() {
		Coach coach = new Coach();
		coach.setPassword("coachPlaintext");
		when(coachrepo.findAll()).thenReturn(List.of(coach));
		when(adminrepo.findAll()).thenReturn(List.of());
		when(atheletrepo.findAll()).thenReturn(List.of());

		runner.run(null);

		ArgumentCaptor<List<Coach>> captor = ArgumentCaptor.forClass(List.class);
		verify(coachrepo).saveAll(captor.capture());
		assertThat(passwordEncoder.matches("coachPlaintext", captor.getValue().get(0).getPassword())).isTrue();
	}

	@Test
	@DisplayName("positive: an empty database migrates nothing and saves nothing")
	void emptyDatabase() {
		when(adminrepo.findAll()).thenReturn(List.of());
		when(atheletrepo.findAll()).thenReturn(List.of());
		when(coachrepo.findAll()).thenReturn(List.of());

		runner.run(null);

		verify(adminrepo, never()).saveAll(anyList());
		verify(atheletrepo, never()).saveAll(anyList());
		verify(coachrepo, never()).saveAll(anyList());
	}

	@SuppressWarnings("unchecked")
	@Test
	@DisplayName("positive: a mix of migrated and unmigrated rows only re-saves the unmigrated one")
	void mixedBatchOnlyResavesWhatNeedsIt() {
		Athelet alreadyMigrated = new Athelet();
		alreadyMigrated.setPassword(passwordEncoder.encode("already-fine"));
		Athelet stillPlaintext = new Athelet();
		stillPlaintext.setPassword("still-plaintext");
		when(atheletrepo.findAll()).thenReturn(List.of(alreadyMigrated, stillPlaintext));
		when(adminrepo.findAll()).thenReturn(List.of());
		when(coachrepo.findAll()).thenReturn(List.of());

		runner.run(null);

		ArgumentCaptor<List<Athelet>> captor = ArgumentCaptor.forClass(List.class);
		verify(atheletrepo).saveAll(captor.capture());
		assertThat(captor.getValue()).hasSize(1);
		assertThat(passwordEncoder.matches("still-plaintext", captor.getValue().get(0).getPassword())).isTrue();
	}
}
