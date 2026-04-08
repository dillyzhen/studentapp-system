package com.shlms.service;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.shlms.entity.AdviceReport;
import com.shlms.entity.Student;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class PdfGenerationService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public byte[] generateReportPdf(AdviceReport report) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            Document document = new Document(PageSize.A4, 50, 50, 50, 50);
            PdfWriter.getInstance(document, baos);

            document.open();

            // Title
            addTitle(document, report.getTitle());

            // Student Info
            addStudentInfo(document, report.getStudent());

            // Report Info
            addReportInfo(document, report);

            // Content
            addContent(document, report.getContent());

            // Footer
            addFooter(document, report);

            document.close();

            return baos.toByteArray();
        } catch (Exception e) {
            log.error("Failed to generate PDF for report: {}", report.getId(), e);
            throw new RuntimeException("PDF 生成失败: " + e.getMessage());
        }
    }

    private void addTitle(Document document, String title) throws DocumentException {
        Font titleFont = new Font(Font.HELVETICA, 18, Font.BOLD, new Color(0, 0, 255));
        Paragraph titlePara = new Paragraph(title, titleFont);
        titlePara.setAlignment(Element.ALIGN_CENTER);
        titlePara.setSpacingAfter(20);
        document.add(titlePara);

        // Add line separator
        document.add(Chunk.NEWLINE);
    }

    private void addStudentInfo(Document document, Student student) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(64, 64, 64));
        Paragraph section = new Paragraph("学生信息", sectionFont);
        section.setSpacingAfter(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        addTableRow(table, "姓名:", student.getName(), labelFont, valueFont);
        addTableRow(table, "学号:", student.getStudentNo() != null ? student.getStudentNo() : "-", labelFont, valueFont);
        addTableRow(table, "班级:", student.getClassName() != null ? student.getClassName() : "-", labelFont, valueFont);
        addTableRow(table, "性别:", student.getGender() != null ? student.getGender().getDisplayName() : "-", labelFont, valueFont);
        addTableRow(table, "年龄:", student.getAge() != null ? student.getAge() + " 岁" : "-", labelFont, valueFont);

        document.add(table);
    }

    private void addReportInfo(Document document, AdviceReport report) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(64, 64, 64));
        Paragraph section = new Paragraph("报告信息", sectionFont);
        section.setSpacingAfter(10);
        document.add(section);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(100);
        table.setSpacingAfter(15);

        Font labelFont = new Font(Font.HELVETICA, 10, Font.BOLD);
        Font valueFont = new Font(Font.HELVETICA, 10, Font.NORMAL);

        addTableRow(table, "报告编号:", report.getId(), labelFont, valueFont);
        addTableRow(table, "生成时间:", report.getCreatedAt().format(DATETIME_FORMATTER), labelFont, valueFont);
        addTableRow(table, "审核状态:", report.getStatus().getDisplayName(), labelFont, valueFont);

        if (report.getAuditor() != null) {
            addTableRow(table, "审核人:", report.getAuditor().getName(), labelFont, valueFont);
        }
        if (report.getAuditedAt() != null) {
            addTableRow(table, "审核时间:", report.getAuditedAt().format(DATETIME_FORMATTER), labelFont, valueFont);
        }

        document.add(table);
    }

    private void addContent(Document document, String content) throws DocumentException {
        Font sectionFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(64, 64, 64));
        Paragraph section = new Paragraph("AI 分析建议", sectionFont);
        section.setSpacingAfter(10);
        document.add(section);

        // Parse content and format
        Font contentFont = new Font(Font.HELVETICA, 11, Font.NORMAL);
        String[] lines = content.split("\n");

        for (String line : lines) {
            if (line.trim().isEmpty()) {
                document.add(Chunk.NEWLINE);
                continue;
            }

            // Check if it's a header line
            if (line.startsWith("【") && line.endsWith("】")) {
                Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(0, 0, 255));
                Paragraph header = new Paragraph(line, headerFont);
                header.setSpacingBefore(10);
                header.setSpacingAfter(5);
                document.add(header);
            }
            // Check if it's a numbered point
            else if (line.matches("^\\d+\\..*")) {
                Font pointFont = new Font(Font.HELVETICA, 11, Font.BOLD);
                Paragraph point = new Paragraph(line, pointFont);
                point.setSpacingBefore(5);
                document.add(point);
            }
            // Regular content
            else {
                Paragraph para = new Paragraph(line, contentFont);
                para.setFirstLineIndent(20);
                document.add(para);
            }
        }

        document.add(Chunk.NEWLINE);
    }

    private void addFooter(Document document, AdviceReport report) throws DocumentException {
        document.add(Chunk.NEWLINE);

        Font footerFont = new Font(Font.HELVETICA, 9, Font.ITALIC, new Color(128, 128, 128));

        Paragraph footer1 = new Paragraph(
            "本报告由 SHLMS 学生健康档案与信息管理系统自动生成",
            footerFont
        );
        footer1.setAlignment(Element.ALIGN_CENTER);
        document.add(footer1);

        Paragraph footer2 = new Paragraph(
            "报告生成时间: " + LocalDateTime.now().format(DATETIME_FORMATTER),
            footerFont
        );
        footer2.setAlignment(Element.ALIGN_CENTER);
        document.add(footer2);

        Paragraph footer3 = new Paragraph(
            "本报告仅供参考，具体教育决策请结合实际情况",
            footerFont
        );
        footer3.setAlignment(Element.ALIGN_CENTER);
        document.add(footer3);
    }

    private void addTableRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderWidth(0.5f);
        labelCell.setBackgroundColor(new Color(211, 211, 211));
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorderWidth(0.5f);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}
