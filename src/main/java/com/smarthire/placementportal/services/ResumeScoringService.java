package com.smarthire.placementportal.services;

import org.springframework.stereotype.Service;

@Service
public class ResumeScoringService {

    public String determineFinalStatus(double resumeScore, double testScore) {
        double overallScore = (resumeScore * 0.6) + (testScore * 0.4);

        if (overallScore >= 70) {
            return "Selected";
        } else if (overallScore >= 50) {
            return "On Hold";
        } else {
            return "Rejected";
        }
    }

    public double extractScoreFromText(String aiResponse) {
        if (aiResponse == null || aiResponse.isEmpty()) return 0;

        String[] parts = aiResponse.split("[^0-9]+");
        for (String part : parts) {
            try {
                int val = Integer.parseInt(part);
                if (val >= 0 && val <= 100) {
                    return (double) val;
                }
            } catch (NumberFormatException ignored) {
            }
        }
        return 0;
    }
}
