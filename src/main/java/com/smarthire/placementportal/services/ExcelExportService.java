package com.smarthire.placementportal.services;

import org.springframework.stereotype.Service;
import com.smarthire.placementportal.repositories.CandidateRepository;
import com.smarthire.placementportal.models.Candidate;

import java.util.List;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

@Service
public class ExcelExportService {

    private final CandidateRepository candidateRepo;

    public ExcelExportService(CandidateRepository candidateRepo) {
        this.candidateRepo = candidateRepo;
    }

    public ByteArrayInputStream exportCandidates() {
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet("Candidates");

            // ✅ Header row
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Name");
            header.createCell(1).setCellValue("Email");
            header.createCell(2).setCellValue("Contact Number"); // ✅ New
            header.createCell(3).setCellValue("Job Role");
            header.createCell(4).setCellValue("Resume Score");
            header.createCell(5).setCellValue("Test Score");
            header.createCell(6).setCellValue("Final Score");
            header.createCell(7).setCellValue("Status");

            List<Candidate> list = candidateRepo.findAll();
            int rowIdx = 1;
            for (Candidate c : list) {
                Row row = sheet.createRow(rowIdx++);
                row.createCell(0).setCellValue(c.getFullName());
                row.createCell(1).setCellValue(c.getEmail());
                row.createCell(2).setCellValue(c.getContactNumber()); // ✅ New
                row.createCell(3).setCellValue(c.getJobRole());
                row.createCell(4).setCellValue(c.getResumeScore());
                row.createCell(5).setCellValue(c.getTestScore());
                row.createCell(6).setCellValue(c.getResumeScore() + c.getTestScore());
                row.createCell(7).setCellValue(c.getStatus());
            }

            ByteArrayOutputStream out = new ByteArrayOutputStream();
            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        } catch (IOException e) {
            throw new RuntimeException("Failed to export Excel", e);
        }
    }
}
