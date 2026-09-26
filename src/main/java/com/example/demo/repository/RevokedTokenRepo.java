package com.example.demo.repository;

import java.time.Instant;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.RevokedToken;

import jakarta.transaction.Transactional;

@Repository
public interface RevokedTokenRepo extends JpaRepository<RevokedToken, Long> {

	boolean existsByTokenHash(String tokenHash);

	@Modifying
	@Transactional
	@Query("delete from RevokedToken r where r.expiresAt < ?1")
	void deleteExpiredBefore(Instant cutoff);
}
