package com.example.demo.service;

import com.example.demo.entity.Admin;
import com.example.demo.repository.Adminrepo;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.exception.InvalidCredentialsException;
import com.example.demo.security.principal.AdminPrinciple;
import com.example.demo.security.principal.AtheletPrincilpals;
import com.example.demo.security.principal.CoachPrinciple;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

/**
 * Positive and negative coverage for principal resolution and credential
 * checking - the security-critical half of the former god class.
 */
@ExtendWith(MockitoExtension.class)
class AuthenticationServiceImplTest {

	@Mock
	private Adminrepo adr;
	@Mock
	private Atheletrepo athr;
	@Mock
	private Coachrepo chr;

	// A real encoder, not a mock: these tests need to prove a wrong password
	// is actually rejected by BCrypt comparison, not by a stubbed answer.
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private AuthenticationServiceImpl service;

	@BeforeEach
	void setUp() {
		service = new AuthenticationServiceImpl(adr, athr, chr, passwordEncoder);
	}

	/** Every fixture stores a BCrypt hash of {@code password}, never the raw value. */
	private Athelet athlete(String email, String password) {
		Athelet a = new Athelet();
		a.setEmail(email);
		a.setPassword(passwordEncoder.encode(password));
		return a;
	}

	private Admin admin(String email, String password) {
		Admin a = new Admin();
		a.setEmail(email);
		a.setPassword(passwordEncoder.encode(password));
		return a;
	}

	private Coach coach(String email, String password) {
		Coach c = new Coach();
		c.setEmail(email);
		c.setPassword(passwordEncoder.encode(password));
		return c;
	}

	@Nested
	@DisplayName("loadUserByUsername")
	class LoadUserByUsername {

		@Test
		@DisplayName("positive: an athlete resolves to an athlete principal with ROLE_ATHELET")
		void resolvesAthlete() {
			when(athr.findByEmail("a@example.com")).thenReturn(Optional.of(athlete("a@example.com", "pw")));

			UserDetails details = service.loadUserByUsername("a@example.com");

			assertThat(details).isInstanceOf(AtheletPrincilpals.class);
			assertThat(details.getUsername()).isEqualTo("a@example.com");
			assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
					.containsExactly("ROLE_ATHELET");
		}

		@Test
		@DisplayName("positive: falls through to admin when no athlete matches")
		void resolvesAdmin() {
			when(athr.findByEmail("ad@example.com")).thenReturn(Optional.empty());
			when(adr.findByEmail("ad@example.com")).thenReturn(Optional.of(admin("ad@example.com", "pw")));

			UserDetails details = service.loadUserByUsername("ad@example.com");

			assertThat(details).isInstanceOf(AdminPrinciple.class);
			assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
					.containsExactly("ROLE_ADMIN");
		}

		@Test
		@DisplayName("positive: falls through to coach when neither athlete nor admin matches")
		void resolvesCoach() {
			when(athr.findByEmail("c@example.com")).thenReturn(Optional.empty());
			when(adr.findByEmail("c@example.com")).thenReturn(Optional.empty());
			when(chr.findByEmail("c@example.com")).thenReturn(Optional.of(coach("c@example.com", "pw")));

			UserDetails details = service.loadUserByUsername("c@example.com");

			assertThat(details).isInstanceOf(CoachPrinciple.class);
			assertThat(details.getAuthorities()).extracting(GrantedAuthority::getAuthority)
					.containsExactly("ROLE_COACH");
		}

		@Test
		@DisplayName("negative: an unknown email throws UsernameNotFoundException, per the UserDetailsService contract")
		void unknownEmailThrows() {
			when(athr.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
			when(adr.findByEmail("nobody@example.com")).thenReturn(Optional.empty());
			when(chr.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.loadUserByUsername("nobody@example.com"))
					.isInstanceOf(UsernameNotFoundException.class)
					.hasMessageContaining("Coach not found");
		}
	}

	@Nested
	@DisplayName("addminsign")
	class AdminSign {

		@Test
		@DisplayName("positive: returns the admin when the password matches")
		void correctPassword() {
			Admin stored = admin("ad@example.com", "right");
			when(adr.findByEmail("ad@example.com")).thenReturn(Optional.of(stored));

			assertThat(service.addminsign("ad@example.com", "right")).isSameAs(stored);
		}

		@Test
		@DisplayName("negative: a wrong password is rejected")
		void wrongPassword() {
			when(adr.findByEmail("ad@example.com")).thenReturn(Optional.of(admin("ad@example.com", "right")));

			assertThatThrownBy(() -> service.addminsign("ad@example.com", "wrong"))
					.isInstanceOf(InvalidCredentialsException.class);
		}

		@Test
		@DisplayName("negative: an unknown email is rejected")
		void unknownEmail() {
			when(adr.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.addminsign("nobody@example.com", "pw"))
					.isInstanceOf(InvalidCredentialsException.class);
		}
	}

	@Nested
	@DisplayName("Athlethsign")
	class AthleteSign {

		@Test
		@DisplayName("positive: returns the athlete when the password matches")
		void correctPassword() {
			Athelet stored = athlete("a@example.com", "right");
			when(athr.findByEmail("a@example.com")).thenReturn(Optional.of(stored));

			assertThat(service.Athlethsign("a@example.com", "right")).isSameAs(stored);
		}

		@Test
		@DisplayName("negative: a wrong password is rejected")
		void wrongPassword() {
			when(athr.findByEmail("a@example.com")).thenReturn(Optional.of(athlete("a@example.com", "right")));

			assertThatThrownBy(() -> service.Athlethsign("a@example.com", "wrong"))
					.isInstanceOf(InvalidCredentialsException.class);
		}

		@Test
		@DisplayName("negative: an unknown email is rejected")
		void unknownEmail() {
			when(athr.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.Athlethsign("nobody@example.com", "pw"))
					.isInstanceOf(InvalidCredentialsException.class);
		}
	}

	@Nested
	@DisplayName("coachsign")
	class CoachSign {

		@Test
		@DisplayName("positive: returns the coach when the password matches")
		void correctPassword() {
			Coach stored = coach("c@example.com", "right");
			when(chr.findByEmail("c@example.com")).thenReturn(Optional.of(stored));

			assertThat(service.coachsign("c@example.com", "right")).isSameAs(stored);
		}

		@Test
		@DisplayName("negative: a wrong password is rejected")
		void wrongPassword() {
			when(chr.findByEmail("c@example.com")).thenReturn(Optional.of(coach("c@example.com", "right")));

			assertThatThrownBy(() -> service.coachsign("c@example.com", "wrong"))
					.isInstanceOf(InvalidCredentialsException.class);
		}

		@Test
		@DisplayName("negative: an unknown email is rejected")
		void unknownEmail() {
			when(chr.findByEmail("nobody@example.com")).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.coachsign("nobody@example.com", "pw"))
					.isInstanceOf(InvalidCredentialsException.class);
		}
	}

	@Test
	@DisplayName("security: a wrong password and an unknown email fail identically, so logins cannot enumerate accounts")
	void failureMessagesDoNotRevealWhetherTheAccountExists() {
		when(adr.findByEmail("real@example.com")).thenReturn(Optional.of(admin("real@example.com", "right")));
		when(adr.findByEmail("ghost@example.com")).thenReturn(Optional.empty());

		String wrongPasswordMessage = catchThrowable(
				() -> service.addminsign("real@example.com", "wrong")).getMessage();
		String unknownEmailMessage = catchThrowable(
				() -> service.addminsign("ghost@example.com", "wrong")).getMessage();

		assertThat(wrongPasswordMessage).isEqualTo(unknownEmailMessage);
	}
}
