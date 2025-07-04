package com.smarthire.placementportal.services;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Map;

@Service
public class LocalLLMService {

    @Value("${ollama.url}")
    private String ollamaUrl;

    private final RestTemplate restTemplate;

    public LocalLLMService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String generateWithOllama(String prompt) {
        Map<String, Object> body = Map.of(
            "model", "mistral",
            "prompt", prompt,
            "stream", false
        );

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);

        ResponseEntity<Map> response = restTemplate.exchange(ollamaUrl, HttpMethod.POST, entity, Map.class);

        if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
            return (String) response.getBody().get("response");
        }
        return "Error calling local AI";
    }

    public String scoreResume(String resumeText, String jobRole) {
        String prompt = """
    You are an experienced HR recruiter. Evaluate the following resume for the job role: """ + jobRole + """

    Instructions:
    1. Give a score out of 100 based on relevance to the job role, technical skills, clarity, and presentation.
    2. If the candidate has limited experience, consider their education, academic projects, certifications, and communication skills.
    3. Start the response strictly in this format:
       Score: <number>
       Feedback: <2-3 line helpful feedback for improvement>

    Resume:
    """ + resumeText;

        return generateWithOllama(prompt);
    }

    public String scoreTestAnswers(String[] answers, String jobRole) {
        StringBuilder sb = new StringBuilder();
        sb.append("You are an interviewer for the job role: ").append(jobRole).append(".\n");
        sb.append("""
    Evaluate the candidate's answers in an easy and supportive way out of 100.
    
    Instructions:
    - Be lenient and encouraging.
    - Consider they may be a fresher.
    - Give marks based on basic correctness and effort.
    - Each question carries 20 marks.
    
    Output format:
    Score: <number>
    Feedback: <short helpful and positive message>
    
    Answers:
    """);
        for (int i = 0; i < answers.length; i++) {
            sb.append("Q").append(i + 1).append(": ").append(answers[i]).append("\n");
        }
        return generateWithOllama(sb.toString());
    }
    

    public String[] generateTestQuestions(String resumeText, String jobRole) {
        String prompt = """
    You are an AI recruiter.
    
    Generate exactly 5 simple, easy, and one-line technical or communication questions.
    Base these questions only on the resume content and job role.
    
    Instructions:
    Strictly generate only 5 questions.
- Each question must be only one line (short and clear).
- Do not leave any blank lines between questions.
- No numbering. No sub-parts. No paragraphs.
- Output format: plain text, each question on a new line, no blank lines.

    Job Role: """ + jobRole + resumeText + """
    
    Resume:
    """ + resumeText;
    
        String response = generateWithOllama(prompt);
        return response.split("\\r?\\n");
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
            } catch (NumberFormatException ignored) {}
        }
        return 0;
    }
}
