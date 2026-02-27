package com.example.demo;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface RequestRepo extends JpaRepository<Requestforacoach,Long> {
Optional<Requestforacoach> findByAthid_Athid(Long id);
List<Requestforacoach> findByAthid_Coachid_Adid_Adminid(Long id);
}
