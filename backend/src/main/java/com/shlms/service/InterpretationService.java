package com.shlms.service;

import com.shlms.ai.AiSessionManager;
import com.shlms.dto.*;
import com.shlms.entity.*;
import com.shlms.enums.AuditActionType;
import com.shlms.enums.DiffField;
import com.shlms.enums.ReportStatus;
import com.shlms.exception.ResourceNotFoundException;
import com.shlms.exception.UnauthorizedException;
import com.shlms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class InterpretationService {

    private final RawRecordRepository rawRecordRepository;
    private final AiInterpretationRepository interpretationRepository;
    private final DiffRecordRepository diffRecordRepository;
    private final AdviceReportRepository adviceReportRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final AiSessionManager sessionManager;
    private final AiService aiService;
    private final AuditLogService auditLogService;
    private final TimelineService timelineService;

    @Transactional
    public InterpretationResponse startInterpretation(String rawRecordId, String teacherId) {
        RawRecord rawRecord = rawRecordRepository.findById(rawRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("原始记录不存在"));

        // Verify teacher has access to this student
        Student student = rawRecord.getStudent();
        // Note: Teacher access verification should be done before calling this method

        // Generate history summary
        List<RawRecord> studentRecords = rawRecordRepository.findByStudentIdOrderBySubmittedAtDesc(student.getId());
        String historySummary = aiService.generateHistorySummary(student.getId(), studentRecords);

        // Create or get AI session
        AiSessionManager.AiSession session = sessionManager.getSession(student.getId(), teacherId);
        if (session == null) {
            session = sessionManager.createSession(student.getId(), teacherId, historySummary);
        }

        // Update raw record status
        rawRecord.setStatus(com.shlms.enums.RecordStatus.PROCESSING);
        rawRecordRepository.save(rawRecord);

        return InterpretationResponse.builder()
                .sessionId(session.getSessionId())
                .studentId(student.getId())
                .studentName(student.getName())
                .rawRecordId(rawRecordId)
                .rawContent(rawRecord.getContent())
                .historySummary(historySummary)
                .sessionRemainingMinutes(sessionManager.getSessionRemainingTime(student.getId(), teacherId))
                .build();
    }

    @Transactional
    public AiInterpretationResponse generateInterpretation(String rawRecordId, String teacherId) {
        RawRecord rawRecord = rawRecordRepository.findById(rawRecordId)
                .orElseThrow(() -> new ResourceNotFoundException("原始记录不存在"));

        Student student = rawRecord.getStudent();
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("老师不存在"));

        // Get or create session
        AiSessionManager.AiSession session = sessionManager.getSession(student.getId(), teacherId);
        if (session == null) {
            throw new RuntimeException("AI Session 不存在，请先开始解读");
        }

        // Validate session isolation
        sessionManager.validateSessionIsolation(student.getId(), teacherId, student.getId());

        // Call AI service
        AiService.InterpretationResult result = aiService.interpret(
                student.getId(),
                teacherId,
                session.getHistorySummary(),
                rawRecord.getContent(),
                rawRecordId
        );

        if (!result.isSuccess()) {
            return AiInterpretationResponse.builder()
                    .success(false)
                    .errorMessage(result.getErrorMessage())
                    .build();
        }

        // Save interpretation record
        AiInterpretation interpretation = AiInterpretation.builder()
                .rawRecord(rawRecord)
                .sessionId(result.getSessionId())
                .systemPrompt(result.getSystemPrompt())
                .userPrompt(result.getUserPrompt())
                .rawResponse(result.getContent())
                .interpretationContent(result.getContent())
                .inputTokens(result.getInputTokens())
                .outputTokens(result.getOutputTokens())
                .costUsd(BigDecimal.valueOf(result.getCostUsd()))
                .modelVersion(result.getModelVersion())
                .traceId(result.getTraceId())
                .durationMs(result.getDurationMs())
                .build();

        AiInterpretation saved = interpretationRepository.save(interpretation);

        // Record timeline event
        timelineService.recordInterpretationGenerated(student, saved.getId(), teacherId);

        // Create initial advice report
        AdviceReport report = AdviceReport.builder()
                .interpretation(saved)
                .student(student)
                .title(generateReportTitle(student, rawRecord))
                .content(result.getContent())
                .status(ReportStatus.DRAFT)
                .build();
        adviceReportRepository.save(report);

        // Audit log
        auditLogService.log(AuditActionType.INTERPRETATION_GENERATE, teacherId, teacher.getRole().name(),
                "AI_INTERPRETATION", saved.getId(),
                "RawRecord: " + rawRecordId + ", Tokens: " + result.getTotalTokens() + ", Cost: $" + String.format("%.6f", result.getCostUsd()));

        return AiInterpretationResponse.builder()
                .success(true)
                .interpretationId(saved.getId())
                .reportId(report.getId())
                .content(result.getContent())
                .inputTokens(result.getInputTokens())
                .outputTokens(result.getOutputTokens())
                .totalTokens(result.getTotalTokens())
                .costUsd(result.getCostUsd())
                .modelVersion(result.getModelVersion())
                .traceId(result.getTraceId())
                .durationMs(result.getDurationMs())
                .build();
    }

    @Transactional
    public void editInterpretation(String interpretationId, String teacherId, InterpretationEditRequest request) {
        AiInterpretation interpretation = interpretationRepository.findById(interpretationId)
                .orElseThrow(() -> new ResourceNotFoundException("解读记录不存在"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("老师不存在"));

        AdviceReport report = adviceReportRepository.findByInterpretationId(interpretationId)
                .orElseThrow(() -> new ResourceNotFoundException("建议报告不存在"));

        String beforeContent = report.getContent();
        String afterContent = request.getContent();

        if (!beforeContent.equals(afterContent)) {
            // Save diff record
            DiffRecord diff = DiffRecord.builder()
                    .interpretation(interpretation)
                    .fieldName(DiffField.CONTENT.name())
                    .beforeValue(beforeContent)
                    .afterValue(afterContent)
                    .editor(teacher)
                    .editReason(request.getEditReason())
                    .build();
            diffRecordRepository.save(diff);

            // Update report
            report.setContent(afterContent);
            report.setUpdatedAt(LocalDateTime.now());
            adviceReportRepository.save(report);

            // Audit log
            auditLogService.log(AuditActionType.INTERPRETATION_EDIT, teacherId, teacher.getRole().name(),
                    "AI_INTERPRETATION", interpretationId,
                    "Edit reason: " + request.getEditReason());
        }
    }

    @Transactional
    public void approveReport(String reportId, String teacherId, ReportApproveRequest request) {
        AdviceReport report = adviceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("报告不存在"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("老师不存在"));

        report.setStatus(ReportStatus.APPROVED);
        report.setAuditor(teacher);
        report.setAuditedAt(LocalDateTime.now());
        report.setAuditComment(request.getComment());
        report.setUpdatedAt(LocalDateTime.now());

        adviceReportRepository.save(report);

        // Update raw record status
        RawRecord rawRecord = report.getInterpretation().getRawRecord();
        rawRecord.setStatus(com.shlms.enums.RecordStatus.COMPLETED);
        rawRecordRepository.save(rawRecord);

        // Audit log
        auditLogService.log(AuditActionType.REPORT_APPROVE, teacherId, teacher.getRole().name(),
                "ADVICE_REPORT", reportId, "Approved: " + request.getComment());
    }

    @Transactional
    public void distributeReport(String reportId, String teacherId) {
        AdviceReport report = adviceReportRepository.findById(reportId)
                .orElseThrow(() -> new ResourceNotFoundException("报告不存在"));

        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("老师不存在"));

        if (report.getStatus() != ReportStatus.APPROVED) {
            throw new RuntimeException("报告未审核，无法分发");
        }

        report.setStatus(ReportStatus.DISTRIBUTED);
        report.setDistributedAt(LocalDateTime.now());
        report.setUpdatedAt(LocalDateTime.now());

        adviceReportRepository.save(report);

        // Record timeline event
        timelineService.recordReportDistributed(report.getStudent(), reportId, teacherId);

        // Audit log
        auditLogService.log(AuditActionType.REPORT_DISTRIBUTE, teacherId, teacher.getRole().name(),
                "ADVICE_REPORT", reportId, "Distributed to parents");
    }

    @Transactional(readOnly = true)
    public List<AdviceReportResponse> getTeacherReports(String teacherId, ReportStatus status) {
        return adviceReportRepository.findByTeacherIdAndStatus(teacherId, status)
                .stream()
                .map(this::convertToReportResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RawRecordResponse> getPendingSubmissions(String teacherId) {
        return rawRecordRepository.findPendingByTeacherId(teacherId, com.shlms.enums.RecordStatus.PENDING)
                .stream()
                .map(this::convertToRawRecordResponse)
                .collect(Collectors.toList());
    }

    private String generateReportTitle(Student student, RawRecord rawRecord) {
        String typeDisplay = rawRecord.getType().getDisplayName();
        return String.format("%s - %s分析报告 (%s)",
                student.getName(),
                typeDisplay,
                LocalDateTime.now().toLocalDate());
    }

    private AdviceReportResponse convertToReportResponse(AdviceReport report) {
        return AdviceReportResponse.builder()
                .id(report.getId())
                .studentId(report.getStudent().getId())
                .studentName(report.getStudent().getName())
                .title(report.getTitle())
                .content(report.getContent())
                .status(report.getStatus())
                .statusDisplayName(report.getStatus().getDisplayName())
                .auditorName(report.getAuditor() != null ? report.getAuditor().getName() : null)
                .auditedAt(report.getAuditedAt())
                .distributedAt(report.getDistributedAt())
                .createdAt(report.getCreatedAt())
                .build();
    }

    private RawRecordResponse convertToRawRecordResponse(RawRecord record) {
        return RawRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent().getId())
                .studentName(record.getStudent().getName())
                .type(record.getType())
                .typeDisplayName(record.getType().getDisplayName())
                .content(record.getContent())
                .status(record.getStatus())
                .statusDisplayName(record.getStatus().getDisplayName())
                .submittedAt(record.getSubmittedAt())
                .build();
    }
}
