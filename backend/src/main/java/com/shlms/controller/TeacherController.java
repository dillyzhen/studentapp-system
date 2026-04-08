package com.shlms.controller;

import com.shlms.dto.*;
import com.shlms.enums.ReportStatus;
import com.shlms.security.UserPrincipal;
import com.shlms.service.InterpretationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/teacher")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'ADMIN')")
public class TeacherController {

    private final InterpretationService interpretationService;

    // Dashboard
    @GetMapping("/dashboard/pending-submissions")
    public ResponseEntity<ApiResponse<List<RawRecordResponse>>> getPendingSubmissions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<RawRecordResponse> submissions = interpretationService.getPendingSubmissions(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }

    @GetMapping("/dashboard/reports")
    public ResponseEntity<ApiResponse<List<AdviceReportResponse>>> getMyReports(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(required = false) ReportStatus status) {
        if (status == null) {
            status = ReportStatus.DRAFT;
        }
        List<AdviceReportResponse> reports = interpretationService.getTeacherReports(currentUser.getId(), status);
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    // AI Interpretation Workbench
    @PostMapping("/interpretations/start/{rawRecordId}")
    public ResponseEntity<ApiResponse<InterpretationResponse>> startInterpretation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String rawRecordId) {
        InterpretationResponse response = interpretationService.startInterpretation(rawRecordId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("会话已创建", response));
    }

    @PostMapping("/interpretations/generate/{rawRecordId}")
    public ResponseEntity<ApiResponse<AiInterpretationResponse>> generateInterpretation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String rawRecordId) {
        AiInterpretationResponse response = interpretationService.generateInterpretation(rawRecordId, currentUser.getId());
        if (response.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success("AI 解读完成", response));
        } else {
            return ResponseEntity.ok(ApiResponse.error(response.getErrorMessage()));
        }
    }

    @PutMapping("/interpretations/{interpretationId}/edit")
    public ResponseEntity<ApiResponse<Void>> editInterpretation(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String interpretationId,
            @Valid @RequestBody InterpretationEditRequest request) {
        interpretationService.editInterpretation(interpretationId, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("编辑已保存", null));
    }

    @PostMapping("/reports/{reportId}/approve")
    public ResponseEntity<ApiResponse<Void>> approveReport(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String reportId,
            @Valid @RequestBody ReportApproveRequest request) {
        interpretationService.approveReport(reportId, currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.success("审核通过", null));
    }

    @PostMapping("/reports/{reportId}/distribute")
    public ResponseEntity<ApiResponse<Void>> distributeReport(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String reportId) {
        interpretationService.distributeReport(reportId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success("报告已分发", null));
    }
}
