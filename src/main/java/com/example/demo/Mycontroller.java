package com.example.demo;

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
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
//@CrossOrigin(origins = "http://localhost:5173")
public class Mycontroller {
//	@Autowired
//	JwtUtil jwtUtil;
	@Autowired
	Servicefile ss;
	@GetMapping("/")
	public ResponseEntity<?> test() {
		try {
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Tested React ok");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Fail");
			
		}
	}
	@PostMapping("/registerathlet")
	public ResponseEntity<?> registration(@RequestBody Athelet a){
		try {
			ss.addAthlete(a);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration fail");
		}
		
		
	}
	@PostMapping("/registercoach/{id}")
	public ResponseEntity<?> coachregistration(@RequestBody Coach s,@PathVariable long id){
		try {
//			ss.addCoach(s);
			ss.addcoachbyadid(s, id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration fail");
		}
		
		
	}

	
	@GetMapping("/viewDataAthlet")
	public ResponseEntity<List<Athelet>> Athelet(){
		try {
			List<Athelet> data=ss.getAllAthelet();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	@GetMapping("/viewDataAthlet/{id}")
	public ResponseEntity<Optional<Athelet>> Athelet(@PathVariable long id){
		try {
			Optional<Athelet> data=ss.getAllAthelet(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	

	@GetMapping("/viewDataCoach")
	public ResponseEntity<List<Coach>> Coach(){
		try {
			List<Coach> data=ss.getAllCoach();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	@GetMapping("/viewDataCoach/{id}")
	public ResponseEntity<Optional<Coach>> Coach(@PathVariable long id){
		try {
			Optional<Coach> data=ss.getAllCoach(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	//delete the coach
	@DeleteMapping("/viewDataCoach/{id}")
	public ResponseEntity<?> deletebyid(@PathVariable long id){
		try {
			ss.deletecoachbyid(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("delete");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	//edit the coach
	@PutMapping("/viewDataCoach/{id}")
	public ResponseEntity<?> editbyid(@PathVariable long id,@RequestBody Coach c){
		try {
			ss.updatecoachbyid(id,c);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Edit");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	//assigend the coach by athid and adminid
	
	@PostMapping("/AssigendCoach")
	public ResponseEntity<?> assingedcoach(@RequestBody Athelet a){
		
		ss.Assigendid(a.getAthid(),a.getCoachid().getCoachid());
		return ResponseEntity.ok("assigend");
	}
	@GetMapping("/viewDataTraningplan")
	public ResponseEntity<List<Traningplan>> viewDataTraningplan(){
		try {
			List<Traningplan> data=ss.getAllTraningplan();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	@GetMapping("/viewDataPerformancelog")
	public ResponseEntity<List<Performancelog>> viewDataPerformancelog(){
		try {
			List<Performancelog> data=ss.getAllPerformlog();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	
	
	@GetMapping("/viewDataWorkdril")
	public ResponseEntity<List<Workdirl>> viewDataWorkdril(){
		try {
			List<Workdirl> data=ss.getAllWorkdril();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
//by coach id
	@GetMapping("/viewDataWorkdrilByCoachid/{id}")
	public ResponseEntity<List<Workdirl>> viewDataWorkdrilByCoachid(@PathVariable long id){
		try {
			List<Workdirl> data=ss.getAllWorkdrilByCoachid(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	@GetMapping("/viewDataFeedback")
	public ResponseEntity<List<Feedback>> viewDataFeedback(){
		try {
			List<Feedback> data=ss.getAllFeedback();
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	//view fedd back by coachid 
	
	@GetMapping("/viewDataFeedback/{id}")
	public ResponseEntity<List<Feedback>> viewDataFeedbackbyid(@PathVariable long id){
		try {
			List<Feedback> data=ss.getAllFeedbackbycoachid(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	@PostMapping("/login")
	public ResponseEntity<?> loginUser(
	        @RequestBody LoginRequest request,
	        HttpSession session) {

	    if (request.getRole().equals("Admin")) {
			// 1️Check Admin
			Admin admin = ss.addminsign(request.getEmail(), request.getPassword());
			if (admin != null) {
				session.setAttribute("auser", admin);
				System.out.println("Session ID: " + session.getId());
				System.out.println("Session ID by couch: " + session.getId());
				System.out.println("Attributes: " + session.getAttribute("auser"));
				return ResponseEntity.ok(admin);
			} 
		}
	    else if (request.getRole().equals("Coach")) {
			// 2️Check Coach
			Coach coach = ss.coachsign(request.getEmail(), request.getPassword());
			if (coach != null) {
				session.setAttribute("cuser", coach);
				session.setAttribute("couch", "Session cosch");
				System.out.println("Session ID: " + session.getId());
				System.out.println("Session ID by couch: " + session.getId());
				System.out.println("Attributes: " + session.getAttribute("cuser"));

				return ResponseEntity.ok(coach);
			} 
		}
		else if (request.getRole().equals("Athelet")) {
			// 3️ Check Athlete
			Athelet athlete = ss.Athlethsign(request.getEmail(), request.getPassword());
			System.out.println(athlete.getName());//it give me right value aditya its not the null value 
			
			if (athlete != null) {
				session.setAttribute("athuser", athlete);
				System.out.println("Session ID: " + session.getId());
				System.out.println("Attributes: " + session.getAttribute("athuser"));

				return ResponseEntity.ok(athlete);
			} 
		}
		return ResponseEntity.ok(null);
	}
	@PostMapping("/logout")
	public ResponseEntity<String> logout(HttpSession session) {
		if (session!=null) {
			session.invalidate();
		}
		return ResponseEntity.ok("Session expired");
		
	}
	//check-session athlet
	@GetMapping("/checksession")
	public ResponseEntity<Boolean> checkSession(HttpSession session) {
	if(session.getAttribute("athuser")==null) {
		 return ResponseEntity
	                .ok(false);
	 }else {
		 
		 return ResponseEntity.ok(true);
		
	 }
	}
	//check-session admin
	@GetMapping("/checksessionadmin")
	public ResponseEntity<Boolean> checkSessionadmin(HttpSession session){
		
		 if(session.getAttribute("auser")==null) {
			 return ResponseEntity
		                .ok(false);
		 }else
		 return ResponseEntity.ok(true);
	}
	//check-session coach
	@GetMapping("/checksessioncoach")
	public ResponseEntity<Boolean> checkSessioncoach(HttpSession session){
		
		 if(session.getAttribute("cuser")==null) {
			 return ResponseEntity
		                .ok(false);
		 }else
		 return ResponseEntity.ok(true);
	}

	@PostMapping("/viewDataFeedback/{id}")
	public ResponseEntity<String> addcomplain(@RequestBody Feedback Feed,@PathVariable long id){
		try {
			System.out.println("Atheid id is :"+id);
			ss.addComplain(Feed,id);
			
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Feedback Add");
		} catch (Exception e) {
			System.out.println(e);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Fail to add");
		} 
	}

	@GetMapping("/viewDataAthletCoach/{id}")
	public ResponseEntity<List<Athelet>> AtheletCoach(@PathVariable long id){
		try {
			List<Athelet> data=ss.getAllatheletbycouchid(id);
			System.out.println(data.size());
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

	//plan schedule
	
	@PostMapping("/addPlan/{id}")
	public ResponseEntity<String> addPlan(
	        @RequestBody Traningplan feed,
	        @PathVariable long id) {

	    try {
	        System.out.println("Coach id is: " + id);
	        ss.addplan(feed, id);
	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body("Plan added successfully");

	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(e.getMessage());

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Failed to add plan");
	    }
	}

	//for coach plans see
	@GetMapping("/viewDataTraningplan/{id}")
	public ResponseEntity<List<Traningplan>> viewDataTraningplan(@PathVariable long id){
		try {
			List<Traningplan> data=ss.getAllTraningplan(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	@DeleteMapping("/viewDataTraningplan/{id}")
	public ResponseEntity<?> deleteplanbyid(@PathVariable long id){
		try {
			ss.deleteplanbyid(id);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("delete the plan");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	
	@PutMapping("/viewDataTraningplan/{id}")
	public ResponseEntity<?> editplanbyid(@PathVariable long id,@RequestBody Traningplan t){
		try {
			ss.updateplanbyid(id,t);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("plan updated");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	//addworkdrill
	@PostMapping("/addwork/{id}")
	public ResponseEntity<String> addwork(
	        @RequestBody Workdirl feed,
	        @PathVariable long id) {

	    try {
	        System.out.println("plan id is: " + id);
	        ss.addworkdril(feed, id);
	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body("work added successfully");

	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(e.getMessage());

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Failed to add work");
	    }
	}
//add the performance
	@PostMapping("/addDataPerformancelog/{id}/{wid}")
	public ResponseEntity<String> addperformance(
	        @RequestBody Performancelog feed,@PathVariable long wid,
	        @PathVariable long id) {

	    try {
	        System.out.println("plan id is: " + id);
	        ss.addPerformancedata(feed, id,wid);
	        return ResponseEntity.status(HttpStatus.CREATED)
	                .body("Performance added successfully");

	    } catch (RuntimeException e) {
	        return ResponseEntity.status(HttpStatus.NOT_FOUND)
	                .body(e.getMessage());

	    } catch (Exception e) {
	        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Failed to add performance");
	    }
	}
//find the performance by coachid
	@GetMapping("/viewDataPerformancelog/{id}")
	public ResponseEntity<List<Performancelog>> viewDataPerformancelogbyid(@PathVariable long id){
		try {
			List<Performancelog> data=ss.getPerformancelogById(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}
	@GetMapping("/viewDataPerformancelogAthid/{id}")
	public ResponseEntity<List<Performancelog>> viewDataPerformancelogbyatheletid(@PathVariable long id){
		try {
			List<Performancelog> data=ss.getPerformancelogByAtheletId(id);
			return ResponseEntity.ok(data);
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
		}
	}

@PostMapping("/addrequest/{id}")
public ResponseEntity<String> addrequest(@RequestBody Requestforacoach req,@PathVariable long id){
	try {
		ss.addrequest(req, id);
		return ResponseEntity.status(HttpStatus.CREATED).body("Request added successfully");
	} catch (Exception e) {
		 return ResponseEntity.status(HttpStatus.BAD_REQUEST)
	                .body("Failed to add Request");
	}
}
@GetMapping("/viewrequest")
public ResponseEntity<?> viewrequest(){
	try {
		List<Requestforacoach> data=ss.viewrequest();
		return ResponseEntity.ok(data);
	} catch (Exception e) {
		 return ResponseEntity.ok(null);
	}
}

}
