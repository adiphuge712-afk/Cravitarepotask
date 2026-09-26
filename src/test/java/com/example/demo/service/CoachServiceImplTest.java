package com.example.demo.service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Coach;
import com.example.demo.dto.CoachUpdateRequest;
import com.example.demo.repository.Coachrepo;
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
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CoachServiceImplTest {

	@Mock
	private Coachrepo chr;

	@InjectMocks
	private CoachServiceImpl service;

	private static Coach coachWith(String name, String email, String password) {
		Coach c = new Coach();
		c.setName(name);
		c.setEmail(email);
		c.setPassword(password);
		c.setAge(35);
		c.setSpecialization("Chess");
		c.setExperience(5);
		return c;
	}

	private static CoachUpdateRequest updateRequest(String name, String email) {
		CoachUpdateRequest r = new CoachUpdateRequest();
		r.setName(name);
		r.setEmail(email);
		r.setAge(41);
		r.setSpecialization("Cricket");
		r.setExperience(12);
		return r;
	}

	@Test
	@DisplayName("positive: getAllCoach returns every coach")
	void getAll() {
		List<Coach> all = List.of(new Coach(), new Coach());
		when(chr.findAll()).thenReturn(all);

		assertThat(service.getAllCoach()).isEqualTo(all);
	}

	@Test
	@DisplayName("positive: getAllCoach(id) returns the coach when present")
	void getByIdPresent() {
		Coach c = new Coach();
		when(chr.findById(2L)).thenReturn(Optional.of(c));

		assertThat(service.getAllCoach(2L)).contains(c);
	}

	@Test
	@DisplayName("negative: getAllCoach(id) returns empty for an unknown id")
	void getByIdAbsent() {
		when(chr.findById(404L)).thenReturn(Optional.empty());

		assertThat(service.getAllCoach(404L)).isEmpty();
	}

	@Test
	@DisplayName("positive: deletecoachbyid deletes an existing coach")
	void deleteExisting() {
		when(chr.existsById(2L)).thenReturn(true);

		service.deletecoachbyid(2L);

		verify(chr).deleteById(2L);
	}

	@Test
	@DisplayName("negative: deletecoachbyid is a no-op for an unknown id")
	void deleteAbsent() {
		when(chr.existsById(404L)).thenReturn(false);

		service.deletecoachbyid(404L);

		verify(chr, never()).deleteById(anyLong());
	}

	@Test
	@DisplayName("positive: updatecoachbyid copies the editable profile fields")
	void updateExisting() {
		Coach stored = coachWith("Old", "old@example.com", "oldpw");
		when(chr.findById(2L)).thenReturn(Optional.of(stored));
		when(chr.save(stored)).thenReturn(stored);

		Coach result = service.updatecoachbyid(2L, updateRequest("New", "new@example.com"));

		assertThat(result.getName()).isEqualTo("New");
		assertThat(result.getEmail()).isEqualTo("new@example.com");
		assertThat(result.getAge()).isEqualTo(41);
		assertThat(result.getSpecialization()).isEqualTo("Cricket");
		assertThat(result.getExperience()).isEqualTo(12);
		verify(chr).save(stored);
	}

	@Test
	@DisplayName("positive: updatecoachbyid never lets an inbound payload change the stored role")
	void updateCannotChangeRole() {
		Coach stored = coachWith("Old", "old@example.com", "oldpw");
		when(chr.findById(2L)).thenReturn(Optional.of(stored));
		when(chr.save(stored)).thenReturn(stored);

		Coach result = service.updatecoachbyid(2L, updateRequest("New", "new@example.com"));

		assertThat(result.getRole()).isEqualTo("COACH");
	}

	@Test
	@DisplayName("REGRESSION: updatecoachbyid cannot change the stored password - CoachUpdateRequest has no such field")
	void updateCannotChangePassword() {
		Coach stored = coachWith("Old", "old@example.com", "original-password");
		when(chr.findById(2L)).thenReturn(Optional.of(stored));
		when(chr.save(stored)).thenReturn(stored);

		// There is no setter on CoachUpdateRequest for a password, so there is
		// nothing an "edit profile" payload could put here even if it tried -
		// this is what closes the admin UI's coach-edit-password mistake at
		// the API boundary, not just by hiding the field in the form.
		Coach result = service.updatecoachbyid(2L, updateRequest("New", "new@example.com"));

		assertThat(result.getPassword()).isEqualTo("original-password");
	}

	@Test
	@DisplayName("negative: updatecoachbyid throws for an unknown id and saves nothing")
	void updateAbsent() {
		when(chr.findById(404L)).thenReturn(Optional.empty());

		assertThatThrownBy(() -> service.updatecoachbyid(404L, updateRequest("New", "new@example.com")))
				.isInstanceOf(ResourceNotFoundException.class)
				.hasMessageContaining("Coach not found");

		verify(chr, never()).save(org.mockito.ArgumentMatchers.any(Coach.class));
	}
}
