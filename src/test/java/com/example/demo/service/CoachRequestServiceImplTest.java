package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.RequestRepo;
import com.example.demo.entity.Requestforacoach;
import com.example.demo.sse.CoachRequestEventBroadcaster;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
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
class CoachRequestServiceImplTest {

	@Mock
	private RequestRepo rdr;
	@Mock
	private Atheletrepo athr;
	@Mock
	private CoachRequestEventBroadcaster broadcaster;

	@InjectMocks
	private CoachRequestServiceImpl service;

	@Test
	@DisplayName("positive: first request for an athlete creates a new row, attached to that athlete")
	void firstRequestCreatesRow() {
		Athelet athlete = new Athelet();
		when(athr.findById(3L)).thenReturn(Optional.of(athlete));
		when(rdr.findFirstByAthid_AthidOrderByRqidDesc(3L)).thenReturn(Optional.empty());

		Requestforacoach req = new Requestforacoach();
		req.setRequest("Please assign me a coach");
		service.addrequest(req, 3L);

		ArgumentCaptor<Requestforacoach> captor = ArgumentCaptor.forClass(Requestforacoach.class);
		verify(rdr).save(captor.capture());
		assertThat(captor.getValue().getAthid()).isSameAs(athlete);
		assertThat(captor.getValue().getRequest()).isEqualTo("Please assign me a coach");
		verify(broadcaster).publishRequestSubmitted(3L);
	}

	@Test
	@DisplayName("REGRESSION: a second request from the same athlete updates the existing row instead of creating a duplicate")
	void secondRequestUpdatesExistingRow() {
		Athelet athlete = new Athelet();
		when(athr.findById(3L)).thenReturn(Optional.of(athlete));

		Requestforacoach existing = new Requestforacoach();
		existing.setRqid(7);
		existing.setRequest("First message");
		when(rdr.findFirstByAthid_AthidOrderByRqidDesc(3L)).thenReturn(Optional.of(existing));

		Requestforacoach resubmission = new Requestforacoach();
		resubmission.setRequest("Updated message");
		service.addrequest(resubmission, 3L);

		// The existing row is updated in place - same rqid, new text - never a
		// second insert. This is what used to crash viewrequestbyathelet once
		// two rows existed for one athlete.
		verify(rdr).save(existing);
		assertThat(existing.getRqid()).isEqualTo(7);
		assertThat(existing.getRequest()).isEqualTo("Updated message");
		verify(broadcaster).publishRequestSubmitted(3L);
	}

	@Test
	@DisplayName("negative: addrequest throws for an unknown athlete and saves nothing, publishes nothing")
	void unknownAthleteThrows() {
		when(athr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.addrequest(new Requestforacoach(), 404L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Athelet");

		verify(rdr, never()).save(any(Requestforacoach.class));
		verifyNoInteractions(broadcaster);
	}

	@Test
	@DisplayName("positive: viewrequestbyathelet returns the athlete's request when present")
	void viewByAthletePresent() {
		Requestforacoach req = new Requestforacoach();
		when(rdr.findFirstByAthid_AthidOrderByRqidDesc(3L)).thenReturn(Optional.of(req));

		assertThat(service.viewrequestbyathelet(3L)).contains(req);
	}

	@Test
	@DisplayName("negative: viewrequestbyathelet returns empty when the athlete has not asked")
	void viewByAthleteAbsent() {
		when(rdr.findFirstByAthid_AthidOrderByRqidDesc(404L)).thenReturn(Optional.empty());

		assertThat(service.viewrequestbyathelet(404L)).isEmpty();
	}

	@Test
	@DisplayName("positive: viewrequest returns every request")
	void viewAll() {
		List<Requestforacoach> all = List.of(new Requestforacoach());
		when(rdr.findAll()).thenReturn(all);

		assertThat(service.viewrequest()).isEqualTo(all);
	}

	@Test
	@DisplayName("positive: viewrequestByadminid scopes requests to one admin")
	void viewByAdmin() {
		List<Requestforacoach> scoped = List.of(new Requestforacoach());
		when(rdr.findByAthid_Coachid_Adid_Adminid(7L)).thenReturn(scoped);

		assertThat(service.viewrequestByadminid(7L)).isEqualTo(scoped);
	}

	@Test
	@DisplayName("negative: an admin with no requests yields an empty list")
	void viewByAdminEmpty() {
		when(rdr.findByAthid_Coachid_Adid_Adminid(7L)).thenReturn(List.of());

		assertThat(service.viewrequestByadminid(7L)).isEmpty();
	}
}
