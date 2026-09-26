package com.example.demo.service;

import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Feedback;
import com.example.demo.repository.Feedbackrepo;
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
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FeedbackServiceImplTest {

	@Mock
	private Feedbackrepo fdr;
	@Mock
	private Atheletrepo athr;

	@InjectMocks
	private FeedbackServiceImpl service;

	@Test
	@DisplayName("positive: getAllFeedback returns every complaint")
	void getAll() {
		List<Feedback> all = List.of(new Feedback());
		when(fdr.findAll()).thenReturn(all);

		assertThat(service.getAllFeedback()).isEqualTo(all);
	}

	@Test
	@DisplayName("positive: getAllFeedbackbycoachid queries by the coach the athlete belongs to")
	void byCoach() {
		List<Feedback> feedback = List.of(new Feedback());
		when(fdr.findByAthid_Coachid_Coachid(4L)).thenReturn(feedback);

		assertThat(service.getAllFeedbackbycoachid(4L)).isEqualTo(feedback);
	}

	@Test
	@DisplayName("negative: a coach with no complaints yields an empty list")
	void byCoachEmpty() {
		when(fdr.findByAthid_Coachid_Coachid(4L)).thenReturn(List.of());

		assertThat(service.getAllFeedbackbycoachid(4L)).isEmpty();
	}

	@Test
	@DisplayName("positive: addComplain attaches the submitting athlete before saving")
	void addComplaint() {
		Athelet athlete = new Athelet();
		when(athr.findById(3L)).thenReturn(Optional.of(athlete));

		Feedback f = new Feedback();
		service.addComplain(f, 3L);

		assertThat(f.getAthid()).isSameAs(athlete);
		verify(fdr).save(f);
	}

	@Test
	@DisplayName("negative: addComplain throws for an unknown athlete and saves nothing")
	void addComplaintUnknownAthlete() {
		when(athr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.addComplain(new Feedback(), 404L))
				.isInstanceOf(RuntimeException.class)
				.hasMessageContaining("Athelet not found");

		verify(fdr, never()).save(any(Feedback.class));
	}
}
