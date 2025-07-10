package com.smarthire.placementportal.services;

import org.springframework.stereotype.Service;

@Service
public class PlagiarismCheckService {

    public double checkSimilarity(String a, String b) {
        if (a == null || b == null) return 0.0;
        a = a.toLowerCase().replaceAll("[^a-z0-9 ]", "");
        b = b.toLowerCase().replaceAll("[^a-z0-9 ]", "");

        String[] wordsA = a.split("\\s+");
        String[] wordsB = b.split("\\s+");

        int matches = 0;
        for (String wordA : wordsA) {
            for (String wordB : wordsB) {
                if (wordA.equals(wordB)) {
                    matches++;
                    break;
                }
            }
        }

        return (2.0 * matches) / (wordsA.length + wordsB.length); // 0 to 1 range
    }

    public double calculatePlagiarismScore(String[] answers) {
        double maxSim = 0;
        for (int i = 0; i < answers.length; i++) {
            for (int j = i + 1; j < answers.length; j++) {
                double sim = checkSimilarity(answers[i], answers[j]);
                maxSim = Math.max(maxSim, sim);
            }
        }
        return maxSim * 100; // % similarity
    }
}
