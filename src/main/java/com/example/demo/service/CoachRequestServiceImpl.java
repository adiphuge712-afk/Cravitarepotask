package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.repository.RequestRepo;
import com.example.demo.entity.Requestforacoach;
import com.example.demo.sse.CoachRequestEventBroadcaster;

@Service
public class CoachRequestServiceImpl implements CoachRequestService {

	private final RequestRepo rdr;
	private final Atheletrepo athr;
	private final CoachRequestEventBroadcaster broadcaster;

	public CoachRequestServiceImpl(RequestRepo rdr, Atheletrepo athr, CoachRequestEventBroadcaster broadcaster) {
		this.rdr = rdr;
		this.athr = athr;
		this.broadcaster = broadcaster;
	}

	@Override
	public void addrequest(Requestforacoach req, long athleteId) {
		Athelet a = athr.findById(athleteId).orElseThrow(() -> new ResourceNotFoundException("Athelet", athleteId));

		// Upsert rather than always inserting: nothing previously stopped an
		// athlete submitting the form twice, which produced two rows for one
		// athlete and made every later "what's my request status?" lookup
		// throw (see findFirstByAthid_AthidOrderByRqidDesc). One outstanding
		// request per athlete also matches what the UI already assumes.
		Requestforacoach existing = rdr.findFirstByAthid_AthidOrderByRqidDesc(athleteId).orElse(null);
		if (existing != null) {
			existing.setRequest(req.getRequest());
			rdr.save(existing);
			broadcaster.publishRequestSubmitted(athleteId);
			return;
		}

		req.setAthid(a);
		rdr.save(req);
		broadcaster.publishRequestSubmitted(athleteId);
	}

	@Override
	public List<Requestforacoach> viewrequest() {
		return rdr.findAll();
	}

	@Override
	public List<Requestforacoach> viewrequestByadminid(long adminId) {
		return rdr.findByAthid_Coachid_Adid_Adminid(adminId);
	}

	@Override
	public Optional<Requestforacoach> viewrequestbyathelet(long athleteId) {
		return rdr.findFirstByAthid_AthidOrderByRqidDesc(athleteId);
	}
}
