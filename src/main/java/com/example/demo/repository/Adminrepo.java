package com.example.demo.repository;

import com.example.demo.entity.Admin;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface Adminrepo extends JpaRepository<Admin, Long> {
	Optional<Admin> findByEmail(String email);
}
