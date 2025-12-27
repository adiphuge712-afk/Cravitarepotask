package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpSession;

@RestController
@CrossOrigin("*")
public class Mycontroller {
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
	@PostMapping("/registercoach")
	public ResponseEntity<?> coachregistration(@RequestBody Coach s){
		try {
			ss.addCoach(s);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Registration success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Registration fail");
		}
		
		
	}
	@GetMapping("/loginadmin")
	public ResponseEntity<?> Login(@RequestParam String email,@RequestParam String password,HttpSession session){
		try {
		Admin a=ss.addminsign(email,password);
			session.setAttribute("auser", a);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Login success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login fail");
		}
		
	}
	
	
	
	@GetMapping("/loginathleth")
	public ResponseEntity<?> athletLogin(@RequestParam String email,@RequestParam String password,HttpSession session){
		try {
		Athelet a=ss.Athlethsign(email,password);
			session.setAttribute("athuser", a);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Login success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login fail");
		}
		
	}
	@GetMapping("/logincoach")
	public ResponseEntity<?> coachLogin(@RequestParam String email,@RequestParam String password,HttpSession session){
		try {
		Coach a=ss.coachsign(email,password);
			session.setAttribute("cuser", a);
			return ResponseEntity.status(HttpStatus.ACCEPTED).body("Login success");
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Login fail");
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
	
	

}
