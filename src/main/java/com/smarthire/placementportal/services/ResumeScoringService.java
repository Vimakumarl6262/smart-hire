package com.smarthire.placementportal.services;

import org.springframework.stereotype.Service;

@Service
public class ResumeScoringService {

    private static final String[] KEYWORDS = {"Java", "Spring", "Hibernate", "SQL", "Communication", "Teamwork", "Problem-solving"};

    public double calculateScore(String resumeText) {
        int totalKeywords = KEYWORDS.length;
        int foundKeywords = 0;

        resumeText = resumeText.toLowerCase();

        for (String keyword : KEYWORDS) {
            if (resumeText.contains(keyword.toLowerCase())) {
                foundKeywords++;
            }
        }

        return ((double) foundKeywords / totalKeywords) * 100; // score out of 100
    }
}
