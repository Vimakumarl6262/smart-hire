package com.smarthire.placementportal.controllers;

import com.smarthire.placementportal.models.Candidate;
import com.smarthire.placementportal.services.AdminService;
import com.smarthire.placementportal.services.EmailService;
import com.smarthire.placementportal.services.ExcelExportService;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final EmailService emailService;
    private final ExcelExportService excelService;

    // ✅ Admin dashboard
    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        List<Candidate> candidates = adminService.getAllCandidates();
        model.addAttribute("candidates", candidates);
        return "admin_dashboard";
    }

    // ✅ Update candidate status
    @PostMapping("/status/{id}")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        adminService.updateStatus(id, status);
        return "redirect:/admin/dashboard";
    }

    // ✅ Send result email
    @PostMapping("/sendEmail/{id}")
    public String sendEmail(@PathVariable Long id) {
        emailService.sendResultEmail(id);
        return "redirect:/admin/dashboard";
    }

    // ✅ Delete candidate
    @PostMapping("/delete/{id}")
    public String deleteCandidate(@PathVariable Long id) {
        adminService.deleteCandidate(id);
        return "redirect:/admin/dashboard";
    }

    // ✅ Export candidate data to Excel
    @GetMapping("/export")
    public ResponseEntity<InputStreamResource> exportToExcel() {
        ByteArrayInputStream in = excelService.exportCandidates();
        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=candidates.xlsx");

        return ResponseEntity.ok()
                .headers(headers)
                .contentType(MediaType.parseMediaType("application/vnd.ms-excel"))
                .body(new InputStreamResource(in));
    }

    // ✅ Serve resume file for view/download
    @GetMapping("/resume/{fileName:.+}")
    public ResponseEntity<Resource> viewResume(@PathVariable String fileName) {
        try {
            Path filePath = Paths.get(System.getProperty("user.dir") + "/uploads/resumes/").resolve(fileName).normalize();
            if (!Files.exists(filePath)) {
                return ResponseEntity.notFound().build();
            }

            Resource resource = new InputStreamResource(Files.newInputStream(filePath));
            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + fileName + "\"")
                    .body(resource);

        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
