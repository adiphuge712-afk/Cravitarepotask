package com.example.demo;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface Performancerepo extends JpaRepository<Performancelog, Long> {
List<Performancelog> findByAthid_Coachid_coachid(Long id);
List<Performancelog> findByAthid_Athid(Long id);
}
