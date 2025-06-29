package com.smarthire.placementportal.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;
    private String email;
    private String contactNumber;
    private String jobRole;

    @Column(length = 2000)
    private String answer1;

    @Column(length = 2000)
    private String answer2;

    @Column(length = 2000)
    private String answer3;

    @Column(length = 2000)
    private String answer4;

    @Column(length = 2000)
    private String answer5;

    private String resumeFileName;
    private double resumeScore;
    private double testScore;

    private String status;

    @Column(nullable = false)
    private double originalityScore = 0.0;

    // 🆕 Added missing field
    @Column(nullable = false)
    private double totalScore = 0.0;
}
