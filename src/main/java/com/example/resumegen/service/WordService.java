package com.example.resumegen.service;

import com.example.resumegen.model.Resume;
import org.apache.poi.xwpf.usermodel.*;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class WordService {

    public byte[] generateWord(Resume resume) {
        try (XWPFDocument document = new XWPFDocument();
                ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Header
            XWPFParagraph title = document.createParagraph();
            title.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun titleRun = title.createRun();
            titleRun.setText(resume.getFullName());
            titleRun.setBold(true);
            titleRun.setFontSize(20);

            XWPFParagraph contact = document.createParagraph();
            contact.setAlignment(ParagraphAlignment.CENTER);
            XWPFRun contactRun = contact.createRun();
            contactRun.setText(resume.getEmail() + " | " + resume.getPhone());
            contactRun.setFontSize(12);

            // Summary
            createSectionHeader(document, "Summary");
            createBodyText(document, resume.getSummary());

            // Skills
            createSectionHeader(document, "Skills");
            createBodyText(document, String.join(", ", resume.getSkills()));

            // Experience
            createSectionHeader(document, "Experience");
            for (Resume.Experience exp : resume.getExperiences()) {
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();
                r.setBold(true);
                r.setText(exp.getTitle() + " at " + exp.getCompany());
                r.addBreak();

                XWPFRun r2 = p.createRun();
                r2.setItalic(true);
                r2.setText(exp.getDuration());
                r2.addBreak();

                XWPFRun r3 = p.createRun();
                r3.setText(exp.getDescription());
            }

            // Education
            createSectionHeader(document, "Education");
            for (Resume.Education edu : resume.getEducations()) {
                XWPFParagraph p = document.createParagraph();
                XWPFRun r = p.createRun();
                r.setBold(true);
                r.setText(edu.getDegree());
                r.addBreak();

                XWPFRun r2 = p.createRun();
                r2.setText(edu.getSchool() + " (" + edu.getYear() + ")");
            }

            document.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void createSectionHeader(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setBold(true);
        r.setFontSize(16);
        r.setText(text);
        p.setBorderBottom(Borders.SINGLE);
    }

    private void createBodyText(XWPFDocument doc, String text) {
        XWPFParagraph p = doc.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(text);
    }
}
