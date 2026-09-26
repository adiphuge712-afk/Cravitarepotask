package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Feedback;
import com.example.demo.repository.Feedbackrepo;

@Service
public class FeedbackServiceImpl implements FeedbackService {

	private final Feedbackrepo fdr;
	private final Atheletrepo athr;

	public FeedbackServiceImpl(Feedbackrepo fdr, Atheletrepo athr) {
		this.fdr = fdr;
		this.athr = athr;
	}

	@Override
	public List<Feedback> getAllFeedback() {
		return fdr.findAll();
	}

	@Override
	public List<Feedback> getAllFeedbackbycoachid(long coachId) {
		return fdr.findByAthid_Coachid_Coachid(coachId);
	}

	@Override
	public void addComplain(Feedback f, long athleteId) {
		Athelet a = athr.findById(athleteId).orElseThrow(() -> new ResourceNotFoundException("Athelet", athleteId));
		f.setAthid(a);
		fdr.save(f);
	}
}
