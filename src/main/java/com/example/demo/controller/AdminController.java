package com.example.demo.controller;

import java.util.List;
import java.util.Optional;

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
import com.example.demo.service.CoachRequestService;
import com.example.demo.service.CoachService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.PerformanceService;
import com.example.demo.service.RegistrationService;
import com.example.demo.service.TrainingPlanService;
import com.example.demo.service.WorkoutService;
import com.example.demo.sse.CoachRequestEventBroadcaster;
import com.example.demo.sse.PerformanceEventBroadcaster;
import com.example.demo.sse.WorkoutEventBroadcaster;
import com.example.demo.entity.Requestforacoach;
import com.example.demo.entity.Traningplan;
import com.example.demo.entity.Workdirl;
import com.example.demo.dto.AthleteRegistrationRequest;
import com.example.demo.dto.CoachRegistrationRequest;


@RestController
//@CrossOrigin(origins = "http://localhost:5173")
@RequestMapping("/admin")
public class AdminController {
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
		private CoachRequestService coachRequestService;
		@Autowired
		private RegistrationService registrationService;
		@Autowired
		private PerformanceEventBroadcaster performanceEvents;
		@Autowired
		private CoachRequestEventBroadcaster coachRequestEvents;
		@Autowired
		private WorkoutEventBroadcaster workoutEvents;
		@GetMapping("/")
		public ResponseEntity<?> test() {
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Tested React ok admin");
		}
		@PostMapping("/registerathlet")
		public ResponseEntity<?> registration(@RequestBody AthleteRegistrationRequest request){
			registrationService.registerAthlete(request);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration success");
		}
		@PostMapping("/registercoach/{id}")
		public ResponseEntity<?> coachregistration(@RequestBody CoachRegistrationRequest request,@PathVariable long id){
			accessGuard.requireAccessToAdmin(id);
			registrationService.registerCoach(request, id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration success");
		}


		@GetMapping("/viewDataAthlet")
		public ResponseEntity<List<Athelet>> Athelet(){
			return ResponseEntity.ok(athleteService.getAllAthelet());
		}

		@GetMapping("/viewDataCoach")
		public ResponseEntity<List<Coach>> Coach(){
			return ResponseEntity.ok(coachService.getAllCoach());
		}
		@GetMapping("/viewDataCoach/{id}")
		public ResponseEntity<Optional<Coach>> Coach(@PathVariable long id){
			accessGuard.requireAccessToCoach(id);
			return ResponseEntity.ok(coachService.getAllCoach(id));
		}
		//delete the coach
		@DeleteMapping("/viewDataCoach/{id}")
		public ResponseEntity<?> deletebyid(@PathVariable long id){
			coachService.deletecoachbyid(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("delete");
		}
		//edit the coach
		@PutMapping("/viewDataCoach/{id}")
		public ResponseEntity<?> editbyid(@PathVariable long id,@RequestBody CoachUpdateRequest request){
			coachService.updatecoachbyid(id, request);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Edit");
		}
		//assigend the coach by athid and adminid

		@PostMapping("/AssigendCoach")
		public ResponseEntity<?> assingedcoach(@RequestBody Athelet a){

			athleteService.Assigendid(a.getAthid(),a.getCoachid().getCoachid());
			return ResponseEntity.ok("assigend");
		}
		@GetMapping("/viewDataTraningplan")
		public ResponseEntity<List<Traningplan>> viewDataTraningplan(){
			return ResponseEntity.ok(trainingPlanService.getAllTraningplan());
		}

		@GetMapping("/viewDataPerformancelog")
		public ResponseEntity<List<Performancelog>> viewDataPerformancelog(){
			return ResponseEntity.ok(performanceService.getAllPerformlog());
		}


		@GetMapping("/viewDataWorkdril")
		public ResponseEntity<List<Workdirl>> viewDataWorkdril(){
			return ResponseEntity.ok(workoutService.getAllWorkdril());
		}

	//for athelet date vise shechedule check
	//by coach id


		@GetMapping("/viewDataFeedback")
		public ResponseEntity<List<Feedback>> viewDataFeedback(){
			return ResponseEntity.ok(feedbackService.getAllFeedback());
		}
		//view fedd back by coachid

		//plan schedule

		//for coach plans see
		//addworkdrill
	//add the performance
		//for athelet to set the default complete
	//find the performance by coachid
	@GetMapping("/viewrequest")
	public ResponseEntity<?> viewrequest(){
		return ResponseEntity.ok(coachRequestService.viewrequest());
	}
	//by athelet id

	// Every performance-log change, live - an admin already has unrestricted
	// read access to this data via viewDataPerformancelog, so no additional
	// object-level check is needed here.
	@GetMapping("/sse/performance")
	public SseEmitter subscribePerformanceEvents() {
		return performanceEvents.subscribeAdmin();
	}

	// Every coach-request submission or assignment, live - same reasoning as
	// the performance stream above: an admin already reads all of this
	// unrestricted via viewrequest.
	@GetMapping("/sse/coach-requests")
	public SseEmitter subscribeCoachRequestEvents() {
		return coachRequestEvents.subscribeAdmin();
	}

	// Every drill added by any coach, academy-wide - same reasoning as the two
	// streams above: an admin already reads all of this unrestricted via
	// viewDataWorkdril.
	@GetMapping("/sse/schedule")
	public SseEmitter subscribeScheduleEvents() {
		return workoutEvents.subscribeAdmin();
	}

	// One connection instead of three - the admin dashboard had grown a
	// separate performance, coach-request and schedule stream, repeating the
	// exact problem /athelet/sse/events was built to avoid (see
	// Mycontroller.subscribeAllEvents): a browser allows only 6 concurrent
	// HTTP/1.1 connections per origin, and three long-lived streams on this
	// one page alone - on top of every coach and athlete page open in other
	// tabs - could exhaust that budget and stall ordinary page loads.
	@GetMapping("/sse/events")
	public SseEmitter subscribeAllEvents() {
		SseEmitter emitter = new SseEmitter(30L * 60 * 1000);

		performanceEvents.registerAdmin(emitter);
		coachRequestEvents.registerAdmin(emitter);
		workoutEvents.registerAdmin(emitter);

		return emitter;
	}
}
