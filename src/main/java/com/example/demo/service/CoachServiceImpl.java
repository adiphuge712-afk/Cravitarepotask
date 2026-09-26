package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Coach;
import com.example.demo.dto.CoachUpdateRequest;
import com.example.demo.repository.Coachrepo;

@Service
public class CoachServiceImpl implements CoachService {

	private final Coachrepo chr;

	public CoachServiceImpl(Coachrepo chr) {
		this.chr = chr;
	}

	@Override
	public List<Coach> getAllCoach() {
		return chr.findAll();
	}

	@Override
	public Optional<Coach> getAllCoach(long id) {
		return chr.findById(id);
	}

	@Override
	public void deletecoachbyid(long id) {
		if (chr.existsById(id)) {
			chr.deleteById(id);
		}
	}

	@Override
	public Coach updatecoachbyid(long id, CoachUpdateRequest request) {
		Coach ch = chr.findById(id).orElseThrow(() -> new ResourceNotFoundException("Coach", id));
		ch.setName(request.getName());
		ch.setEmail(request.getEmail());
		ch.setAge(request.getAge());
		ch.setSpecialization(request.getSpecialization());
		ch.setExperience(request.getExperience());
		// Neither role nor password can be set from here: CoachUpdateRequest has
		// no fields for either, by construction. Password changes go through
		// the forgot-password flow instead of a generic "edit profile" form.
		return chr.save(ch);
	}
}
