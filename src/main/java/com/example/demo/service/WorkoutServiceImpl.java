package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Traningplan;
import com.example.demo.repository.Traningplanrepo;
import com.example.demo.entity.Workdirl;
import com.example.demo.repository.Workdrilrepo;
import com.example.demo.sse.WorkoutEventBroadcaster;

@Service
public class WorkoutServiceImpl implements WorkoutService {

	private final Workdrilrepo wdr;
	private final Traningplanrepo tdr;
	private final WorkoutEventBroadcaster broadcaster;

	public WorkoutServiceImpl(Workdrilrepo wdr, Traningplanrepo tdr, WorkoutEventBroadcaster broadcaster) {
		this.wdr = wdr;
		this.tdr = tdr;
		this.broadcaster = broadcaster;
	}

	@Override
	public List<Workdirl> getAllWorkdril() {
		return wdr.findAll();
	}

	@Override
	public List<Workdirl> getAllWorkdrilByCoachid(long coachId) {
		return wdr.findByPlan_Coachid_Coachid(coachId);
	}

	@Override
	public void addworkdril(Workdirl plan, long planId) {
		LocalDate date = LocalDate.now();
		Traningplan c = tdr.findById(planId).orElseThrow(() -> new ResourceNotFoundException("Training plan", planId));
		plan.setPlan(c);
		plan.setStartdate(date);
		Workdirl saved = wdr.save(plan);
		broadcaster.publish(saved);
	}

	@Override
	public List<Workdirl> getallworkdril_by_todays_date(LocalDate date, long coachId) {
		return wdr.findByPlan_Coachid_CoachidAndStartdate(coachId, date);
	}

	@Override
	public List<Workdirl> getallworkdrilBydate(LocalDate date, long coachId) {
		return wdr.findByPlan_Coachid_CoachidAndStartdate(coachId, date);
	}

	@Override
	public List<Workdirl> getallworkdrilBydatetodate(LocalDate from, LocalDate to, long coachId) {
		return wdr.findByStartdateBetweenAndPlan_Coachid_Coachid(from, to, coachId);
	}
}
