package com.example.demo.repository;

import com.example.demo.entity.Requestforacoach;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RequestRepo extends JpaRepository<Requestforacoach,Long> {

	/**
	 * The athlete's most recent request. Deliberately "first ordered by id
	 * descending" rather than a bare {@code findByAthid_Athid}: nothing at the
	 * database level stops an athlete from having more than one row (there is
	 * no unique constraint), and a plain {@code Optional}-returning finder
	 * throws {@code NonUniqueResultException} the moment a second row exists.
	 * This stays safe regardless of how many rows accumulate.
	 */
	Optional<Requestforacoach> findFirstByAthid_AthidOrderByRqidDesc(Long id);

List<Requestforacoach> findByAthid_Coachid_Adid_Adminid(Long id);
}
