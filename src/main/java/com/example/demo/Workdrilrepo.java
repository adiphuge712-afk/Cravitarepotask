package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface Workdrilrepo  extends JpaRepository<Workdirl, Long>{
List<Workdirl> findByPlan_Coachid_Coachid(Long id);
}
