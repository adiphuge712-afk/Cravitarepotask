package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.sse.CoachRequestEventBroadcaster;

@Service
public class AthleteServiceImpl implements AthleteService {

	private final Atheletrepo athr;
	private final Coachrepo chr;
	private final CoachRequestEventBroadcaster broadcaster;

	public AthleteServiceImpl(Atheletrepo athr, Coachrepo chr, CoachRequestEventBroadcaster broadcaster) {
		this.athr = athr;
		this.chr = chr;
		this.broadcaster = broadcaster;
	}

	@Override
	public List<Athelet> getAllAthelet() {
		return athr.findAll();
	}

	@Override
	public Optional<Athelet> getAllAthelet(long id) {
		return athr.findById(id);
	}

	@Override
	public List<Athelet> getAllatheletbycouchid(long coachId) {
		return athr.findByCoachid_Coachid(coachId);
	}

	@Override
	public void Assigendid(long aid, long cid) {
		Coach ch = chr.findById(cid).orElseThrow(() -> new ResourceNotFoundException("Coach", cid));
		Athelet a = athr.findById(aid).orElseThrow(() -> new ResourceNotFoundException("Athelet", aid));
		a.setCoachid(ch);
		athr.save(a);
		broadcaster.publishCoachAssigned(aid);
	}
}
