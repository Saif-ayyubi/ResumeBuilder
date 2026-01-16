package com.example.resumegen.service;

import com.example.resumegen.model.Resume;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;

@Service
public class PdfService {

    public byte[] generatePdf(Resume resume) {
        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Document document = new Document();
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 24);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font textFont = FontFactory.getFont(FontFactory.HELVETICA, 12);

            // Header
            document.add(new Paragraph(resume.getFullName(), titleFont));
            document.add(new Paragraph(resume.getEmail() + " | " + resume.getPhone(), textFont));
            document.add(new Paragraph(" ")); // Spacer

            // Summary
            document.add(new Paragraph("Summary", sectionFont));
            document.add(new Paragraph(resume.getSummary(), textFont));
            document.add(new Paragraph(" "));

            // Skills
            document.add(new Paragraph("Skills", sectionFont));
            document.add(new Paragraph(String.join(", ", resume.getSkills()), textFont));
            document.add(new Paragraph(" "));

            // Experience
            document.add(new Paragraph("Experience", sectionFont));
            for (Resume.Experience exp : resume.getExperiences()) {
                document.add(new Paragraph(exp.getTitle() + " at " + exp.getCompany(),
                        FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                document.add(new Paragraph(exp.getDuration(), FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 12)));
                document.add(new Paragraph(exp.getDescription(), textFont));
                document.add(new Paragraph(" "));
            }

            // Education
            document.add(new Paragraph("Education", sectionFont));
            for (Resume.Education edu : resume.getEducations()) {
                document.add(new Paragraph(edu.getDegree(), FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14)));
                document.add(new Paragraph(edu.getSchool() + " (" + edu.getYear() + ")", textFont));
            }

            document.close();
            return out.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
