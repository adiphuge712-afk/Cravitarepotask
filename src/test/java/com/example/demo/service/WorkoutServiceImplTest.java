package com.example.demo.service;

import com.example.demo.entity.Traningplan;
import com.example.demo.repository.Traningplanrepo;
import com.example.demo.entity.Workdirl;
import com.example.demo.repository.Workdrilrepo;
import com.example.demo.sse.WorkoutEventBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WorkoutServiceImplTest {

	@Mock
	private Workdrilrepo wdr;
	@Mock
	private Traningplanrepo tdr;
	@Mock
	private WorkoutEventBroadcaster broadcaster;

	@InjectMocks
	private WorkoutServiceImpl service;

	@Test
	@DisplayName("positive: getAllWorkdril returns every drill")
	void getAll() {
		List<Workdirl> all = List.of(new Workdirl());
		when(wdr.findAll()).thenReturn(all);

		assertThat(service.getAllWorkdril()).isEqualTo(all);
	}

	@Test
	@DisplayName("positive: getAllWorkdrilByCoachid queries by coach")
	void byCoach() {
		List<Workdirl> drills = List.of(new Workdirl());
		when(wdr.findByPlan_Coachid_Coachid(4L)).thenReturn(drills);

		assertThat(service.getAllWorkdrilByCoachid(4L)).isEqualTo(drills);
	}

	@Test
	@DisplayName("positive: addworkdril attaches the plan and stamps today's date")
	void addDrill() {
		Traningplan plan = new Traningplan();
		when(tdr.findById(6L)).thenReturn(Optional.of(plan));

		Workdirl drill = new Workdirl();
		when(wdr.save(drill)).thenReturn(drill);
		service.addworkdril(drill, 6L);

		assertThat(drill.getPlan()).isSameAs(plan);
		assertThat(drill.getStartdate()).isEqualTo(LocalDate.now());
		verify(wdr).save(drill);
		verify(broadcaster).publish(drill);
	}

	@Test
	@DisplayName("negative: addworkdril throws for an unknown plan, saves nothing, publishes nothing")
	void addDrillUnknownPlan() {
		when(tdr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.addworkdril(new Workdirl(), 404L))
				.isInstanceOf(RuntimeException.class);

		verify(wdr, never()).save(any(Workdirl.class));
		verifyNoInteractions(broadcaster);
	}

	@Test
	@DisplayName("positive: today's drills are queried with (coachId, date) in that order")
	void todaysDrills() {
		LocalDate today = LocalDate.now();
		List<Workdirl> drills = List.of(new Workdirl());
		when(wdr.findByPlan_Coachid_CoachidAndStartdate(4L, today)).thenReturn(drills);

		assertThat(service.getallworkdril_by_todays_date(today, 4L)).isEqualTo(drills);
		verify(wdr).findByPlan_Coachid_CoachidAndStartdate(4L, today);
	}

	@Test
	@DisplayName("positive: a single-date lookup delegates with (coachId, date)")
	void byDate() {
		LocalDate date = LocalDate.of(2026, 5, 4);
		when(wdr.findByPlan_Coachid_CoachidAndStartdate(4L, date)).thenReturn(List.of());

		assertThat(service.getallworkdrilBydate(date, 4L)).isEmpty();
		verify(wdr).findByPlan_Coachid_CoachidAndStartdate(4L, date);
	}

	@Test
	@DisplayName("positive: a date-range lookup delegates with (from, to, coachId) in that order")
	void byDateRange() {
		LocalDate from = LocalDate.of(2026, 5, 1);
		LocalDate to = LocalDate.of(2026, 5, 31);
		List<Workdirl> drills = List.of(new Workdirl());
		when(wdr.findByStartdateBetweenAndPlan_Coachid_Coachid(from, to, 4L)).thenReturn(drills);

		assertThat(service.getallworkdrilBydatetodate(from, to, 4L)).isEqualTo(drills);
		verify(wdr).findByStartdateBetweenAndPlan_Coachid_Coachid(from, to, 4L);
	}

	@Test
	@DisplayName("negative: a range with no drills yields an empty list, not an error")
	void byDateRangeEmpty() {
		LocalDate from = LocalDate.of(2026, 5, 1);
		LocalDate to = LocalDate.of(2026, 5, 31);
		when(wdr.findByStartdateBetweenAndPlan_Coachid_Coachid(from, to, 4L)).thenReturn(List.of());

		assertThat(service.getallworkdrilBydatetodate(from, to, 4L)).isEmpty();
	}
}
