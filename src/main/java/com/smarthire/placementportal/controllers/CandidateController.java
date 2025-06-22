package com.smarthire.placementportal.controllers;

import com.smarthire.placementportal.models.Candidate;
import com.smarthire.placementportal.services.CandidateService;
import com.smarthire.placementportal.services.ResumeParserService;
import com.smarthire.placementportal.services.ResumeScoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.apache.tika.exception.TikaException;

import java.io.File;
import java.io.IOException;

@Controller                       // <-- HTML view भी रेंडर करेगा
@RequestMapping("/candidates")
public class CandidateController {

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/resumes/";

    @Autowired
    private CandidateService candidateService;

    @Autowired
    private ResumeParserService resumeParserService;

    @Autowired
    private ResumeScoringService resumeScoringService;

    /** ----- GET form page  ----- */
    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";        // templates/register.html
    }

    /** ----- POST form submit ----- */
    @PostMapping(value = "/register",
                 consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseBody
    public String registerCandidate(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String contactNumber,
            @RequestParam String jobRole,
            @RequestParam("resume") MultipartFile resume) throws IOException {

        /* 1. upload folder ensure */
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        /* 2. save file */
        String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();
        File savedFile = new File(UPLOAD_DIR + fileName);
        resume.transferTo(savedFile);

        /* 3. extract text + score */
        String resumeText;
        try {
            resumeText = resumeParserService.extractTextFromResume(savedFile);
        } catch (TikaException e) {
            return "❌ Error processing resume: " + e.getMessage();
        }
        double resumeScore = resumeScoringService.calculateScore(resumeText);

        /* 4. save in DB */
        Candidate c = Candidate.builder()
                .fullName(fullName)
                .email(email)
                .contactNumber(contactNumber)
                .jobRole(jobRole)
                .resumeFileName(fileName)
                .resumeScore(resumeScore)
                .build();
        candidateService.saveCandidate(c);

        return "✅ Candidate saved! Resume Score: " + String.format("%.2f", resumeScore);
    }
}
