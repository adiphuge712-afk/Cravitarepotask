package com.example.demo.repository;

import com.example.demo.entity.Coach;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Coachrepo  extends JpaRepository<Coach, Long> {
	Optional<Coach> findByEmail(String email);
}
