package com.example.demo;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class Servicefile {
	@Autowired
RequestRepo rdr;	
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
public List<Traningplan> getAllTraningplan(long id){
	List<Traningplan> list=tdr.findByCoachid_Coachid(id);
	return list;
}

public List<Workdirl> getAllWorkdril(){
	return wdr.findAll();
}
public List<Workdirl> getAllWorkdrilByCoachid(long id){
	return wdr.findByPlan_Coachid_Coachid(id);
}
public List<Feedback> getAllFeedback(){
	return fdr.findAll();
}
public List<Feedback> getAllFeedbackbycoachid(long id){
	
	return fdr.findByAthid_Coachid_Coachid(id);
}

public List<Performancelog> getAllPerformlog(){
	return pdr.findAll();
}
public List<Performancelog> getPerformancelogByAtheletId(long id){
	
	return pdr.findByAthid_Athid(id);
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
public List<Athelet> getAllatheletbycouchid(long id){
	
	List<Athelet> a=athr.findByCoachid_Coachid(id);
	System.out.println(a.size());
	return  a;
}
public void deleteplanbyid(long id) {
	if (tdr.existsById(id)) {
		tdr.deleteById(id);
	}else {
		System.out.println("plan id not found");
	}
	
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
public Traningplan updateplanbyid(long id,Traningplan c){
	Traningplan ch=tdr.findById(id).orElseThrow(()->new RuntimeException("Id not found"));
	ch.setEnddate(c.getEnddate());
	ch.setPlanname(c.getPlanname());
	ch.setPlantype(c.getPlantype());
	ch.setStartdate(c.getStartdate());
	return tdr.save(ch);
	
}

public void Assigendid(long aid,long cid) {
	
		Coach ch=chr.findById(cid).orElseThrow(()->new RuntimeException("Coach not found"));
		Athelet a=athr.findById(aid).orElseThrow(()->new RuntimeException("Athelet not found"));
		a.setCoachid(ch);
//		ch.setAth(a);
		
		athr.save(a);
	
}
public void addComplain(Feedback f,long id) {
	Athelet a=athr.findById(id).orElseThrow(()->new RuntimeException("Athelet not found"));
	f.setAthid(a);
	 fdr.save(f);
}
public void addplan(Traningplan plan,long id) {
	Coach c=chr.findById(id).orElseThrow(()->new RuntimeException("Coach not found"));
	plan.setCoachid(c);
	tdr.save(plan);
}
public void addworkdril(Workdirl plan,long id) {
	LocalDate date=LocalDate.now();
	Traningplan c=tdr.findById(id).orElseThrow(()->new RuntimeException("Coach not found"));
	plan.setPlan(c);
	plan.setStartdate(date);
	wdr.save(plan);
}
public void addPerformancedata(Performancelog per,long athid,long workid) {
//	Athelet a=athr.findById(id).orElseThrow(()->new RuntimeException("Athelet not found"));
//	Workdirl w=wdr.findById(wid).orElseThrow(()->new RuntimeException("Workdril not found"));
//	LocalDate date= LocalDate.now();
//	per.setDate(date);
//	per.setAthid(a);
//	per.setWorkid(w);
//	pdr.save(per);
	 Performancelog existing =
	            pdr.findByAthid_AthidAndWorkid_Workid(athid, workid);

	    Athelet athlete = athr.findById(athid)
	            .orElseThrow(() -> new RuntimeException("Athlete not found"));

	    Workdirl work = wdr.findById(workid)
	            .orElseThrow(() -> new RuntimeException("Workdril not found"));

	    if (existing != null) {

	        // 🔄 UPDATE existing row
	        existing.setCompletestatus(per.getCompletestatus());
	        existing.setPerformancematrix(per.getPerformancematrix());
	        existing.setFatiquelevel(per.getFatiquelevel());
	        existing.setDate(LocalDate.now());
	        pdr.save(existing);

	    } else {

	        // ➕ CREATE new row
	        per.setAthid(athlete);
	        per.setWorkid(work);
	        per.setDate(LocalDate.now());
	        pdr.save(per);
	    }
	
	
}

public void updatePerformancedata(String data,long workid,long athid) {

    Performancelog perform =pdr.findByAthid_AthidAndWorkid_Workid(athid, workid);
    LocalDate date= LocalDate.now();
    if (perform == null) {

        Athelet athlete = athr.findById(athid)
                .orElseThrow(() -> new RuntimeException("Athlete not found"));

        Workdirl work = wdr.findById(workid)
                .orElseThrow(() -> new RuntimeException("Work not found"));

        perform = new Performancelog();
        perform.setAthid(athlete);
        perform.setWorkid(work);
    }
perform.setDate(date);
    perform.setCompletestatus(data);
    pdr.save(perform);
    
}


public List<Performancelog> getPerformancelogById(long id) {
	return pdr.findByAthid_Coachid_coachid(id);
	
}
public void addrequest(Requestforacoach req,long id) {
	Athelet a=athr.findById(id).orElseThrow(()->new RuntimeException("Athelet not found"));
	
	req.setAthid(a);
rdr.save(req);
}
public List<Requestforacoach> viewrequest(){
	return rdr.findAll();
}


public List<Workdirl> getallworkdril_by_todays_date(LocalDate date,long id){
	return wdr.findByPlan_Coachid_CoachidAndStartdate(id, date);
	
}
public List<Workdirl> getallworkdrilBydate(LocalDate date,long id){
	return wdr.findByPlan_Coachid_CoachidAndStartdate(id,date);
	
}


}
