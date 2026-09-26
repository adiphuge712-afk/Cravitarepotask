package com.example.demo.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.exception.ResourceNotFoundException;
import com.example.demo.entity.Athelet;
import com.example.demo.repository.Atheletrepo;
import com.example.demo.entity.Performancelog;
import com.example.demo.repository.Performancerepo;
import com.example.demo.entity.Workdirl;
import com.example.demo.repository.Workdrilrepo;
import com.example.demo.sse.PerformanceEventBroadcaster;

@Service
public class PerformanceServiceImpl implements PerformanceService {

	private final Performancerepo pdr;
	private final Atheletrepo athr;
	private final Workdrilrepo wdr;
	private final PerformanceEventBroadcaster broadcaster;

	public PerformanceServiceImpl(Performancerepo pdr, Atheletrepo athr, Workdrilrepo wdr,
			PerformanceEventBroadcaster broadcaster) {
		this.pdr = pdr;
		this.athr = athr;
		this.wdr = wdr;
		this.broadcaster = broadcaster;
	}

	@Override
	public List<Performancelog> getAllPerformlog() {
		return pdr.findAll();
	}

	@Override
	public List<Performancelog> getPerformancelogByAtheletId(long athleteId) {
		return pdr.findByAthid_Athid(athleteId);
	}

	@Override
	public List<Performancelog> getPerformancelogById(long coachId) {
		return pdr.findByAthid_Coachid_coachid(coachId);
	}

	@Override
	public void addPerformancedata(Performancelog per, long athid, long workid) {
		Performancelog existing = pdr.findByAthid_AthidAndWorkid_Workid(athid, workid);

		Athelet athlete = athr.findById(athid)
				.orElseThrow(() -> new ResourceNotFoundException("Athlete", athid));

		Workdirl work = wdr.findById(workid)
				.orElseThrow(() -> new ResourceNotFoundException("Workdril", workid));

		Performancelog saved;
		if (existing != null) {
			existing.setCompletestatus(per.getCompletestatus());
			existing.setPerformancematrix(per.getPerformancematrix());
			existing.setFatiquelevel(per.getFatiquelevel());
			existing.setDate(LocalDate.now());
			saved = pdr.save(existing);
		} else {
			per.setAthid(athlete);
			per.setWorkid(work);
			per.setDate(LocalDate.now());
			saved = pdr.save(per);
		}
		broadcaster.publish(saved);
	}

	@Override
	public void updatePerformancedata(String data, long athid, long workid) {
		Performancelog perform = pdr.findByAthid_AthidAndWorkid_Workid(athid, workid);
		LocalDate date = LocalDate.now();

		if (perform == null) {
			Athelet athlete = athr.findById(athid)
					.orElseThrow(() -> new ResourceNotFoundException("Athlete", athid));

			Workdirl work = wdr.findById(workid)
					.orElseThrow(() -> new ResourceNotFoundException("Workdril", workid));

			perform = new Performancelog();
			perform.setAthid(athlete);
			perform.setWorkid(work);
		}

		perform.setDate(date);
		perform.setCompletestatus(data);
		Performancelog saved = pdr.save(perform);
		broadcaster.publish(saved);
	}
}
