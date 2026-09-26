package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Performancelog;
import com.example.demo.repository.Performancerepo;
import com.example.demo.entity.Workdirl;
import com.example.demo.repository.Workdrilrepo;
import com.example.demo.sse.PerformanceEventBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Positive and negative coverage for performance logging, including the
 * regression test for the swapped athlete-id / workout-id arguments.
 */
@ExtendWith(MockitoExtension.class)
class PerformanceServiceImplTest {

	private static final long ATHLETE_ID = 11L;
	private static final long WORK_ID = 22L;

	@Mock
	private Performancerepo pdr;
	@Mock
	private Atheletrepo athr;
	@Mock
	private Workdrilrepo wdr;
	@Mock
	private PerformanceEventBroadcaster broadcaster;

	@InjectMocks
	private PerformanceServiceImpl service;

	@Nested
	@DisplayName("reads")
	class Reads {

		@Test
		@DisplayName("positive: getAllPerformlog returns everything the repository has")
		void getAll() {
			List<Performancelog> logs = List.of(new Performancelog(), new Performancelog());
			when(pdr.findAll()).thenReturn(logs);

			assertThat(service.getAllPerformlog()).isEqualTo(logs);
		}

		@Test
		@DisplayName("positive: getPerformancelogByAtheletId queries by athlete id")
		void byAthleteId() {
			List<Performancelog> logs = List.of(new Performancelog());
			when(pdr.findByAthid_Athid(ATHLETE_ID)).thenReturn(logs);

			assertThat(service.getPerformancelogByAtheletId(ATHLETE_ID)).isEqualTo(logs);
		}

		@Test
		@DisplayName("positive: getPerformancelogById queries by coach id")
		void byCoachId() {
			List<Performancelog> logs = List.of(new Performancelog());
			when(pdr.findByAthid_Coachid_coachid(5L)).thenReturn(logs);

			assertThat(service.getPerformancelogById(5L)).isEqualTo(logs);
		}

		@Test
		@DisplayName("negative: no rows yields an empty list, not an error")
		void emptyIsNotAnError() {
			when(pdr.findByAthid_Athid(ATHLETE_ID)).thenReturn(List.of());

			assertThat(service.getPerformancelogByAtheletId(ATHLETE_ID)).isEmpty();
		}
	}

	@Nested
	@DisplayName("addPerformancedata")
	class AddPerformance {

		@Test
		@DisplayName("positive: creates a new log when none exists for the pair")
		void createsWhenAbsent() {
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			Athelet athlete = new Athelet();
			Workdirl work = new Workdirl();
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
			when(wdr.findById(WORK_ID)).thenReturn(Optional.of(work));

			Performancelog incoming = new Performancelog();
			incoming.setCompletestatus("DONE");

			service.addPerformancedata(incoming, ATHLETE_ID, WORK_ID);

			ArgumentCaptor<Performancelog> captor = ArgumentCaptor.forClass(Performancelog.class);
			verify(pdr).save(captor.capture());
			Performancelog saved = captor.getValue();
			assertThat(saved.getAthid()).isSameAs(athlete);
			assertThat(saved.getWorkid()).isSameAs(work);
			assertThat(saved.getCompletestatus()).isEqualTo("DONE");
			assertThat(saved.getDate()).isEqualTo(LocalDate.now());
		}

		@Test
		@DisplayName("positive: updates the existing log in place rather than inserting a duplicate")
		void updatesWhenPresent() {
			Performancelog existing = new Performancelog();
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(existing);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.of(new Athelet()));
			when(wdr.findById(WORK_ID)).thenReturn(Optional.of(new Workdirl()));

			Performancelog incoming = new Performancelog();
			incoming.setCompletestatus("DONE");
			incoming.setFatiquelevel("LOW");

			service.addPerformancedata(incoming, ATHLETE_ID, WORK_ID);

			verify(pdr).save(existing);
			assertThat(existing.getCompletestatus()).isEqualTo("DONE");
			assertThat(existing.getFatiquelevel()).isEqualTo("LOW");
		}

		@Test
		@DisplayName("negative: unknown athlete id throws and nothing is saved")
		void unknownAthlete() {
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.addPerformancedata(new Performancelog(), ATHLETE_ID, WORK_ID))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Athlete not found");

			verify(pdr, never()).save(any(Performancelog.class));
		}

		@Test
		@DisplayName("negative: unknown workout id throws and nothing is saved")
		void unknownWorkout() {
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.of(new Athelet()));
			when(wdr.findById(WORK_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.addPerformancedata(new Performancelog(), ATHLETE_ID, WORK_ID))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Workdril not found");

			verify(pdr, never()).save(any(Performancelog.class));
		}
	}

	@Nested
	@DisplayName("updatePerformancedata")
	class UpdatePerformance {

		@Test
		@DisplayName("REGRESSION: athlete id and workout id are used in their own slots, not swapped")
		void argumentsAreNotSwapped() {
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.of(new Athelet()));
			when(wdr.findById(WORK_ID)).thenReturn(Optional.of(new Workdirl()));

			service.updatePerformancedata("DONE", ATHLETE_ID, WORK_ID);

			// Before the fix the signature was (data, workid, athid) while every
			// caller passed (data, athid, workid), so the athlete was looked up by
			// work id and the workout by athlete id. These four verifications are
			// what fail if that ordering ever regresses.
			verify(pdr).findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID);
			verify(athr).findById(ATHLETE_ID);
			verify(wdr).findById(WORK_ID);
			verify(athr, never()).findById(WORK_ID);
		}

		@Test
		@DisplayName("positive: updates the completion status of an existing log")
		void updatesExisting() {
			Performancelog existing = new Performancelog();
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(existing);

			service.updatePerformancedata("COMPLETE", ATHLETE_ID, WORK_ID);

			verify(pdr).save(existing);
			assertThat(existing.getCompletestatus()).isEqualTo("COMPLETE");
			assertThat(existing.getDate()).isEqualTo(LocalDate.now());
		}

		@Test
		@DisplayName("positive: creates the log when the pair has none yet")
		void createsWhenAbsent() {
			Athelet athlete = new Athelet();
			Workdirl work = new Workdirl();
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.of(athlete));
			when(wdr.findById(WORK_ID)).thenReturn(Optional.of(work));

			service.updatePerformancedata("COMPLETE", ATHLETE_ID, WORK_ID);

			ArgumentCaptor<Performancelog> captor = ArgumentCaptor.forClass(Performancelog.class);
			verify(pdr).save(captor.capture());
			assertThat(captor.getValue().getAthid()).isSameAs(athlete);
			assertThat(captor.getValue().getWorkid()).isSameAs(work);
			assertThat(captor.getValue().getCompletestatus()).isEqualTo("COMPLETE");
		}

		@Test
		@DisplayName("negative: unknown athlete id throws when the log has to be created")
		void unknownAthlete() {
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.updatePerformancedata("DONE", ATHLETE_ID, WORK_ID))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Athlete not found");

			verify(pdr, never()).save(any(Performancelog.class));
		}

		@Test
		@DisplayName("negative: unknown workout id throws when the log has to be created")
		void unknownWorkout() {
			when(pdr.findByAthid_AthidAndWorkid_Workid(ATHLETE_ID, WORK_ID)).thenReturn(null);
			when(athr.findById(ATHLETE_ID)).thenReturn(Optional.of(new Athelet()));
			when(wdr.findById(WORK_ID)).thenReturn(Optional.empty());

			assertThatThrownBy(() -> service.updatePerformancedata("DONE", ATHLETE_ID, WORK_ID))
					.isInstanceOf(RuntimeException.class)
					.hasMessageContaining("Workdril not found");

			verify(pdr, never()).save(any(Performancelog.class));
		}
	}
}
