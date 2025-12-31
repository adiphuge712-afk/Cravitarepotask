package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin(origins = "http://localhost:5173"
)
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
	@GetMapping("/viewDataFeedback")
	public ResponseEntity<List<Feedback>> viewDataFeedback(){
		try {
			List<Feedback> data=ss.getAllFeedback();
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
				
				return ResponseEntity.ok(admin);
			} 
		}
	    else if (request.getRole().equals("Coach")) {
			// 2️Check Coach
			Coach coach = ss.coachsign(request.getEmail(), request.getPassword());
			if (coach != null) {
				session.setAttribute("cuser", coach);
		
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




}
