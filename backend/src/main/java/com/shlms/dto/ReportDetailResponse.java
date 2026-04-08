package com.shlms.dto;

import com.shlms.enums.ReportStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportDetailResponse {

    private String id;
    private String title;
    private String content;
    private String studentId;
    private String studentName;
    private String studentClass;
    private ReportStatus status;
    private String statusDisplayName;
    private String auditorName;
    private String auditComment;
    private LocalDateTime auditedAt;
    private LocalDateTime distributedAt;
    private LocalDateTime firstViewedAt;
    private Integer viewCount;
    private Integer downloadCount;
    private LocalDateTime createdAt;
    private BigDecimal aiCostUsd;
    private Integer aiTokens;
}
