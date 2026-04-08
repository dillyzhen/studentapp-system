package com.shlms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiCostStatsResponse {

    private String startDate;
    private String endDate;
    private BigDecimal totalCostUsd;
    private Long totalTokens;
    private Long totalInterpretations;
    private BigDecimal averageCostPerInterpretation;
    private List<TeacherAiCost> teacherCosts;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TeacherAiCost {
        private String teacherId;
        private String teacherName;
        private Long interpretationCount;
        private BigDecimal totalCostUsd;
        private Long totalTokens;
    }
}
