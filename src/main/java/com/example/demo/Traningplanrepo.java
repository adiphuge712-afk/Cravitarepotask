package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface Traningplanrepo  extends JpaRepository<Traningplan, Long> {
List<Traningplan> findByCoachid_Coachid(Long id);
}
