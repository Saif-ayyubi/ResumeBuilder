package com.example.resumegen.service;

import com.example.resumegen.model.Resume;
import com.lowagie.text.Document;
import com.lowagie.text.Font;
import com.lowagie.text.Paragraph;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class PdfGenerationService {

    public byte[] generatePdf(Resume resume) throws IOException {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD);
            Font headerFont = new Font(Font.HELVETICA, 14, Font.BOLD);
            Font normalFont = new Font(Font.HELVETICA, 12, Font.NORMAL);

            document.add(new Paragraph(resume.getFullName(), titleFont));
            document.add(new Paragraph(resume.getEmail() + " | " + resume.getPhone(), normalFont));
            document.add(new Paragraph(" ")); // Spacer

            document.add(new Paragraph("Summary", headerFont));
            document.add(new Paragraph(resume.getSummary(), normalFont));
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Skills", headerFont));
            if (resume.getSkills() != null) {
                for (String skill : resume.getSkills()) {
                    document.add(new Paragraph("• " + skill, normalFont));
                }
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Experience", headerFont));
            if (resume.getExperiences() != null) {
                for (Resume.Experience exp : resume.getExperiences()) {
                    document.add(new Paragraph(exp.getPosition() + " at " + exp.getCompany(), new Font(Font.HELVETICA, 12, Font.BOLD)));
                    document.add(new Paragraph(exp.getDuration(), new Font(Font.HELVETICA, 10, Font.ITALIC)));
                    document.add(new Paragraph(exp.getDescription(), normalFont));
                    document.add(new Paragraph(" "));
                }
            }

            document.add(new Paragraph("Education", headerFont));
            if (resume.getEducations() != null) {
                for (Resume.Education edu : resume.getEducations()) {
                    document.add(new Paragraph(edu.getDegree() + ", " + edu.getInstitution(), normalFont));
                    document.add(new Paragraph(edu.getYear(), new Font(Font.HELVETICA, 10, Font.ITALIC)));
                }
            }

            document.close();
            return out.toByteArray();
        }
    }
}
