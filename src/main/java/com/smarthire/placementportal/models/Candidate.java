package com.smarthire.placementportal.models;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Candidate {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String fullName;

    private String email;

    private String contactNumber;

    private String jobRole;

    private String resumeFileName; // store filename

    private double resumeScore;

    private double testScore;

    private double originalityScore;

    private double totalScore;
}
