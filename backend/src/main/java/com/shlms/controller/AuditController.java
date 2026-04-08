package com.shlms.controller;

import com.shlms.dto.ApiResponse;
import com.shlms.dto.AuditLogResponse;
import com.shlms.entity.AuditLog;
import com.shlms.enums.AuditActionType;
import com.shlms.repository.AuditLogRepository;
import com.shlms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping("/logs")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getAuditLogs(
            @RequestParam(required = false) String actorId,
            @RequestParam(required = false) AuditActionType actionType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logs;

        if (actorId != null) {
            logs = auditLogRepository.findByActorIdOrderByCreatedAtDesc(actorId, pageable);
        } else if (actionType != null) {
            logs = auditLogRepository.findByActionTypeOrderByCreatedAtDesc(actionType, pageable);
        } else {
            logs = auditLogRepository.findAll(pageable);
        }

        List<AuditLogResponse> response = logs.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/my-logs")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'PARENT')")
    public ResponseEntity<ApiResponse<List<AuditLogResponse>>> getMyAuditLogs(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        Page<AuditLog> logs = auditLogRepository.findByActorIdOrderByCreatedAtDesc(currentUser.getId(), pageable);

        List<AuditLogResponse> response = logs.getContent().stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private AuditLogResponse convertToResponse(AuditLog log) {
        return AuditLogResponse.builder()
                .id(log.getId())
                .actionType(log.getActionType())
                .actionTypeDisplayName(log.getActionType() != null ? log.getActionType().getDisplayName() : null)
                .actorId(log.getActorId())
                .actorRole(log.getActorRole())
                .targetType(log.getTargetType())
                .targetId(log.getTargetId())
                .details(log.getDetails())
                .ipAddress(log.getIpAddress())
                .createdAt(log.getCreatedAt())
                .build();
    }
}
