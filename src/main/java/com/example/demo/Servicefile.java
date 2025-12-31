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
public void addcoachbyadid(Coach a,long id) {
	Admin ad=adr.findById(id).orElseThrow(()-> new RuntimeException("Id not found"));
	a.setAdid(ad);
	chr.save(a);
}
//public void addCoach(Coach a) {
//	chr.save(a);
//}

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

public void deletecoachbyid(long id) {
	if (chr.existsById(id)) {
		chr.deleteById(id);
	}else {
		System.out.println("Coach id not found");
	}
	
}
public Coach updatecoachbyid(long id,Coach c){
	Coach ch=chr.findById(id).orElseThrow(()->new RuntimeException("Id not found"));
	ch.setName(c.getName());
	ch.setEmail(c.getEmail());
	ch.setPassword(c.getPassword());
	ch.setAge(c.getAge());
	ch.setSpecialization(c.getSpecialization());
	ch.setExperience(c.getExperience());
	return chr.save(ch);
	
}

public void Assigendid(long aid,long cid) {
	
		Coach ch=chr.findById(cid).orElseThrow(()->new RuntimeException("Coach not found"));
		Athelet a=athr.findById(aid).orElseThrow(()->new RuntimeException("Athelet not found"));
		a.setCoachid(ch);
//		ch.setAth(a);
		
		athr.save(a);
	
}
//public Athelet getProfile(String email) {
//    return athr
//            .findByEmail(email)
//            .orElseThrow(() ->
//                new RuntimeException("User not found"));
//}
}
