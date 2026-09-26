package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Feedback;

/** Athlete complaints / feedback about coaches. */
public interface FeedbackService {

	List<Feedback> getAllFeedback();

	List<Feedback> getAllFeedbackbycoachid(long coachId);

	/**
	 * @throws RuntimeException if no athlete has that id
	 */
	void addComplain(Feedback f, long athleteId);
}
