package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.entity.Traningplan;
import com.example.demo.repository.Traningplanrepo;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TrainingPlanServiceImplTest {

	@Mock
	private Traningplanrepo tdr;
	@Mock
	private Coachrepo chr;

	@InjectMocks
	private TrainingPlanServiceImpl service;

	@Test
	@DisplayName("positive: getAllTraningplan returns every plan")
	void getAll() {
		List<Traningplan> all = List.of(new Traningplan());
		when(tdr.findAll()).thenReturn(all);

		assertThat(service.getAllTraningplan()).isEqualTo(all);
	}

	@Test
	@DisplayName("positive: getAllTraningplan(coachId) queries by coach")
	void getByCoach() {
		List<Traningplan> plans = List.of(new Traningplan());
		when(tdr.findByCoachid_Coachid(4L)).thenReturn(plans);

		assertThat(service.getAllTraningplan(4L)).isEqualTo(plans);
	}

	@Test
	@DisplayName("negative: a coach with no plans yields an empty list")
	void getByCoachEmpty() {
		when(tdr.findByCoachid_Coachid(4L)).thenReturn(List.of());

		assertThat(service.getAllTraningplan(4L)).isEmpty();
	}

	@Test
	@DisplayName("positive: deleteplanbyid deletes an existing plan")
	void deleteExisting() {
		when(tdr.existsById(6L)).thenReturn(true);

		service.deleteplanbyid(6L);

		verify(tdr).deleteById(6L);
	}

	@Test
	@DisplayName("negative: deleteplanbyid is a no-op for an unknown id")
	void deleteAbsent() {
		when(tdr.existsById(404L)).thenReturn(false);

		service.deleteplanbyid(404L);

		verify(tdr, never()).deleteById(anyLong());
	}

	@Test
	@DisplayName("positive: updateplanbyid copies the editable plan fields")
	void updateExisting() {
		Traningplan stored = new Traningplan();
		when(tdr.findById(6L)).thenReturn(Optional.of(stored));
		when(tdr.save(stored)).thenReturn(stored);

		Traningplan incoming = new Traningplan();
		incoming.setPlanname("Pre-season");
		incoming.setPlantype("Strength");
		incoming.setStartdate(LocalDate.of(2026, 1, 1));
		incoming.setEnddate(LocalDate.of(2026, 3, 1));

		Traningplan result = service.updateplanbyid(6L, incoming);

		assertThat(result.getPlanname()).isEqualTo("Pre-season");
		assertThat(result.getPlantype()).isEqualTo("Strength");
		assertThat(result.getStartdate()).isEqualTo(LocalDate.of(2026, 1, 1));
		assertThat(result.getEnddate()).isEqualTo(LocalDate.of(2026, 3, 1));
	}

	@Test
	@DisplayName("negative: updateplanbyid throws for an unknown id and saves nothing")
	void updateAbsent() {
		when(tdr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updateplanbyid(404L, new Traningplan()))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Training plan not found");

		verify(tdr, never()).save(any(Traningplan.class));
	}

	@Test
	@DisplayName("positive: addplan attaches the owning coach before saving")
	void addPlan() {
		Coach coach = new Coach();
		when(chr.findById(4L)).thenReturn(Optional.of(coach));

		Traningplan plan = new Traningplan();
		service.addplan(plan, 4L);

		assertThat(plan.getCoachid()).isSameAs(coach);
		verify(tdr).save(plan);
	}

	@Test
	@DisplayName("negative: addplan throws for an unknown coach and saves nothing")
	void addPlanUnknownCoach() {
		when(chr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.addplan(new Traningplan(), 404L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Coach not found");

		verify(tdr, never()).save(any(Traningplan.class));
	}
}
