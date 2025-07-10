package com.smarthire.placementportal.services;

import com.smarthire.placementportal.models.Candidate;
import com.smarthire.placementportal.repositories.CandidateRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AdminService {

    private final CandidateRepository candidateRepo;

    public List<Candidate> getAllCandidates() {
        return candidateRepo.findAll();
    }

    public void updateStatus(Long id, String status) {
        Candidate c = candidateRepo.findById(id).orElseThrow();
        c.setStatus(status);
        candidateRepo.save(c);
    }

    // ✅ Add this method
    public void deleteCandidate(Long id) {
        candidateRepo.deleteById(id);
    }
}
