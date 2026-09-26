package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Admin;
import com.example.demo.repository.Adminrepo;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.dto.AthleteRegistrationRequest;
import com.example.demo.dto.CoachRegistrationRequest;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Positive and negative coverage for account registration - the code path
 * that carries the role mass-assignment fix.
 */
@ExtendWith(MockitoExtension.class)
class RegistrationServiceImplTest {

	@Mock
	private Atheletrepo athr;
	@Mock
	private Adminrepo adr;
	@Mock
	private Coachrepo chr;

	// A real encoder, not a mock: the property under test here is that a
	// password survives encode-then-matches with its original value, which a
	// mocked PasswordEncoder would just assert away rather than prove.
	private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

	private RegistrationServiceImpl service;

	private AthleteRegistrationRequest athleteRequest;
	private CoachRegistrationRequest coachRequest;

	@BeforeEach
	void setUp() {
		service = new RegistrationServiceImpl(athr, adr, chr, passwordEncoder);

		athleteRequest = new AthleteRegistrationRequest();
		athleteRequest.setName("Aditya");
		athleteRequest.setEmail("aditya@example.com");
		athleteRequest.setPassword("secret");
		athleteRequest.setAge(21);
		athleteRequest.setSporttype("Cricket");

		coachRequest = new CoachRegistrationRequest();
		coachRequest.setName("Coach Kapil");
		coachRequest.setAge(40);
		coachRequest.setEmail("coach@example.com");
		coachRequest.setPassword("secret");
		coachRequest.setSpecialization("Cricket");
		coachRequest.setExperience(10);
	}

	@Nested
	@DisplayName("registerAthlete")
	class RegisterAthlete {

		@Test
		@DisplayName("positive: persists the athlete with every field mapped")
		void persistsAllFields() {
			when(athr.save(any(Athelet.class))).thenAnswer(i -> i.getArgument(0));

			Athelet saved = service.registerAthlete(athleteRequest);

			ArgumentCaptor<Athelet> captor = ArgumentCaptor.forClass(Athelet.class);
			verify(athr).save(captor.capture());
			Athelet persisted = captor.getValue();

			assertThat(persisted.getName()).isEqualTo("Aditya");
			assertThat(persisted.getEmail()).isEqualTo("aditya@example.com");
			assertThat(persisted.getAge()).isEqualTo(21);
			assertThat(persisted.getSporttype()).isEqualTo("Cricket");
			assertThat(saved).isSameAs(persisted);
		}

		@Test
		@DisplayName("REGRESSION: the stored password is a BCrypt hash, never the plaintext, and it still validates the original password")
		void passwordIsHashedNotPlaintext() {
			when(athr.save(any(Athelet.class))).thenAnswer(i -> i.getArgument(0));

			Athelet saved = service.registerAthlete(athleteRequest);

			assertThat(saved.getPassword())
					.isNotEqualTo("secret")
					.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
			assertThat(passwordEncoder.matches("secret", saved.getPassword())).isTrue();
		}

		@Test
		@DisplayName("positive: role is always server-assigned to ATHELET")
		void alwaysAssignsAthletRole() {
			when(athr.save(any(Athelet.class))).thenAnswer(i -> i.getArgument(0));

			Athelet saved = service.registerAthlete(athleteRequest);

			assertThat(saved.getRole()).isEqualTo("ATHELET");
		}

		@Test
		@DisplayName("positive: touches only the athlete repository")
		void touchesOnlyAthleteRepo() {
			when(athr.save(any(Athelet.class))).thenAnswer(i -> i.getArgument(0));

			service.registerAthlete(athleteRequest);

			verifyNoInteractions(adr, chr);
		}

		@Test
		@DisplayName("negative: an empty request still yields an ATHELET, never a null role")
		void emptyRequestStillGetsAthletRole() {
			when(athr.save(any(Athelet.class))).thenAnswer(i -> i.getArgument(0));

			// Every other field is left unset on purpose - only the password has
			// to be non-null, because encoding null would NPE before the role
			// assignment under test ever runs.
			AthleteRegistrationRequest emptyRequest = new AthleteRegistrationRequest();
			emptyRequest.setPassword("irrelevant");

			Athelet saved = service.registerAthlete(emptyRequest);

			assertThat(saved.getRole()).isEqualTo("ATHELET");
			assertThat(saved.getName()).isNull();
			assertThat(saved.getAge()).isZero();
		}

		@Test
		@DisplayName("negative: a duplicate-email failure from the repository propagates")
		void propagatesRepositoryFailure() {
			doThrow(new RuntimeException("duplicate key value violates unique constraint"))
					.when(athr).save(any(Athelet.class));

			assertThatThrownBy(() -> service.registerAthlete(athleteRequest))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("duplicate key");
		}
	}

	@Nested
	@DisplayName("registerCoach")
	class RegisterCoach {

		@Test
		@DisplayName("positive: persists the coach with every field mapped and the admin linked")
		void persistsAllFieldsAndLinksAdmin() {
			Admin admin = new Admin();
			admin.setAdminid(7);
			when(adr.findById(7L)).thenReturn(Optional.of(admin));
			when(chr.save(any(Coach.class))).thenAnswer(i -> i.getArgument(0));

			Coach saved = service.registerCoach(coachRequest, 7L);

			ArgumentCaptor<Coach> captor = ArgumentCaptor.forClass(Coach.class);
			verify(chr).save(captor.capture());
			Coach persisted = captor.getValue();

			assertThat(persisted.getName()).isEqualTo("Coach Kapil");
			assertThat(persisted.getAge()).isEqualTo(40);
			assertThat(persisted.getEmail()).isEqualTo("coach@example.com");
			assertThat(persisted.getSpecialization()).isEqualTo("Cricket");
			assertThat(persisted.getExperience()).isEqualTo(10);
			assertThat(persisted.getAdid()).isSameAs(admin);
			assertThat(saved).isSameAs(persisted);
		}

		@Test
		@DisplayName("REGRESSION: the stored password is a BCrypt hash, never the plaintext, and it still validates the original password")
		void passwordIsHashedNotPlaintext() {
			when(adr.findById(7L)).thenReturn(Optional.of(new Admin()));
			when(chr.save(any(Coach.class))).thenAnswer(i -> i.getArgument(0));

			Coach saved = service.registerCoach(coachRequest, 7L);

			assertThat(saved.getPassword())
					.isNotEqualTo("secret")
					.matches("^\\$2[aby]\\$\\d{2}\\$.{53}$");
			assertThat(passwordEncoder.matches("secret", saved.getPassword())).isTrue();
		}

		@Test
		@DisplayName("positive: role is always server-assigned to COACH")
		void alwaysAssignsCoachRole() {
			Admin admin = new Admin();
			when(adr.findById(7L)).thenReturn(Optional.of(admin));
			when(chr.save(any(Coach.class))).thenAnswer(i -> i.getArgument(0));

			Coach saved = service.registerCoach(coachRequest, 7L);

			assertThat(saved.getRole()).isEqualTo("COACH");
		}

		@Test
		@DisplayName("negative: unknown admin id throws and nothing is saved")
		void unknownAdminIdThrowsAndSavesNothing() {
			when(adr.findById(404L)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.registerCoach(coachRequest, 404L))
					.isInstanceOf(ResourceNotFoundException.class)
					.hasMessageContaining("Admin not found");

			verify(chr, never()).save(any(Coach.class));
		}

		@Test
		@DisplayName("negative: a duplicate-email failure from the repository propagates")
		void propagatesRepositoryFailure() {
			when(adr.findById(7L)).thenReturn(Optional.of(new Admin()));
			doThrow(new RuntimeException("duplicate key value violates unique constraint"))
					.when(chr).save(any(Coach.class));

			assertThatThrownBy(() -> service.registerCoach(coachRequest, 7L))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("duplicate key");
		}
	}
}
