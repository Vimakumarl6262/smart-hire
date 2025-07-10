package com.smarthire.placementportal.services;

import org.springframework.stereotype.Service;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import com.smarthire.placementportal.repositories.CandidateRepository;
import com.smarthire.placementportal.models.Candidate;

@Service
@RequiredArgsConstructor
public class EmailService {

    private final CandidateRepository candidateRepo;
    private final JavaMailSender mailSender;

    public void sendResultEmail(Long id) {
        Candidate c = candidateRepo.findById(id).orElseThrow();

        String status = c.getStatus();
        Double testScoreWrapper = c.getTestScore();
        int testScore = testScoreWrapper != null ? testScoreWrapper.intValue() : 0;

        String subject = "SmartHire – Test Result";
        String message;

        if ("Selected".equalsIgnoreCase(status)) {
            message = "Congratulations! You are selected for the role: " + c.getJobRole()
                    + "\n\nYour Test Score: " + testScore + "/5";
        } else if ("Rejected".equalsIgnoreCase(status)) {
            message = "Thank you for applying. Unfortunately, you have not been selected for the role: " + c.getJobRole()
                    + "\n\nYour Test Score: " + testScore + "/5";
        } else {
            message = "Your application for the role: " + c.getJobRole()
                    + " is still pending review.\n\nYour Test Score: " + testScore + "/5";
        }

        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(c.getEmail());
        mail.setSubject(subject);
        mail.setText(message);
        mailSender.send(mail);
    }
}
