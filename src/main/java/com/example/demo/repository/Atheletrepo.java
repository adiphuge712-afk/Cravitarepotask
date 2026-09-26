package com.example.demo.repository;

import com.example.demo.entity.Athelet;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Atheletrepo extends JpaRepository<Athelet, Long>{
	Optional<Athelet> findByEmail(String email);
	List<Athelet> findByCoachid_Coachid(Long obj);
}
