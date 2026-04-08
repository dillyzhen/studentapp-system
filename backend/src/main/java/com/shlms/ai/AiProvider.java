package com.shlms.ai;

import lombok.Builder;
import lombok.Data;

public interface AiProvider {

    AiResponse interpret(AiRequest request);

    @Data
    @Builder
    class AiRequest {
        private String systemPrompt;
        private String userPrompt;
        private String historySummary;
        private String currentContent;
        private String model;
        private Integer maxTokens;
        private String studentId;
    }

    @Data
    @Builder
    class AiResponse {
        private String content;
        private Integer inputTokens;
        private Integer outputTokens;
        private String modelVersion;
        private String traceId;
        private Long durationMs;
        private boolean success;
        private String errorMessage;
    }
}
