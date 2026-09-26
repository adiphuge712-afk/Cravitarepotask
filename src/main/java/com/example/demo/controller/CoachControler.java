package com.example.demo.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import com.example.demo.entity.Admin;
import com.example.demo.entity.Athelet;
import com.example.demo.entity.Coach;
import com.example.demo.entity.Feedback;
import com.example.demo.dto.CoachUpdateRequest;
import com.example.demo.entity.Performancelog;
import com.example.demo.security.AccessGuard;
import com.example.demo.service.AthleteService;
import com.example.demo.service.CoachService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.PerformanceService;
import com.example.demo.service.TrainingPlanService;
import com.example.demo.service.WorkoutService;
import com.example.demo.sse.PerformanceEventBroadcaster;
import com.example.demo.sse.WorkoutEventBroadcaster;
import com.example.demo.entity.Requestforacoach;
import com.example.demo.entity.Traningplan;
import com.example.demo.entity.Workdirl;
import com.example.demo.dto.AthleteRegistrationRequest;
import com.example.demo.dto.CoachRegistrationRequest;


@RestController
//@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/coach")
public class CoachControler {
	@Autowired
	private AccessGuard accessGuard;
	@Autowired
	private AthleteService athleteService;
	@Autowired
	private CoachService coachService;
	@Autowired
	private TrainingPlanService trainingPlanService;
	@Autowired
	private WorkoutService workoutService;
	@Autowired
	private FeedbackService feedbackService;
	@Autowired
	private PerformanceService performanceService;
	@Autowired
	private PerformanceEventBroadcaster performanceEvents;
	@Autowired
	private WorkoutEventBroadcaster workoutEvents;

	@GetMapping("/")
	public ResponseEntity<?> test() {
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Tested React ok coach");
	}

	@GetMapping("/viewDataAthlet")
	public ResponseEntity<List<Athelet>> Athelet() {
		return ResponseEntity.ok(athleteService.getAllAthelet());
	}

	@GetMapping("/viewDataCoach")
	public ResponseEntity<List<Coach>> Coach() {
		return ResponseEntity.ok(coachService.getAllCoach());
	}

	// delete the coach
	// edit the coach
	// assigend the coach by athid and adminid

	// for athelet date vise shechedule check
	// by coach id
	@GetMapping("/viewDataWorkdrilByCoachid/{id}")
	public ResponseEntity<List<Workdirl>> viewDataWorkdrilByCoachid(@PathVariable long id) {
		accessGuard.requireAccessToCoach(id);
		return ResponseEntity.ok(workoutService.getAllWorkdrilByCoachid(id));
	}

	// view fedd back by coachid

	@GetMapping("/viewDataFeedback/{id}")
	public ResponseEntity<List<Feedback>> viewDataFeedbackbyid(@PathVariable long id) {
		accessGuard.requireAccessToCoach(id);
		return ResponseEntity.ok(feedbackService.getAllFeedbackbycoachid(id));
	}


	@GetMapping("/viewDataAthletCoach/{id}")
	public ResponseEntity<List<Athelet>> AtheletCoach(@PathVariable long id) {
		accessGuard.requireAccessToCoach(id);
		return ResponseEntity.ok(athleteService.getAllatheletbycouchid(id));
	}

	// plan schedule

	@PostMapping("/addPlan/{id}")
	public ResponseEntity<String> addPlan(@RequestBody Traningplan feed, @PathVariable long id) {
		accessGuard.requireAccessToCoach(id);
		trainingPlanService.addplan(feed, id);
		return ResponseEntity.status(HttpStatus.CREATED).body("Plan added successfully");
	}

	// for coach plans see
	@GetMapping("/viewDataTraningplan/{id}")
	public ResponseEntity<List<Traningplan>> viewDataTraningplan(@PathVariable long id) {
		accessGuard.requireAccessToCoach(id);
		return ResponseEntity.ok(trainingPlanService.getAllTraningplan(id));
	}

	@DeleteMapping("/viewDataTraningplan/{id}")
	public ResponseEntity<?> deleteplanbyid(@PathVariable long id) {
		accessGuard.requireAccessToPlan(id);
		trainingPlanService.deleteplanbyid(id);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("delete the plan");
	}

	@PutMapping("/viewDataTraningplan/{id}")
	public ResponseEntity<?> editplanbyid(@PathVariable long id, @RequestBody Traningplan t) {
		accessGuard.requireAccessToPlan(id);
		trainingPlanService.updateplanbyid(id, t);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("plan updated");
	}

	// addworkdrill
	@PostMapping("/addwork/{id}")
	public ResponseEntity<String> addwork(@RequestBody Workdirl feed, @PathVariable long id) {
		accessGuard.requireAccessToPlan(id);
		workoutService.addworkdril(feed, id);
		return ResponseEntity.status(HttpStatus.CREATED).body("work added successfully");
	}

	// add the performance
	@PostMapping("/addDataPerformancelog/{id}/{wid}")
	public ResponseEntity<String> addperformance(@RequestBody Performancelog feed, @PathVariable long wid,
			@PathVariable long id) {
				accessGuard.requireAccessToAthlete(id);
		performanceService.addPerformancedata(feed, id, wid);
		return ResponseEntity.status(HttpStatus.CREATED).body("Performance added successfully");
	}

	@PutMapping("/updatePerformancelog/{aid}/{wid}")
	public ResponseEntity<String> updateperformance(@RequestBody Performancelog data, @PathVariable long wid,
			@PathVariable long aid) {
				accessGuard.requireAccessToAthlete(aid);
		// addPerformancedata, not updatePerformancedata: that other method only
		// ever takes a bare completestatus string (it exists for the athlete's
		// self-complete endpoint, which never submits a performance matrix or
		// fatigue level at all). Calling it here silently dropped both fields
		// on every save through this endpoint - addPerformancedata is the one
		// that actually persists the full body, and it already upserts
		// correctly (used by the sibling "add" endpoint above).
		performanceService.addPerformancedata(data, aid, wid);
		return ResponseEntity.status(HttpStatus.OK).body("Performance updated successfully");
	}

	// for athelet to set the default complete
	// find the performance by coachid
	@GetMapping("/viewDataPerformancelog/{id}")
	public ResponseEntity<List<Performancelog>> viewDataPerformancelogbyid(@PathVariable long id) {
		accessGuard.requireAccessToCoach(id);
		return ResponseEntity.ok(performanceService.getPerformancelogById(id));
	}

	// by athelet id

	// Only changes affecting this coach's own squad. The id comes from the
	// signed-in principal, never from the caller, so a coach can never
	// subscribe to another coach's stream by passing a different id.
	@GetMapping("/sse/performance")
	public SseEmitter subscribePerformanceEvents() {
		return performanceEvents.subscribeCoach(accessGuard.currentCoachId());
	}

	// This coach's own schedule - so adding a drill from one open tab shows
	// up live in another, the same way it does for their athletes.
	@GetMapping("/sse/schedule")
	public SseEmitter subscribeScheduleEvents() {
		return workoutEvents.subscribe(accessGuard.currentCoachId());
	}

	// One connection instead of two - browsers cap concurrent HTTP/1.1
	// connections to a single origin at 6, and every coach page had grown its
	// own separate performance + schedule streams, which repeated the exact
	// problem that /athelet/sse/events exists to avoid (see
	// Mycontroller.subscribeAllEvents for the full reasoning): with several
	// coach pages or tabs open at once, those streams alone could eat most of
	// that budget and leave ordinary page loads stuck on their spinner.
	@GetMapping("/sse/events")
	public SseEmitter subscribeAllEvents() {
		long coachId = accessGuard.currentCoachId();
		SseEmitter emitter = new SseEmitter(30L * 60 * 1000);

		performanceEvents.registerCoach(coachId, emitter);
		workoutEvents.register(coachId, emitter);

		return emitter;
	}
}
