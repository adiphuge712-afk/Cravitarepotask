package com.example.demo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Atheletrepo extends JpaRepository<Athelet, Long>{
	Optional<Athelet> findByEmail(String email);
}
