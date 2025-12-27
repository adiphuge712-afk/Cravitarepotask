package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Servicefile {
@Autowired
Adminrepo adr;
@Autowired
Atheletrepo athr;
@Autowired
Coachrepo chr;
@Autowired
Feedbackrepo fdr;
@Autowired
Performancerepo pdr;
@Autowired
Traningplanrepo tdr;
@Autowired
Workdrilrepo wdr;
public Admin addminsign(String email,String password) {

	Admin c=adr.findByEmail(email).orElseThrow(()->new RuntimeException("Email not found"));
	if(!c.getPassword().equals(password)) {
		throw new RuntimeException("Password not found");
	}
	return c;
}

public Athelet Athlethsign(String email,String password) {

	Athelet c=athr.findByEmail(email).orElseThrow(()->new RuntimeException("Email not found"));
	if(!c.getPassword().equals(password)) {
		throw new RuntimeException("Password not found");
	}
	return c;
}
public Coach coachsign(String email,String password) {

	Coach c=chr.findByEmail(email).orElseThrow(()->new RuntimeException("Email not found"));
	if(!c.getPassword().equals(password)) {
		throw new RuntimeException("Password not found");
	}
	return c;
}

public void addAthlete(Athelet a) {
	athr.save(a);
}
public void addCoach(Coach a) {
	chr.save(a);
}

public List<Athelet> getAllAthelet(){
	return athr.findAll();
}
public List<Traningplan> getAllTraningplan(){
	return tdr.findAll();
}
public List<Workdirl> getAllWorkdril(){
	return wdr.findAll();
}
public List<Feedback> getAllFeedback(){
	return fdr.findAll();
}
public List<Performancelog> getAllPerformlog(){
	return pdr.findAll();
}

public List<Coach> getAllCoach(){
	return chr.findAll();
}
Optional<Athelet> getAllAthelet(long id){
	return athr.findById(id);
}
Optional<Coach> getAllCoach(long id){
	return chr.findById(id);
}
}
