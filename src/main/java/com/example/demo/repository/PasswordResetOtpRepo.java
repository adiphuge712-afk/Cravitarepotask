package com.example.demo.repository;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.PasswordResetOtp;

import jakarta.transaction.Transactional;

@Repository
public interface PasswordResetOtpRepo extends JpaRepository<PasswordResetOtp, Long> {

	/** The most recent attempt for an email, whatever its state. */
	Optional<PasswordResetOtp> findTopByEmailOrderByCreatedAtDesc(String email);

	Optional<PasswordResetOtp> findByEmailAndResetToken(String email, String resetToken);

	/** Invalidate any outstanding codes before issuing a new one. */
	@Modifying
	@Transactional
	@Query("update PasswordResetOtp o set o.used = true where o.email = ?1 and o.used = false")
	void invalidateOutstanding(String email);

	@Modifying
	@Transactional
	@Query("delete from PasswordResetOtp o where o.expiresAt < ?1")
	void deleteExpiredBefore(Instant cutoff);
}
