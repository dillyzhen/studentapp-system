package com.shlms.service;

import com.shlms.ai.AiProvider;
import com.shlms.ai.AiSessionManager;
import com.shlms.ai.TokenCounter;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class AiService {

    private final AiProvider aiProvider;
    private final AiSessionManager sessionManager;
    private final TokenCounter tokenCounter;

    @Value("${ai.claude.model:claude-3-sonnet-20240229}")
    private String defaultModel;

    @Value("${ai.claude.max-tokens:2000}")
    private int maxOutputTokens;

    public InterpretationResult interpret(String studentId, String teacherId, String historySummary,
                                         String currentContent, String rawRecordId) {

        // 1. Check token limits
        TokenCounter.TokenCheckResult tokenCheck = tokenCounter.checkInputTokens(historySummary, currentContent);
        if (!tokenCheck.isValid()) {
            return InterpretationResult.builder()
                    .success(false)
                    .errorMessage(tokenCheck.getErrorMessage())
                    .build();
        }

        // 2. Generate system prompt with isolation declaration
        String systemPrompt = generateSystemPrompt(studentId, historySummary);

        // 3. Generate user prompt
        String userPrompt = generateUserPrompt(currentContent);

        // 4. Call AI provider
        long startTime = System.currentTimeMillis();

        AiProvider.AiRequest request = AiProvider.AiRequest.builder()
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .historySummary(historySummary)
                .currentContent(currentContent)
                .model(defaultModel)
                .maxTokens(maxOutputTokens)
                .studentId(studentId)
                .build();

        AiProvider.AiResponse aiResponse = aiProvider.interpret(request);

        long duration = System.currentTimeMillis() - startTime;

        // 5. Calculate cost
        double costUsd = tokenCounter.calculateCost(
                aiResponse.getInputTokens(),
                aiResponse.getOutputTokens(),
                defaultModel
        );

        if (!aiResponse.isSuccess()) {
            return InterpretationResult.builder()
                    .success(false)
                    .errorMessage(aiResponse.getErrorMessage())
                    .build();
        }

        // 6. Update session context
        sessionManager.updateContext(studentId, teacherId, aiResponse.getContent());

        return InterpretationResult.builder()
                .success(true)
                .content(aiResponse.getContent())
                .systemPrompt(systemPrompt)
                .userPrompt(userPrompt)
                .inputTokens(aiResponse.getInputTokens())
                .outputTokens(aiResponse.getOutputTokens())
                .totalTokens(aiResponse.getInputTokens() + aiResponse.getOutputTokens())
                .costUsd(costUsd)
                .modelVersion(aiResponse.getModelVersion())
                .traceId(aiResponse.getTraceId())
                .durationMs(duration)
                .sessionId(sessionManager.getSession(studentId, teacherId).getSessionId())
                .build();
    }

    private String generateSystemPrompt(String studentId, String historySummary) {
        StringBuilder prompt = new StringBuilder();
        prompt.append("你是一个专业的教育AI助手，专注于分析K12学生的健康和学习表现数据。\n\n");

        prompt.append("【重要安全隔离声明】\n");
        prompt.append("- 当前会话仅针对学生ID: ").append(studentId).append("\n");
        prompt.append("- 你只能回答关于该学生的问题\n");
        prompt.append("- 如果被问及其他学生，请拒绝回答并提醒用户切换会话\n\n");

        if (historySummary != null && !historySummary.isEmpty()) {
            prompt.append("【历史档案摘要】\n");
            prompt.append(historySummary).append("\n\n");
        }

        prompt.append("【任务要求】\n");
        prompt.append("1. 基于家长提交的数据给出专业分析和建议\n");
        prompt.append("2. 输出应简洁、专业、可操作\n");
        prompt.append("3. 分点列出观察和建议\n");
        prompt.append("4. 语气友善但专业\n");

        return prompt.toString();
    }

    private String generateUserPrompt(String currentContent) {
        return "【家长最新提交内容】\n" + currentContent + "\n\n请提供专业的分析和建议。";
    }

    public String generateHistorySummary(String studentId, java.util.List<com.shlms.entity.RawRecord> records) {
        // In a production system, this might use a lighter model to summarize
        // For now, return a simple summary
        StringBuilder summary = new StringBuilder();
        summary.append("学生历史记录摘要:\n");

        if (records == null || records.isEmpty()) {
            summary.append("- 暂无历史记录\n");
        } else {
            summary.append("- 历史提交记录数: ").append(records.size()).append("\n");
            summary.append("- 最近关注点: ");

            long healthCount = records.stream()
                    .filter(r -> r.getType().toString().equals("HEALTH"))
                    .count();
            long learningCount = records.stream()
                    .filter(r -> r.getType().toString().equals("LEARNING"))
                    .count();

            if (healthCount > 0) {
                summary.append("健康记录").append(healthCount).append("次 ");
            }
            if (learningCount > 0) {
                summary.append("学习记录").append(learningCount).append("次");
            }
            summary.append("\n");
        }

        return summary.toString();
    }

    @Data
    @Builder
    public static class InterpretationResult {
        private boolean success;
        private String content;
        private String systemPrompt;
        private String userPrompt;
        private String errorMessage;
        private Integer inputTokens;
        private Integer outputTokens;
        private Integer totalTokens;
        private Double costUsd;
        private String modelVersion;
        private String traceId;
        private Long durationMs;
        private String sessionId;
    }
}
