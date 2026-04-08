package com.shlms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    private Long totalUsers;
    private Long totalTeachers;
    private Long totalParents;
    private Long totalStudents;

    private Long totalSubmissions;
    private Long pendingSubmissions;
    private Long completedSubmissions;

    private Long totalReports;
    private Long draftReports;
    private Long approvedReports;
    private Long distributedReports;

    private Long totalInterpretations;
    private Double totalAiCostUsd;
    private Long totalAiTokens;
}
