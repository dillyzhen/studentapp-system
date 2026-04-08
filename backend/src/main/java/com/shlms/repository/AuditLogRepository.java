package com.shlms.repository;

import com.shlms.entity.AuditLog;
import com.shlms.enums.AuditActionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, String> {

    Page<AuditLog> findByActorIdOrderByCreatedAtDesc(String actorId, Pageable pageable);

    Page<AuditLog> findByActionTypeOrderByCreatedAtDesc(AuditActionType actionType, Pageable pageable);

    Page<AuditLog> findByTargetTypeAndTargetIdOrderByCreatedAtDesc(String targetType, String targetId, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.createdAt BETWEEN :startTime AND :endTime ORDER BY al.createdAt DESC")
    Page<AuditLog> findByTimeRange(@Param("startTime") LocalDateTime startTime, @Param("endTime") LocalDateTime endTime, Pageable pageable);

    @Query("SELECT al FROM AuditLog al WHERE al.actorId = :actorId AND al.actionType = :actionType ORDER BY al.createdAt DESC")
    List<AuditLog> findByActorIdAndActionType(@Param("actorId") String actorId, @Param("actionType") AuditActionType actionType);
}
