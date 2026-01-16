package com.example.resumegen.service;

import com.example.resumegen.model.Resume;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
public class AiService {

        public Resume parsePrompt(String prompt) {
                Resume resume = new Resume();

                // 1. EXTRACT CONTACT INFO (Strict or nothing)
                String email = extractEmail(prompt);
                String phone = extractPhone(prompt);
                String name = extractName(prompt);

                resume.setFullName(name != null ? name : "Your Name Here");
                resume.setEmail(email != null ? email : "");
                resume.setPhone(phone != null ? phone : "");

                // 2. EXTRACT SECTIONS (Strict)
                String summary = extractSection(prompt, "Summary");
                String skillsStr = extractSection(prompt, "Skills");
                String expStr = extractSection(prompt, "Experience");
                String eduStr = extractSection(prompt, "Education");

                resume.setSummary(summary != null && !summary.isEmpty() ? summary : "");

                if (skillsStr != null && !skillsStr.isEmpty()) {
                        // Split by comma or newline
                        List<String> skills = Arrays.stream(skillsStr.split("[,\\n]"))
                                        .map(String::trim)
                                        .filter(s -> !s.isEmpty())
                                        .collect(Collectors.toList());
                        resume.setSkills(skills);
                } else {
                        resume.setSkills(new ArrayList<>());
                }

                List<Resume.Experience> expList = new ArrayList<>();
                if (expStr != null && !expStr.isEmpty()) {
                        // Treat the whole block as one experience entry for simplicity, or try to
                        // split?
                        // User requested "only what use give". Let's put the raw text in description.
                        Resume.Experience exp = new Resume.Experience();
                        exp.setCompany("");
                        exp.setTitle("Work Experience");
                        exp.setDuration("");
                        exp.setDescription(expStr);
                        expList.add(exp);
                }
                resume.setExperiences(expList);

                List<Resume.Education> eduList = new ArrayList<>();
                if (eduStr != null && !eduStr.isEmpty()) {
                        Resume.Education edu = new Resume.Education();
                        // Map user text to Degree so it shows up Bold
                        edu.setDegree(eduStr);
                        // Template uses 'institution', so set it to empty or maybe a substring if we
                        // were smart
                        // For now, put it all in Degree to be safe and visible.
                        edu.setInstitution("");
                        edu.setSchool("");
                        edu.setYear("");
                        eduList.add(edu);
                }
                resume.setEducations(eduList);

                return resume;
        }

        private String extractName(String prompt) {
                // Look for "Name: [Name]"
                // Match until end of line or specific punctuation
                Pattern p1 = Pattern.compile("Name[:\\-]?\\s+([a-zA-Z\\s]+?)(?:\\n|\\r|$|\\s(Email|Mobile|Phone))",
                                Pattern.CASE_INSENSITIVE);
                Matcher m1 = p1.matcher(prompt);
                if (m1.find()) {
                        return formatName(m1.group(1).trim());
                }

                // Look for "I am [Name]" - require Capitalized words to avoid "I am a
                // developer"
                Pattern p2 = Pattern.compile("(?:name is|I am|I'm)\\s+([A-Z][a-zA-Z]*\\s+[A-Z][a-zA-Z]*)");
                Matcher m2 = p2.matcher(prompt);
                if (m2.find()) {
                        return formatName(m2.group(1).trim());
                }

                return null;
        }

        private String formatName(String rawName) {
                if (rawName.split(" ").length > 4)
                        return null;
                String[] words = rawName.split("\\s+");
                StringBuilder sb = new StringBuilder();
                for (String w : words) {
                        if (!w.isEmpty()) {
                                sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase())
                                                .append(" ");
                        }
                }
                return sb.toString().trim();
        }

        private String extractEmail(String prompt) {
                Pattern p1 = Pattern.compile(
                                "(?:email|e-mail)(?:\\s+is|[:\\-])?\\s+([a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,})",
                                Pattern.CASE_INSENSITIVE);
                Matcher m1 = p1.matcher(prompt);
                if (m1.find())
                        return m1.group(1);

                Pattern p2 = Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}",
                                Pattern.CASE_INSENSITIVE);
                Matcher m2 = p2.matcher(prompt);
                if (m2.find())
                        return m2.group();

                return null;
        }

        private String extractPhone(String prompt) {
                Pattern p1 = Pattern.compile(
                                "(?:mobile|phone|call|contact|cell)(?:\\s+is|\\s+at|[:\\-])?\\s+([+\\d\\s\\-().]{10,20})",
                                Pattern.CASE_INSENSITIVE);
                Matcher m1 = p1.matcher(prompt);
                if (m1.find()) {
                        String found = m1.group(1).trim();
                        if (found.replaceAll("[^0-9]", "").length() >= 10)
                                return found;
                }

                Pattern p2 = Pattern.compile("(\\+\\d{1,3}[- ]?)?\\(?\\d{3}\\)?[- ]?\\d{3}[- ]?\\d{4}");
                Matcher m2 = p2.matcher(prompt);
                if (m2.find())
                        return m2.group();

                Pattern p3 = Pattern.compile("\\b\\d{10}\\b");
                Matcher m3 = p3.matcher(prompt);
                if (m3.find())
                        return m3.group();

                return null;
        }

        private String extractSection(String prompt, String sectionName) {
                // Extract content after "Section Name:" until the next section start
                // 1. Find the start of THIS section
                // Matches "Summary:", "Summary -", "Summary "
                Pattern startPattern = Pattern.compile(sectionName + "[:\\-]?\\s+", Pattern.CASE_INSENSITIVE);
                Matcher matcher = startPattern.matcher(prompt);

                if (matcher.find()) {
                        int start = matcher.end();
                        String remaining = prompt.substring(start);

                        // 2. Find the start of the NEXT section (closest one)
                        int nearestEnd = remaining.length();

                        // Keywords that signal a new section
                        String[] keywords = { "Name", "Email", "Mobile", "Phone", "Summary", "Skills", "Experience",
                                        "Education" };

                        for (String kw : keywords) {
                                if (kw.equalsIgnoreCase(sectionName))
                                        continue; // Skip self

                                // Pattern for next section:
                                // Newline OR Space + Keyword + (Colon OR Dash OR Space)
                                // We want to be careful not to match "Experience" inside a sentence like "I
                                // have experience in..."
                                // So reliable markers are: Start of line, or following a punctuation, or
                                // explicit Colon.
                                // Let's look for: \bKeyword[:\-]

                                Pattern nextPattern = Pattern.compile("(?i)(\\n|^|\\.\\s|\\b)" + kw + "[:\\-]?\\s+");
                                Matcher nextMatcher = nextPattern.matcher(remaining);

                                if (nextMatcher.find()) {
                                        int idx = nextMatcher.start();
                                        // If it matched at the very beginning (index 0), it might be a false positive
                                        // if we are just starting?
                                        // But 'remaining' is strictly AFTER the current section header.
                                        if (idx < nearestEnd) {
                                                nearestEnd = idx;
                                        }
                                }
                        }

                        return remaining.substring(0, nearestEnd).trim();
                }
                return null;
        }
}
