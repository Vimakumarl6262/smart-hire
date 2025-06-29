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
import java.util.Map;

@Controller
@RequestMapping("/candidates")
public class CandidateController {

    private static final String UPLOAD_DIR =
            System.getProperty("user.dir") + "/uploads/resumes/";

    @Autowired private CandidateService candidateService;
    @Autowired private ResumeParserService resumeParserService;
    @Autowired private ResumeScoringService resumeScoringService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping("/startTest")
    public String startTest(@RequestParam Map<String, String> data, Model model) {
        model.addAllAttributes(data);
        return "test";
    }

    @PostMapping("/testSubmit")
    public String submitTest(@RequestParam Map<String, String> data, Model model) {
        model.addAllAttributes(data);
        return "submit";
    }

    @PostMapping(value = "/finalSubmit", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String finalSubmit(@RequestParam("resume") MultipartFile resume,
                              @RequestParam Map<String, String> data,
                              Model model) throws IOException {

        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();
        File savedFile = new File(UPLOAD_DIR + fileName);
        resume.transferTo(savedFile);

        String resumeText;
        double resumeScore = 0;
        try {
            resumeText = resumeParserService.extractTextFromResume(savedFile);
            resumeScore = resumeScoringService.calculateResumeScore(resumeText);
        } catch (TikaException e) {
            model.addAttribute("error", "Error reading resume: " + e.getMessage());
            return "candidate-result";
        }

        String[] answers = new String[] {
                data.get("answer1"),
                data.get("answer2"),
                data.get("answer3"),
                data.get("answer4"),
                data.get("answer5")
        };

        double testScore = resumeScoringService.calculateTestScore(answers);
        double originalityScore = 0.0; // Future use
        double totalScore = resumeScore + testScore + originalityScore;
        String status = resumeScoringService.determineFinalStatus(resumeScore, testScore);

        Candidate candidate = Candidate.builder()
                .fullName(data.get("fullName"))
                .email(data.get("email"))
                .contactNumber(data.get("contactNumber"))
                .jobRole(data.get("jobRole"))
                .answer1(data.get("answer1"))
                .answer2(data.get("answer2"))
                .answer3(data.get("answer3"))
                .answer4(data.get("answer4"))
                .answer5(data.get("answer5"))
                .resumeFileName(fileName)
                .resumeScore(resumeScore)
                .testScore(testScore)
                .originalityScore(originalityScore)
                .totalScore(totalScore)
                .status(status)
                .build();

        candidateService.saveCandidate(candidate);

        model.addAttribute("name", data.get("fullName"));
        model.addAttribute("score", String.format("%.2f", resumeScore));
        model.addAttribute("testScore", String.format("%.2f", testScore));
        model.addAttribute("status", status);

        return "candidate-result";
    }
}
