package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Coach;
import com.example.demo.repository.Coachrepo;
import com.example.demo.entity.Traningplan;
import com.example.demo.repository.Traningplanrepo;

@Service
public class TrainingPlanServiceImpl implements TrainingPlanService {

	private final Traningplanrepo tdr;
	private final Coachrepo chr;

	public TrainingPlanServiceImpl(Traningplanrepo tdr, Coachrepo chr) {
		this.tdr = tdr;
		this.chr = chr;
	}

	@Override
	public List<Traningplan> getAllTraningplan() {
		return tdr.findAll();
	}

	@Override
	public List<Traningplan> getAllTraningplan(long coachId) {
		return tdr.findByCoachid_Coachid(coachId);
	}

	@Override
	public void deleteplanbyid(long id) {
		if (tdr.existsById(id)) {
			tdr.deleteById(id);
		}
	}

	@Override
	public Traningplan updateplanbyid(long id, Traningplan c) {
		Traningplan ch = tdr.findById(id).orElseThrow(() -> new ResourceNotFoundException("Training plan", id));
		ch.setEnddate(c.getEnddate());
		ch.setPlanname(c.getPlanname());
		ch.setPlantype(c.getPlantype());
		ch.setStartdate(c.getStartdate());
		return tdr.save(ch);
	}

	@Override
	public void addplan(Traningplan plan, long coachId) {
		Coach c = chr.findById(coachId).orElseThrow(() -> new ResourceNotFoundException("Coach", coachId));
		plan.setCoachid(c);
		tdr.save(plan);
	}
}
