package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Traningplan;

/** Training-plan lifecycle. */
public interface TrainingPlanService {

	List<Traningplan> getAllTraningplan();

	List<Traningplan> getAllTraningplan(long coachId);

	/** No-op if no plan has that id. */
	void deleteplanbyid(long id);

	/**
	 * @throws RuntimeException if no plan has that id
	 */
	Traningplan updateplanbyid(long id, Traningplan c);

	/**
	 * @throws RuntimeException if no coach has that id
	 */
	void addplan(Traningplan plan, long coachId);
}
