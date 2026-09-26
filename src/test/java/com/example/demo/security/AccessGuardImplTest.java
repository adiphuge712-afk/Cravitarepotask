package com.example.demo.security;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.entity.Traningplan;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.Traningplanrepo;
import com.example.demo.security.principal.AdminPrinciple;
import com.example.demo.security.principal.AtheletPrincilpals;
import com.example.demo.security.principal.CoachPrinciple;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

/**
 * Object-level authorization. Role checks alone let any signed-in athlete
 * read and write every other athlete's records just by changing the id in
 * the URL; these are the checks that close that.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AccessGuardImplTest {

	@Mock private Atheletrepo atheletrepo;
	@Mock private Traningplanrepo traningplanrepo;

	private AccessGuardImpl guard;

	@BeforeEach
	void setUp() {
		guard = new AccessGuardImpl(atheletrepo, traningplanrepo);
	}

	@AfterEach
	void clearContext() {
		SecurityContextHolder.clearContext();
	}

	private static Coach coach(long id) {
		Coach c = new Coach();
		c.setCoachid(id);
		return c;
	}

	private static Athelet athlete(long id, Coach assigned) {
		Athelet a = new Athelet();
		a.setAthid(id);
		a.setCoachid(assigned);
		return a;
	}

	private void signedInAs(UserDetails principal) {
		SecurityContextHolder.getContext().setAuthentication(
				new UsernamePasswordAuthenticationToken(principal, null, List.of()));
	}

	private void signedInAsAthlete(long athleteId, Coach assigned) {
		signedInAs(new AtheletPrincilpals(athlete(athleteId, assigned)));
	}

	private void signedInAsCoach(long coachId) {
		signedInAs(new CoachPrinciple(coach(coachId)));
	}

	private void signedInAsAdmin() {
		Admin a = new Admin();
		a.setAdminid(7);
		signedInAs(new AdminPrinciple(a));
	}

	@Nested
	@DisplayName("requireAccessToAthlete")
	class AthleteAccess {

		@Test
		@DisplayName("positive: an athlete may reach their own record")
		void ownRecord() {
			signedInAsAthlete(3, coach(2));
			assertThatCode(() -> guard.requireAccessToAthlete(3)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("REGRESSION: an athlete may NOT reach another athlete's record")
		void otherAthletesRecord() {
			signedInAsAthlete(3, coach(2));
			assertThatThrownBy(() -> guard.requireAccessToAthlete(4))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("positive: a coach may reach an athlete on their own squad")
		void ownSquadMember() {
			signedInAsCoach(2);
			when(atheletrepo.findById(3L)).thenReturn(Optional.of(athlete(3, coach(2))));
			assertThatCode(() -> guard.requireAccessToAthlete(3)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("REGRESSION: a coach may NOT reach an athlete from another squad")
		void otherSquadMember() {
			signedInAsCoach(2);
			when(atheletrepo.findById(5L)).thenReturn(Optional.of(athlete(5, coach(3))));
			assertThatThrownBy(() -> guard.requireAccessToAthlete(5))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("negative: a coach may not reach an unassigned athlete")
		void unassignedAthlete() {
			signedInAsCoach(2);
			when(atheletrepo.findById(9L)).thenReturn(Optional.of(athlete(9, null)));
			assertThatThrownBy(() -> guard.requireAccessToAthlete(9))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("positive: an admin may reach any athlete")
		void adminSeesAll() {
			signedInAsAdmin();
			assertThatCode(() -> guard.requireAccessToAthlete(999)).doesNotThrowAnyException();
		}
	}

	@Nested
	@DisplayName("requireAccessToCoach")
	class CoachAccess {

		@Test
		@DisplayName("positive: a coach may reach their own record")
		void ownRecord() {
			signedInAsCoach(2);
			assertThatCode(() -> guard.requireAccessToCoach(2)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("REGRESSION: a coach may NOT reach another coach's record")
		void otherCoach() {
			signedInAsCoach(2);
			assertThatThrownBy(() -> guard.requireAccessToCoach(3))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("positive: an athlete may reach the coach they are assigned to")
		void ownCoach() {
			signedInAsAthlete(3, coach(2));
			assertThatCode(() -> guard.requireAccessToCoach(2)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("REGRESSION: an athlete may NOT reach a coach they are not assigned to")
		void otherCoachFromAthlete() {
			signedInAsAthlete(3, coach(2));
			assertThatThrownBy(() -> guard.requireAccessToCoach(3))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("negative: an athlete with no coach cannot reach any coach")
		void athleteWithoutCoach() {
			signedInAsAthlete(3, null);
			assertThatThrownBy(() -> guard.requireAccessToCoach(2))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Nested
	@DisplayName("requireAccessToPlan")
	class PlanAccess {

		private Traningplan plan(long planId, Coach owner) {
			Traningplan p = new Traningplan();
			p.setPlanid(planId);
			p.setCoachid(owner);
			return p;
		}

		@Test
		@DisplayName("positive: a coach may edit their own plan")
		void ownPlan() {
			signedInAsCoach(2);
			when(traningplanrepo.findById(10L)).thenReturn(Optional.of(plan(10, coach(2))));
			assertThatCode(() -> guard.requireAccessToPlan(10)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("REGRESSION: a coach may NOT edit another coach's plan")
		void otherCoachesPlan() {
			signedInAsCoach(2);
			when(traningplanrepo.findById(11L)).thenReturn(Optional.of(plan(11, coach(3))));
			assertThatThrownBy(() -> guard.requireAccessToPlan(11))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("negative: an athlete may never edit a plan")
		void athleteDenied() {
			signedInAsAthlete(3, coach(2));
			assertThatThrownBy(() -> guard.requireAccessToPlan(10))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Nested
	@DisplayName("requireAccessToAdmin")
	class AdminAccess {

		@Test
		@DisplayName("positive: an admin may reach their own record")
		void ownRecord() {
			signedInAsAdmin();
			assertThatCode(() -> guard.requireAccessToAdmin(7)).doesNotThrowAnyException();
		}

		@Test
		@DisplayName("REGRESSION: an admin may NOT act under another admin's id")
		void otherAdmin() {
			signedInAsAdmin();
			assertThatThrownBy(() -> guard.requireAccessToAdmin(8))
					.isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("negative: a coach may not reach admin records")
		void coachDenied() {
			signedInAsCoach(2);
			assertThatThrownBy(() -> guard.requireAccessToAdmin(7))
					.isInstanceOf(AccessDeniedException.class);
		}
	}

	@Test
	@DisplayName("SECURITY: with no authenticated caller at all, everything is denied")
	void unauthenticatedDenied() {
		SecurityContextHolder.clearContext();
		assertThatThrownBy(() -> guard.requireAccessToAthlete(1)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> guard.requireAccessToCoach(1)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> guard.requireAccessToPlan(1)).isInstanceOf(AccessDeniedException.class);
		assertThatThrownBy(() -> guard.requireAccessToAdmin(1)).isInstanceOf(AccessDeniedException.class);
	}

	@Nested
	@DisplayName("currentAssignedCoachId")
	class CurrentAssignedCoachId {

		@Test
		@DisplayName("positive: an athlete with a coach gets that coach's id")
		void athleteWithCoach() {
			signedInAsAthlete(3, coach(2));
			assertThat(guard.currentAssignedCoachId()).isEqualTo(2);
		}

		@Test
		@DisplayName("REGRESSION: an athlete with no coach assigned is denied, not given a bogus id")
		void athleteWithoutCoach() {
			signedInAsAthlete(3, null);
			assertThatThrownBy(guard::currentAssignedCoachId).isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("negative: a coach has no 'assigned coach' of their own")
		void coachDenied() {
			signedInAsCoach(2);
			assertThatThrownBy(guard::currentAssignedCoachId).isInstanceOf(AccessDeniedException.class);
		}

		@Test
		@DisplayName("negative: an admin has no 'assigned coach' either")
		void adminDenied() {
			signedInAsAdmin();
			assertThatThrownBy(guard::currentAssignedCoachId).isInstanceOf(AccessDeniedException.class);
		}
	}
}
