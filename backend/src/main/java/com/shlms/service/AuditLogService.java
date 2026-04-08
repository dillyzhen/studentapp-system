package com.shlms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shlms.entity.AuditLog;
import com.shlms.enums.AuditActionType;
import com.shlms.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void log(AuditActionType actionType, String actorId, String actorRole,
                   String targetType, String targetId, String details) {
        try {
            AuditLog auditLog = AuditLog.builder()
                    .actionType(actionType)
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .targetType(targetType)
                    .targetId(targetId)
                    .details(details)
                    .build();

            auditLogRepository.save(auditLog);
            log.debug("Audit log saved: {} by {} on {}", actionType, actorId, targetId);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
            // Don't throw - audit logging should not break the main flow
        }
    }

    @Transactional
    public void logWithIp(AuditActionType actionType, String actorId, String actorRole,
                         String targetType, String targetId, Object details,
                         String ipAddress, String userAgent) {
        try {
            String detailsJson = objectMapper.writeValueAsString(details);

            AuditLog auditLog = AuditLog.builder()
                    .actionType(actionType)
                    .actorId(actorId)
                    .actorRole(actorRole)
                    .targetType(targetType)
                    .targetId(targetId)
                    .details(detailsJson)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .build();

            auditLogRepository.save(auditLog);
        } catch (Exception e) {
            log.error("Failed to save audit log", e);
        }
    }
}
