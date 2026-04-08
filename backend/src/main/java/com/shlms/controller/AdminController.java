package com.shlms.controller;

import com.shlms.dto.*;
import com.shlms.enums.UserRole;
import com.shlms.service.AdminService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;

    // User Management
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @RequestParam(required = false) UserRole role) {
        List<UserResponse> users = adminService.getAllUsers(role);
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    @PostMapping("/users")
    public ResponseEntity<ApiResponse<UserResponse>> createUser(
            @Valid @RequestBody CreateUserRequest request) {
        UserResponse user = adminService.createUser(request);
        return ResponseEntity.ok(ApiResponse.success("用户创建成功", user));
    }

    @PutMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<UserResponse>> updateUser(
            @PathVariable String userId,
            @Valid @RequestBody UpdateUserRequest request) {
        UserResponse user = adminService.updateUser(userId, request);
        return ResponseEntity.ok(ApiResponse.success("用户更新成功", user));
    }

    @DeleteMapping("/users/{userId}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(
            @PathVariable String userId) {
        adminService.deleteUser(userId);
        return ResponseEntity.ok(ApiResponse.success("用户删除成功", null));
    }

    // Teacher-Student Assignment
    @PostMapping("/assignments")
    public ResponseEntity<ApiResponse<Void>> assignStudentToTeacher(
            @Valid @RequestBody AssignmentRequest request) {
        adminService.assignStudentToTeacher(request.getTeacherId(), request.getStudentId(), request.getNotes());
        return ResponseEntity.ok(ApiResponse.success("分配成功", null));
    }

    @DeleteMapping("/assignments")
    public ResponseEntity<ApiResponse<Void>> unassignStudentFromTeacher(
            @RequestParam String teacherId,
            @RequestParam String studentId) {
        adminService.unassignStudentFromTeacher(teacherId, studentId);
        return ResponseEntity.ok(ApiResponse.success("取消分配成功", null));
    }

    @GetMapping("/teachers/{teacherId}/students")
    public ResponseEntity<ApiResponse<List<StudentBriefResponse>>> getTeacherStudents(
            @PathVariable String teacherId) {
        List<StudentBriefResponse> students = adminService.getTeacherStudents(teacherId);
        return ResponseEntity.ok(ApiResponse.success(students));
    }

    // Dashboard Statistics
    @GetMapping("/dashboard/stats")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        DashboardStatsResponse stats = adminService.getDashboardStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // AI Cost Statistics
    @GetMapping("/stats/ai-costs")
    public ResponseEntity<ApiResponse<AiCostStatsResponse>> getAiCostStats(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate) {
        AiCostStatsResponse stats = adminService.getAiCostStats(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}
