package com.shlms.controller;

import com.shlms.dto.*;
import com.shlms.service.ParentService;
import com.shlms.service.RawRecordService;
import com.shlms.service.ReportService;
import com.shlms.security.UserPrincipal;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/parent")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('PARENT', 'ADMIN')")
public class ParentController {

    private final ParentService parentService;
    private final RawRecordService rawRecordService;
    private final ReportService reportService;

    @GetMapping("/students")
    public ResponseEntity<ApiResponse<List<StudentBriefResponse>>> getMyStudents(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<StudentBriefResponse> students = parentService.getMyStudents(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    @GetMapping("/submissions")
    public ResponseEntity<ApiResponse<List<RawRecordResponse>>> getMySubmissions(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<RawRecordResponse> submissions = rawRecordService.getBySubmitterId(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }

    @GetMapping("/submissions/student/{studentId}")
    public ResponseEntity<ApiResponse<List<RawRecordResponse>>> getStudentSubmissions(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String studentId) {
        // Verify the student belongs to this parent
        parentService.verifyParentStudentAccess(currentUser.getId(), studentId);
        List<RawRecordResponse> submissions = rawRecordService.getByStudentId(studentId);
        return ResponseEntity.ok(ApiResponse.success(submissions));
    }

    @PostMapping("/submissions")
    public ResponseEntity<ApiResponse<RawRecordResponse>> submitRecord(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @Valid @RequestBody RawRecordRequest request,
            HttpServletRequest httpRequest) {
        // Verify the student belongs to this parent
        parentService.verifyParentStudentAccess(currentUser.getId(), request.getStudentId());

        RawRecordResponse response = rawRecordService.create(
                request,
                currentUser.getId(),
                httpRequest.getRemoteAddr(),
                httpRequest.getHeader("User-Agent")
        );
        return ResponseEntity.ok(ApiResponse.success("提交成功", response));
    }

    @PostMapping("/submissions/{submissionId}/images")
    public ResponseEntity<ApiResponse<String>> uploadImage(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String submissionId,
            @RequestParam("file") MultipartFile file) {
        // Verify ownership
        rawRecordService.verifyOwnership(submissionId, currentUser.getId());
        String imageUrl = rawRecordService.uploadImage(submissionId, file);
        return ResponseEntity.ok(ApiResponse.success("上传成功", imageUrl));
    }

    // Report endpoints for parents
    @GetMapping("/reports")
    public ResponseEntity<ApiResponse<List<ReportDetailResponse>>> getMyReports(
            @AuthenticationPrincipal UserPrincipal currentUser) {
        List<ReportDetailResponse> reports = reportService.getReportsForParent(currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(reports));
    }

    @GetMapping("/reports/{reportId}")
    public ResponseEntity<ApiResponse<ReportDetailResponse>> getReportDetail(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String reportId) {
        ReportDetailResponse report = reportService.getReportDetail(reportId, currentUser.getId());
        return ResponseEntity.ok(ApiResponse.success(report));
    }

    @GetMapping("/reports/{reportId}/pdf")
    public ResponseEntity<byte[]> downloadReportPdf(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String reportId) {
        return reportService.downloadReportPdf(reportId, currentUser.getId());
    }
}
