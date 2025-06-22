package com.smarthire.placementportal.services;

import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;

@Service
public class ResumeParserService {

    private final Tika tika = new Tika();

    public String extractTextFromResume(File resumeFile) throws IOException, TikaException {
        return tika.parseToString(resumeFile);
    }
}
