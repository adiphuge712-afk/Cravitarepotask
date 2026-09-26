package com.example.demo.service;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Adminrepo;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Coachrepo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Covers the directory that spans all three account tables - especially
 * {@link AccountDirectory#updatePassword}, which is the write path the
 * forgot-password flow lands on.
 */
@ExtendWith(MockitoExtension.class)
class AccountDirectoryImplTest {

	@Mock private Atheletrepo athr;
	@Mock private Adminrepo adr;
	@Mock private Coachrepo chr;

	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private AccountDirectoryImpl directory;

	@BeforeEach
	void setUp() {
		directory = new AccountDirectoryImpl(athr, adr, chr, passwordEncoder);
	}

	@Nested
	@DisplayName("exists")
	class Exists {

		@Test
		@DisplayName("positive: true when any of the three tables has the email")
		void trueWhenCoachHasIt() {
			when(athr.findByEmail("c@example.com")).thenReturn(Optional.empty());
			when(adr.findByEmail("c@example.com")).thenReturn(Optional.empty());
			when(chr.findByEmail("c@example.com")).thenReturn(Optional.of(new Coach()));

			assertThat(directory.exists("c@example.com")).isTrue();
		}

		@Test
		@DisplayName("negative: false when no table has it")
		void falseWhenNobodyHasIt() {
			when(athr.findByEmail(anyString())).thenReturn(Optional.empty());
			when(adr.findByEmail(anyString())).thenReturn(Optional.empty());
			when(chr.findByEmail(anyString())).thenReturn(Optional.empty());

			assertThat(directory.exists("nobody@example.com")).isFalse();
		}

		@Test
		@DisplayName("negative: false for a blank email, without querying any repository")
		void falseForBlankEmail() {
			assertThat(directory.exists("  ")).isFalse();
			assertThat(directory.exists(null)).isFalse();
		}
	}

	@Nested
	@DisplayName("updatePassword")
	class UpdatePassword {

		@Test
		@DisplayName("REGRESSION: the new password is stored as a BCrypt hash, never in the clear")
		void storesAHashNotPlaintext() {
			Athelet athlete = new Athelet();
			when(athr.findByEmail("a@example.com")).thenReturn(Optional.of(athlete));

			boolean updated = directory.updatePassword("a@example.com", "brandNewPassword1");

			assertThat(updated).isTrue();
			ArgumentCaptor<Athelet> captor = ArgumentCaptor.forClass(Athelet.class);
			verify(athr).save(captor.capture());
			assertThat(captor.getValue().getPassword())
					.isNotEqualTo("brandNewPassword1")
					.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
			assertThat(passwordEncoder.matches("brandNewPassword1", captor.getValue().getPassword())).isTrue();
		}

		@Test
		@DisplayName("positive: updates the admin table when the email belongs to an admin")
		void updatesAdmin() {
			when(athr.findByEmail("ad@example.com")).thenReturn(Optional.empty());
			Admin admin = new Admin();
			when(adr.findByEmail("ad@example.com")).thenReturn(Optional.of(admin));

			assertThat(directory.updatePassword("ad@example.com", "newpw123")).isTrue();

			verify(adr).save(admin);
			assertThat(passwordEncoder.matches("newpw123", admin.getPassword())).isTrue();
		}

		@Test
		@DisplayName("positive: updates the coach table when the email belongs to a coach")
		void updatesCoach() {
			when(athr.findByEmail("c@example.com")).thenReturn(Optional.empty());
			when(adr.findByEmail("c@example.com")).thenReturn(Optional.empty());
			Coach coach = new Coach();
			when(chr.findByEmail("c@example.com")).thenReturn(Optional.of(coach));

			assertThat(directory.updatePassword("c@example.com", "newpw123")).isTrue();

			verify(chr).save(coach);
			assertThat(passwordEncoder.matches("newpw123", coach.getPassword())).isTrue();
		}

		@Test
		@DisplayName("negative: no account with that email saves nothing and reports failure")
		void noAccountFound() {
			when(athr.findByEmail(anyString())).thenReturn(Optional.empty());
			when(adr.findByEmail(anyString())).thenReturn(Optional.empty());
			when(chr.findByEmail(anyString())).thenReturn(Optional.empty());

			assertThat(directory.updatePassword("nobody@example.com", "newpw123")).isFalse();

			verify(athr, never()).save(org.mockito.ArgumentMatchers.any());
			verify(adr, never()).save(org.mockito.ArgumentMatchers.any());
			verify(chr, never()).save(org.mockito.ArgumentMatchers.any());
		}

		@Test
		@DisplayName("negative: a blank email updates nothing")
		void blankEmail() {
			assertThat(directory.updatePassword("", "newpw123")).isFalse();
			assertThat(directory.updatePassword(null, "newpw123")).isFalse();
		}
	}
}
