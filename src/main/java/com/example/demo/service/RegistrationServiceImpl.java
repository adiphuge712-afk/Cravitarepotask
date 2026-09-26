package com.example.demo.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Admin;
import com.example.demo.repository.Adminrepo;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.dto.AthleteRegistrationRequest;
import com.example.demo.dto.CoachRegistrationRequest;

/**
 * The single place that decides what role a newly registered account gets.
 */
@Service
public class RegistrationServiceImpl implements RegistrationService {

	private final Atheletrepo athr;
	private final Adminrepo adr;
	private final Coachrepo chr;
	private final PasswordEncoder passwordEncoder;

	public RegistrationServiceImpl(Atheletrepo athr, Adminrepo adr, Coachrepo chr, PasswordEncoder passwordEncoder) {
		this.athr = athr;
		this.adr = adr;
		this.chr = chr;
		this.passwordEncoder = passwordEncoder;
	}

	@Override
	public Athelet registerAthlete(AthleteRegistrationRequest request) {
		// Role is never read from the request: Athelet's constructor already
		// defaults it to "ATHELET" and we never call setRole() here, so a
		// client has no field through which to grant itself another role.
		Athelet athlete = new Athelet();
		athlete.setName(request.getName());
		athlete.setEmail(request.getEmail());
		athlete.setPassword(passwordEncoder.encode(request.getPassword()));
		athlete.setAge(request.getAge());
		athlete.setSporttype(request.getSporttype());
		return athr.save(athlete);
	}

	@Override
	public Coach registerCoach(CoachRegistrationRequest request, long adminId) {
		// Same rule as registerAthlete: role is server-assigned only, Coach's
		// constructor defaults it to "COACH" and it is never overwritten here.
		Admin admin = adr.findById(adminId).orElseThrow(() -> new ResourceNotFoundException("Admin", adminId));
		Coach coach = new Coach();
		coach.setName(request.getName());
		coach.setAge(request.getAge());
		coach.setEmail(request.getEmail());
		coach.setPassword(passwordEncoder.encode(request.getPassword()));
		coach.setSpecialization(request.getSpecialization());
		coach.setExperience(request.getExperience());
		coach.setAdid(admin);
		return chr.save(coach);
	}
}
