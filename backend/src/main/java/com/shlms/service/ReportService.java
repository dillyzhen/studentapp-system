package com.shlms.service;

import com.shlms.dto.ReportDetailResponse;
import com.shlms.entity.AdviceReport;
import com.shlms.entity.AiInterpretation;
import com.shlms.entity.Student;
import com.shlms.enums.AuditActionType;
import com.shlms.enums.ReportStatus;
import com.shlms.exception.ResourceNotFoundException;
import com.shlms.exception.UnauthorizedException;
import com.shlms.repository.AdviceReportRepository;
import com.shlms.repository.UserStudentBindingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {

    private final AdviceReportRepository adviceReportRepository;
    private final UserStudentBindingRepository bindingRepository;
    private final PdfGenerationService pdfGenerationService;
    private final TimelineService timelineService;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<ReportDetailResponse> getReportsForParent(String parentId) {
        // Get all students bound to this parent
        List<String> studentIds = bindingRepository.findByUserId(parentId)
                .stream()
                .map(binding -> binding.getStudent().getId())
                .collect(Collectors.toList());

        // Get distributed reports for these students
        return adviceReportRepository.findAll().stream()
                .filter(report -> studentIds.contains(report.getStudent().getId()))
                .filter(report -> report.getStatus() == ReportStatus.DISTRIBUTED)
                .sorted((a, b) -> b.getDistributedAt().compareTo(a.getDistributedAt()))
                .map(this::convertToDetailResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ReportDetailResponse getReportDetail(String reportId, String parentId) {
        AdviceReport report = adviceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("报告不存在"));

        // Verify parent has access to this student
        boolean hasAccess = bindingRepository.existsByUserIdAndStudentId(parentId, report.getStudent().getId());
        if (!hasAccess) {
            throw new UnauthorizedException("无权查看该报告");
        }

        // Update view count and first viewed time
        if (report.getFirstViewedAt() == null) {
            report.setFirstViewedAt(LocalDateTime.now());
        }
        report.setViewCount(report.getViewCount() + 1);
        adviceReportRepository.save(report);

        // Audit log
        auditLogService.log(AuditActionType.REPORT_VIEW, parentId, "PARENT",
                "ADVICE_REPORT", reportId, "Parent viewed report");

        return convertToDetailResponse(report);
    }

    @Transactional
    public ResponseEntity<byte[]> downloadReportPdf(String reportId, String parentId) {
        AdviceReport report = adviceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("报告不存在"));

        // Verify parent has access
        boolean hasAccess = bindingRepository.existsByUserIdAndStudentId(parentId, report.getStudent().getId());
        if (!hasAccess) {
            throw new UnauthorizedException("无权下载该报告");
        }

        // Generate PDF
        byte[] pdfBytes = pdfGenerationService.generateReportPdf(report);

        // Update download count
        report.setDownloadCount(report.getDownloadCount() + 1);
        adviceReportRepository.save(report);

        // Audit log
        auditLogService.log(AuditActionType.REPORT_DOWNLOAD, parentId, "PARENT",
                "ADVICE_REPORT", reportId, "Parent downloaded PDF");

        String fileName = String.format("%s_%s_%s.pdf",
                report.getStudent().getName(),
                report.getTitle().replaceAll("[^\\u4e00-\\u9fa5a-zA-Z0-9]", "_"),
                LocalDateTime.now().toLocalDate());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + fileName + "\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdfBytes);
    }

    @Transactional(readOnly = true)
    public ReportDetailResponse getReportForTeacher(String reportId, String teacherId) {
        AdviceReport report = adviceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("报告不存在"));

        // Teacher can view reports for students assigned to them
        // (Assuming teacher access is verified at controller level)

        return convertToDetailResponse(report);
    }

    private ReportDetailResponse convertToDetailResponse(AdviceReport report) {
        Student student = report.getStudent();
        AiInterpretation interpretation = report.getInterpretation();

        return ReportDetailResponse.builder()
                .id(report.getId())
                .title(report.getTitle())
                .content(report.getContent())
                .studentId(student.getId())
                .studentName(student.getName())
                .studentClass(student.getClassName())
                .status(report.getStatus())
                .statusDisplayName(report.getStatus().getDisplayName())
                .auditorName(report.getAuditor() != null ? report.getAuditor().getName() : null)
                .auditComment(report.getAuditComment())
                .auditedAt(report.getAuditedAt())
                .distributedAt(report.getDistributedAt())
                .firstViewedAt(report.getFirstViewedAt())
                .viewCount(report.getViewCount())
                .downloadCount(report.getDownloadCount())
                .createdAt(report.getCreatedAt())
                .aiCostUsd(interpretation != null ? interpretation.getCostUsd() : null)
                .aiTokens(interpretation != null ? interpretation.getInputTokens() + interpretation.getOutputTokens() : null)
                .build();
    }
}
