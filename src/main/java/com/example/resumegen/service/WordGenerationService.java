package com.example.resumegen.service;

import com.example.resumegen.model.Resume;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Service
public class WordGenerationService {

    public byte[] generateWord(Resume resume) throws IOException {
        try (XWPFDocument document = new XWPFDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {

            // Name
            XWPFParagraph title = document.createParagraph();
            XWPFRun titleRun = title.createRun();
            titleRun.setText(resume.getFullName() != null ? resume.getFullName() : "");
            titleRun.setBold(true);
            titleRun.setFontSize(18);

            // Contact
            XWPFParagraph contact = document.createParagraph();
            XWPFRun contactRun = contact.createRun();
            contactRun.setText((resume.getEmail() != null ? resume.getEmail() : "") + " | " + (resume.getPhone() != null ? resume.getPhone() : ""));
            contactRun.setFontSize(12);

            // Summary
            addSectionTitle(document, "Summary");
            XWPFParagraph summary = document.createParagraph();
            summary.createRun().setText(resume.getSummary() != null ? resume.getSummary() : "");

            // Skills
            addSectionTitle(document, "Skills");
            if (resume.getSkills() != null) {
                for (String skill : resume.getSkills()) {
                    XWPFParagraph p = document.createParagraph();
                    p.createRun().setText("• " + skill);
                }
            }

            // Experience
            addSectionTitle(document, "Experience");
            if (resume.getExperiences() != null) {
                for (Resume.Experience exp : resume.getExperiences()) {
                    XWPFParagraph p1 = document.createParagraph();
                    XWPFRun r1 = p1.createRun();
                    r1.setText((exp.getPosition() != null ? exp.getPosition() : "") + " at " + (exp.getCompany() != null ? exp.getCompany() : ""));
                    r1.setBold(true);
                    
                    XWPFParagraph p2 = document.createParagraph();
                    XWPFRun r2 = p2.createRun();
                    r2.setText(exp.getDuration() != null ? exp.getDuration() : "");
                    r2.setItalic(true);

                    XWPFParagraph p3 = document.createParagraph();
                    p3.createRun().setText(exp.getDescription() != null ? exp.getDescription() : "");
                }
            }

            // Education
            addSectionTitle(document, "Education");
            if (resume.getEducations() != null) {
                for (Resume.Education edu : resume.getEducations()) {
                    XWPFParagraph p = document.createParagraph();
                    p.createRun().setText((edu.getDegree() != null ? edu.getDegree() : "") + ", " + (edu.getInstitution() != null ? edu.getInstitution() : "") + " (" + (edu.getYear() != null ? edu.getYear() : "") + ")");
                }
            }

            document.write(out);
            return out.toByteArray();
        }
    }

    private void addSectionTitle(XWPFDocument document, String title) {
        XWPFParagraph p = document.createParagraph();
        XWPFRun r = p.createRun();
        r.setText(title);
        r.setBold(true);
        r.setFontSize(14);
        r.addBreak();
    }
}
