package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import com.example.demo.entity.Workdirl;

/** Workout-drill lifecycle and schedule queries. */
public interface WorkoutService {

	List<Workdirl> getAllWorkdril();

	List<Workdirl> getAllWorkdrilByCoachid(long coachId);

	/**
	 * @throws RuntimeException if no training plan has that id
	 */
	void addworkdril(Workdirl plan, long planId);

	List<Workdirl> getallworkdril_by_todays_date(LocalDate date, long coachId);

	List<Workdirl> getallworkdrilBydate(LocalDate date, long coachId);

	List<Workdirl> getallworkdrilBydatetodate(LocalDate from, LocalDate to, long coachId);
}
