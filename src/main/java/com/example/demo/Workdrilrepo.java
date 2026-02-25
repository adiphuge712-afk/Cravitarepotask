package com.example.demo;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface Workdrilrepo  extends JpaRepository<Workdirl, Long>{
List<Workdirl> findByPlan_Coachid_Coachid(Long id);
List<Workdirl> findByPlan_Coachid_CoachidAndStartdate(Long id,LocalDate date);

}
