package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.sse.CoachRequestEventBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
class AthleteServiceImplTest {

	@Mock
	private Atheletrepo athr;
	@Mock
	private Coachrepo chr;
	@Mock
	private CoachRequestEventBroadcaster broadcaster;

	@InjectMocks
	private AthleteServiceImpl service;

	@Test
	@DisplayName("positive: getAllAthelet returns every athlete")
	void getAll() {
		List<Athelet> all = List.of(new Athelet(), new Athelet());
		when(athr.findAll()).thenReturn(all);

		assertThat(service.getAllAthelet()).isEqualTo(all);
	}

	@Test
	@DisplayName("positive: getAllAthelet(id) returns the athlete when present")
	void getByIdPresent() {
		Athelet a = new Athelet();
		when(athr.findById(3L)).thenReturn(Optional.of(a));

		assertThat(service.getAllAthelet(3L)).contains(a);
	}

	@Test
	@DisplayName("negative: getAllAthelet(id) returns empty for an unknown id rather than throwing")
	void getByIdAbsent() {
		when(athr.findById(404L)).thenReturn(Optional.empty());

		assertThat(service.getAllAthelet(404L)).isEmpty();
	}

	@Test
	@DisplayName("positive: getAllatheletbycouchid queries by coach id")
	void byCoachId() {
		List<Athelet> squad = List.of(new Athelet());
		when(athr.findByCoachid_Coachid(9L)).thenReturn(squad);

		assertThat(service.getAllatheletbycouchid(9L)).isEqualTo(squad);
	}

	@Test
	@DisplayName("negative: a coach with no athletes yields an empty list")
	void byCoachIdEmpty() {
		when(athr.findByCoachid_Coachid(9L)).thenReturn(List.of());

		assertThat(service.getAllatheletbycouchid(9L)).isEmpty();
	}

	@Test
	@DisplayName("positive: Assigendid links the coach onto the athlete and saves")
	void assignsCoach() {
		Coach coach = new Coach();
		Athelet athlete = new Athelet();
		when(chr.findById(7L)).thenReturn(Optional.of(coach));
		when(athr.findById(3L)).thenReturn(Optional.of(athlete));

		service.Assigendid(3L, 7L);

		assertThat(athlete.getCoachid()).isSameAs(coach);
		verify(athr).save(athlete);
		verify(broadcaster).publishCoachAssigned(3L);
	}

	@Test
	@DisplayName("negative: unknown coach id throws and saves nothing, publishes nothing")
	void unknownCoach() {
		when(chr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.Assigendid(3L, 404L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Coach not found");

		verify(athr, never()).save(any(Athelet.class));
		verifyNoInteractions(broadcaster);
	}

	@Test
	@DisplayName("negative: unknown athlete id throws and saves nothing, publishes nothing")
	void unknownAthlete() {
		when(chr.findById(7L)).thenReturn(Optional.of(new Coach()));
		when(athr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.Assigendid(404L, 7L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Athelet not found");

		verify(athr, never()).save(any(Athelet.class));
		verifyNoInteractions(broadcaster);
	}
}
