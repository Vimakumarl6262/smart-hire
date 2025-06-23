package com.smarthire.placementportal.repositories;

import com.smarthire.placementportal.models.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    // optional: findByEmail, etc.
}