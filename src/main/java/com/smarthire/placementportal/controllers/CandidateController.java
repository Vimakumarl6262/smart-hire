package com.smarthire.placementportal.controllers;

import com.smarthire.placementportal.models.Candidate;
import com.smarthire.placementportal.services.CandidateService;
import com.smarthire.placementportal.services.LocalLLMService;
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

    private static final String UPLOAD_DIR = System.getProperty("user.dir") + "/uploads/resumes/";

    @Autowired private CandidateService candidateService;
    @Autowired private ResumeParserService resumeParserService;
    @Autowired private ResumeScoringService resumeScoringService;
    @Autowired private LocalLLMService localLLMService;

    @GetMapping("/register")
    public String showRegisterForm() {
        return "register";
    }

    @PostMapping(value = "/startTest", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String startTest(@RequestParam("resume") MultipartFile resume,
                            @RequestParam Map<String, String> data,
                            Model model) throws IOException {

        model.addAllAttributes(data);

        // ✅ Save resume file
        File dir = new File(UPLOAD_DIR);
        if (!dir.exists()) dir.mkdirs();

        String fileName = System.currentTimeMillis() + "_" + resume.getOriginalFilename();
        File savedFile = new File(UPLOAD_DIR + fileName);
        resume.transferTo(savedFile);

        model.addAttribute("resumeFileName", fileName); // to reuse in finalSubmit

        // ✅ Extract resume text
        String resumeText;
        try {
            resumeText = resumeParserService.extractTextFromResume(savedFile);
        } catch (TikaException e) {
            model.addAttribute("error", "Could not read resume: " + e.getMessage());
            return "test";
        }

        // ✅ Generate questions based on job role and resume
        String jobRole = data.get("jobRole");
        String[] questions = localLLMService.generateTestQuestions(resumeText, jobRole);

        for (int i = 0; i < 5; i++) {
            model.addAttribute("question" + (i + 1), questions.length > i ? questions[i] : "Question " + (i + 1));
        }

        return "test";
    }

    @PostMapping("/finalSubmit")
    public String finalSubmit(@RequestParam Map<String, String> data,
                              Model model) throws IOException {

        String resumeFileName = data.get("resumeFileName");
        File resumeFile = new File(UPLOAD_DIR + resumeFileName);

        String resumeText;
        double resumeScore = 0;
        String aiResumeFeedback = "";
        String aiTestFeedback = "";

        try {
            resumeText = resumeParserService.extractTextFromResume(resumeFile);
            aiResumeFeedback = localLLMService.scoreResume(resumeText, data.get("jobRole"));
            resumeScore = resumeScoringService.extractScoreFromText(aiResumeFeedback);
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

        aiTestFeedback = localLLMService.scoreTestAnswers(answers, data.get("jobRole"));
        double testScore = resumeScoringService.extractScoreFromText(aiTestFeedback);

        double originalityScore = 0.0;
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
                .resumeFileName(resumeFileName)
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
        model.addAttribute("totalScore", String.format("%.2f", totalScore));
        model.addAttribute("status", status);
        model.addAttribute("aiFeedback", aiResumeFeedback);
        model.addAttribute("testFeedback", aiTestFeedback);

        return "candidate-result";
    }
}
