package com.example.demo.controller;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
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
import com.example.demo.service.CoachRequestService;
import com.example.demo.service.FeedbackService;
import com.example.demo.service.PerformanceService;
import com.example.demo.service.RegistrationService;
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
@RequestMapping("/athelet")
public class Mycontroller {
	@Autowired
	private AccessGuard accessGuard;
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
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Tested React ok athelet");
	}
	@PostMapping("/registerathlet")
	public ResponseEntity<?> registration(@RequestBody AthleteRegistrationRequest request){
		registrationService.registerAthlete(request);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration success");
	}


	//delete the coach
	//edit the coach
	//assigend the coach by athid and adminid


//for athelet date vise shechedule check
	//for athelet date vise shechedule check date to date
		@GetMapping("/viewDataWorkdrilBydatetodate/{id}")
		public ResponseEntity<List<Workdirl>> viewDataWorkdrilBydatetodate(@RequestParam LocalDate date,@RequestParam LocalDate date2,@PathVariable long id){
			accessGuard.requireAccessToCoach(id);
			return ResponseEntity.ok(workoutService.getallworkdrilBydatetodate(date,date2,id));
		}
//by coach id
	@GetMapping("/viewDataWorkdrilByCoachid/{id}")
	public ResponseEntity<List<Workdirl>> viewDataWorkdrilByCoachid(@PathVariable long id){
		accessGuard.requireAccessToCoach(id);
		return ResponseEntity.ok(workoutService.getAllWorkdrilByCoachid(id));
	}


	@GetMapping("/viewDataWorkdrilByTodaysdateandCoachid/{id}")
	public ResponseEntity<List<Workdirl>> viewDataWorkdrilByTodaysdate(@PathVariable long id){
		accessGuard.requireAccessToCoach(id);
		LocalDate date=LocalDate.now(ZoneId.of("Asia/Kolkata"));
		return ResponseEntity.ok(workoutService.getallworkdril_by_todays_date(date,id));
	}


	//view fedd back by coachid

	@PostMapping("/viewDataFeedback/{id}")
	public ResponseEntity<String> addcomplain(@RequestBody Feedback Feed,@PathVariable long id){
		accessGuard.requireAccessToAthlete(id);
		feedbackService.addComplain(Feed,id);
		return ResponseEntity.status(HttpStatus.ACCEPTED).body("Feedback Add");
	}

	//plan schedule

	//for coach plans see
	//addworkdrill
//add the performance
	//for athelet to set the default complete
	@PutMapping("/updatePerformancelogs/{aid}/{wid}")
	public ResponseEntity<String> updateperformance(@RequestParam String data,@PathVariable long wid,
	        @PathVariable long aid) {
	        	accessGuard.requireAccessToAthlete(aid);

	    performanceService.updatePerformancedata(data,aid,wid);
	    return ResponseEntity.status(HttpStatus.CREATED)
	            .body("Performance added successfully");
	}
//find the performance by coachid
	@GetMapping("/viewDataPerformancelogAthid/{id}")
	public ResponseEntity<List<Performancelog>> viewDataPerformancelogbyatheletid(@PathVariable long id){
		accessGuard.requireAccessToAthlete(id);
		return ResponseEntity.ok(performanceService.getPerformancelogByAtheletId(id));
	}

@PostMapping("/addrequest/{id}")
public ResponseEntity<String> addrequest(@RequestBody Requestforacoach req,@PathVariable long id){
	accessGuard.requireAccessToAthlete(id);
	coachRequestService.addrequest(req, id);
	return ResponseEntity.status(HttpStatus.CREATED).body("Request added successfully");
}
//by athelet id
@GetMapping("/viewrequestbyathelet/{id}")
public ResponseEntity<?> viewrequestbyathelet(@PathVariable long id){
	accessGuard.requireAccessToAthlete(id);
	Optional<Requestforacoach> data=coachRequestService.viewrequestbyathelet(id);
	return ResponseEntity.ok(data);
}

// Only changes to this athlete's own logs. The id comes from the
// signed-in principal, never from the caller, so an athlete can never
// subscribe to another athlete's stream by passing a different id.
@GetMapping("/sse/performance")
public SseEmitter subscribePerformanceEvents(){
	return performanceEvents.subscribeAthlete(accessGuard.currentAthleteId());
}

// Only changes to this athlete's own coach request - submitting one, or an
// admin assigning a coach to them specifically.
@GetMapping("/sse/coach-requests")
public SseEmitter subscribeCoachRequestEvents(){
	return coachRequestEvents.subscribeAthlete(accessGuard.currentAthleteId());
}

// This athlete's own assigned coach's schedule - a new drill shows up here
// live instead of needing a page refresh to see today's training update.
@GetMapping("/sse/schedule")
public SseEmitter subscribeScheduleEvents(){
	return workoutEvents.subscribe(accessGuard.currentAssignedCoachId());
}

// One connection instead of three. Browsers cap concurrent HTTP/1.1
// connections to a single origin at 6; the athlete dashboard alone was
// opening up to three long-lived SSE streams (performance, schedule,
// coach-request), which - especially with more than one tab of this app
// open at once - could exhaust that shared budget and make the page's own
// data-fetching requests queue behind them instead of running, showing up
// as the dashboard hanging on its loading spinner. This subscribes the
// same one emitter to every topic the dashboard needs instead.
@GetMapping("/sse/events")
public SseEmitter subscribeAllEvents(){
	long athleteId = accessGuard.currentAthleteId();
	// Same lifetime as every single-topic SseEmitter these broadcasters mint
	// themselves (see e.g. PerformanceEventBroadcasterImpl.TIMEOUT_MS) -
	// EmitterRegistry that value comes from is package-private on purpose,
	// so this one has to be created here instead of borrowed from it.
	SseEmitter emitter = new SseEmitter(30L * 60 * 1000);

	performanceEvents.registerAthlete(athleteId, emitter);
	coachRequestEvents.registerAthlete(athleteId, emitter);
	try {
		workoutEvents.register(accessGuard.currentAssignedCoachId(), emitter);
	} catch (AccessDeniedException e) {
		// No coach assigned yet - nothing to watch on the schedule feed.
		// Performance and coach-request events still flow through above.
	}

	return emitter;
}
}
