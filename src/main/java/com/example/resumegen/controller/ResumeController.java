package com.example.resumegen.controller;

import com.example.resumegen.model.Resume;
import com.example.resumegen.service.AiService;
import com.example.resumegen.service.PdfService;
import com.example.resumegen.service.WordService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ResumeController {

    private final AiService aiService;
    private final PdfService pdfService;
    private final WordService wordService;

    // Temporary storage for demo (in real app, use database or session)
    private Resume currentResume;

    public ResumeController(AiService aiService, PdfService pdfService, WordService wordService) {
        this.aiService = aiService;
        this.pdfService = pdfService;
        this.wordService = wordService;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/generate")
    public String generate(@RequestParam("prompt") String prompt, Model model) {
        currentResume = aiService.parsePrompt(prompt);
        model.addAttribute("resume", currentResume);
        return "resume-view";
    }

    @GetMapping("/download/pdf")
    public ResponseEntity<byte[]> downloadPdf() {
        if (currentResume == null)
            return ResponseEntity.badRequest().build();
        byte[] data = pdfService.generatePdf(currentResume);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(data);
    }

    @GetMapping("/download/word")
    public ResponseEntity<byte[]> downloadWord() {
        if (currentResume == null)
            return ResponseEntity.badRequest().build();
        byte[] data = wordService.generateWord(currentResume);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume.docx\"")
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(data);
    }
}
