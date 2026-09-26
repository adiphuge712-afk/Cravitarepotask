package com.example.demo.repository;

import com.example.demo.entity.Feedback;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
@Repository
public interface Feedbackrepo extends JpaRepository<Feedback, Long> {
List<Feedback> findByAthid_Coachid_Coachid(Long id);
}
