package com.smarthire.placementportal.controllers;

import com.smarthire.placementportal.models.Candidate;
import com.smarthire.placementportal.services.CandidateService;
import com.smarthire.placementportal.services.ResumeParserService;
import com.smarthire.placementportal.services.ResumeScoringService;
import org.apache.tika.exception.TikaException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@Controller
@RequestMapping("/candidates")
public class CandidateController {

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/resumes/";

    @Autowired private CandidateService candidateService;
    @Autowired private ResumeParserService resumeParserService;
    @Autowired private ResumeScoringService resumeScoringService;

    /* ---------------- GET: registration form ---------------- */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";                 // templates/register.html
    }

    /* --------------- POST: handle registration -------------- */
    @PostMapping(value = "/register", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String registerCandidate(@RequestParam String fullName,
                                    @RequestParam String email,
                                    @RequestParam String contactNumber,
                                    @RequestParam String jobRole,
                                    @RequestParam("resume") MultipartFile resume,
                                    Model model) throws IOException {

        /* 1.  ensure uploads dir */
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        /* 2.  save resume */
        String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();
        File savedFile = new File(UPLOAD_DIR + fileName);
        resume.transferTo(savedFile);

        /* 3.  extract text & score */
        String resumeText;
        double resumeScore = 0;
        try {
            resumeText = resumeParserService.extractTextFromResume(savedFile);
            resumeScore = resumeScoringService.calculateScore(resumeText);
        } catch (TikaException e) {
            model.addAttribute("error", "Error reading resume: " + e.getMessage());
            return "candidate-result";
        }

        /* 4.  store candidate */
        Candidate cand = Candidate.builder()
                .fullName(fullName)
                .email(email)
                .contactNumber(contactNumber)
                .jobRole(jobRole)
                .resumeFileName(fileName)
                .resumeScore(resumeScore)
                .build();
        candidateService.saveCandidate(cand);

        /* 5.  pass data to view */
        model.addAttribute("name", fullName);
        model.addAttribute("score", String.format("%.2f", resumeScore));

        return "candidate-result";         // templates/candidate-result.html
    }
}
