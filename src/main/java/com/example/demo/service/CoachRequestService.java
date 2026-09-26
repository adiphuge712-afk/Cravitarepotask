package com.example.demo.service;

import java.util.List;
import java.util.Optional;

import com.example.demo.entity.Requestforacoach;

/** Athlete requests for a coach, and the admin views over them. */
public interface CoachRequestService {

	/**
	 * @throws RuntimeException if no athlete has that id
	 */
	void addrequest(Requestforacoach req, long athleteId);

	List<Requestforacoach> viewrequest();

	List<Requestforacoach> viewrequestByadminid(long adminId);

	Optional<Requestforacoach> viewrequestbyathelet(long athleteId);
}
