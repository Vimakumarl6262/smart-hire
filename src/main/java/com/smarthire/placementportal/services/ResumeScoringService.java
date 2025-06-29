package com.smarthire.placementportal.services;

import org.springframework.stereotype.Service;

@Service
public class ResumeScoringService {

    private static final String[] KEYWORDS = {
        "java", "spring", "hibernate", "sql", "communication", "teamwork", "problem-solving"
    };

    // 1. Resume scoring by counting presence of keywords
    public double calculateResumeScore(String resumeText) {
        int totalKeywords = KEYWORDS.length;
        int foundKeywords = 0;
        if (resumeText == null) return 0;

        resumeText = resumeText.toLowerCase();

        for (String keyword : KEYWORDS) {
            if (resumeText.contains(keyword)) {
                foundKeywords++;
            }
        }
        return ((double) foundKeywords / totalKeywords) * 100;  // score out of 100
    }

    // 2. Simple test scoring - for example, count number of answered questions
    public double calculateTestScore(String[] answers) {
        if (answers == null) return 0;

        int maxScore = answers.length * 20;  // assume 20 points max per question
        int score = 0;

        for (String ans : answers) {
            if (ans != null && !ans.trim().isEmpty()) {
                score += 20;  // full marks per answered question
            }
        }
        return (double) score;  // score out of maxScore (like 100)
    }

    // 3. Determine final candidate status based on weighted score
    public String determineFinalStatus(double resumeScore, double testScore) {
        double overallScore = (resumeScore * 0.6) + (testScore * 0.4);  // weighted average

        if (overallScore >= 70) {
            return "Selected";
        } else if (overallScore >= 50) {
            return "On Hold";
        } else {
            return "Rejected";
        }
    }
}
