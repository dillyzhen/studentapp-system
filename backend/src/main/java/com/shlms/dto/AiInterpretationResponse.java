package com.shlms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiInterpretationResponse {

    private boolean success;
    private String interpretationId;
    private String reportId;
    private String content;
    private String errorMessage;
    private Integer inputTokens;
    private Integer outputTokens;
    private Integer totalTokens;
    private Double costUsd;
    private String modelVersion;
    private String traceId;
    private Long durationMs;
}
