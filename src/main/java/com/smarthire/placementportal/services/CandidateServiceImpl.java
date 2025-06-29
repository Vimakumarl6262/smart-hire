package com.smarthire.placementportal.services;

import com.smarthire.placementportal.models.Candidate;
import com.smarthire.placementportal.repositories.CandidateRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CandidateServiceImpl implements CandidateService {

    @Autowired
    private CandidateRepository candidateRepository;

    @Override
    public void saveCandidate(Candidate candidate) {
        candidateRepository.save(candidate);
    }
}
