package com.example.demo.service;

import java.util.List;

import com.example.demo.entity.Performancelog;

/**
 * Performance-log reads and upserts.
 *
 * <p>Note the parameter order: every method here takes {@code athid} before
 * {@code workid}. The previous {@code updatePerformancedata} took them the
 * other way round while its callers passed them in this order, so athletes
 * were looked up by work id and vice versa. Keeping one consistent order
 * across both upsert methods is what prevents that class of bug.
 */
public interface PerformanceService {

	List<Performancelog> getAllPerformlog();

	List<Performancelog> getPerformancelogByAtheletId(long athleteId);

	List<Performancelog> getPerformancelogById(long coachId);

	/**
	 * Creates the log for this athlete/workout pair, or updates it if one exists.
	 *
	 * @throws RuntimeException if the athlete or the workout does not exist
	 */
	void addPerformancedata(Performancelog per, long athid, long workid);

	/**
	 * Sets the completion status for this athlete/workout pair, creating the log if absent.
	 *
	 * @throws RuntimeException if the log is absent and the athlete or workout does not exist
	 */
	void updatePerformancedata(String data, long athid, long workid);
}
