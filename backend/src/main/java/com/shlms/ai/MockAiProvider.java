package com.shlms.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@ConditionalOnProperty(name = "ai.provider", havingValue = "mock", matchIfMissing = true)
public class MockAiProvider implements AiProvider {

    @Override
    public AiResponse interpret(AiRequest request) {
        log.info("Mock AI interpretation for student: {}", request.getStudentId());

        // Simulate processing time
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // Generate mock response based on content type
        String content = generateMockResponse(request);

        // Estimate tokens
        int inputTokens = estimateTokens(request.getSystemPrompt())
                + estimateTokens(request.getUserPrompt());
        int outputTokens = estimateTokens(content);

        return AiResponse.builder()
                .content(content)
                .inputTokens(inputTokens)
                .outputTokens(outputTokens)
                .modelVersion("mock-ai-v1")
                .traceId(UUID.randomUUID().toString())
                .durationMs(500L)
                .success(true)
                .build();
    }

    private String generateMockResponse(AiRequest request) {
        String currentContent = request.getCurrentContent();

        if (currentContent.contains("健康") || currentContent.contains("体检") || currentContent.contains("身高") || currentContent.contains("体重")) {
            return "【AI 健康分析建议】\n\n" +
                   "根据家长提交的体检数据：\n\n" +
                   "1. 生长发育评估\n" +
                   "   - 身高体重处于同龄儿童正常范围内\n" +
                   "   - 视力良好（左眼5.0，右眼4.9），注意保持用眼卫生\n\n" +
                   "2. 关注要点\n" +
                   "   - 夜间磨牙现象可能与精神压力或牙齿咬合有关\n" +
                   "   - 建议观察磨牙频率，必要时咨询牙医\n\n" +
                   "3. 运动建议\n" +
                   "   - 足球训练有助于体能发展\n" +
                   "   - 建议每周保持3-4次运动频率\n\n" +
                   "4. 营养建议\n" +
                   "   - 食欲正常，继续保持均衡饮食\n" +
                   "   - 适当补充钙质，促进骨骼发育\n\n" +
                   "请老师关注孩子在校的精神状态，如有磨牙加重情况请及时反馈。";
        } else if (currentContent.contains("学习") || currentContent.contains("成绩") || currentContent.contains("作业")) {
            return "【AI 学习分析建议】\n\n" +
                   "根据家长反馈的学习情况：\n\n" +
                   "1. 数学学科\n" +
                   "   - 分数加减法掌握度85%，基础扎实\n" +
                   "   - 建议加强应用题训练，提升解题思维\n\n" +
                   "2. 语文学科\n" +
                   "   - 古诗背诵流利度有提升\n" +
                   "   - 建议增加阅读理解训练\n\n" +
                   "3. 英语学科\n" +
                   "   - 单词记忆需要加强\n" +
                   "   - 建议每天15分钟单词复习\n\n" +
                   "4. 整体建议\n" +
                   "   - 保持当前学习节奏\n" +
                   "   - 重点关注英语词汇积累\n" +
                   "   - 适当奖励机制提升学习动力\n\n" +
                   "请老师在课堂上多关注孩子的英语学习情况。";
        } else {
            return "【AI 综合分析建议】\n\n" +
                   "根据家长提交的信息：\n\n" +
                   "1. 当前状况评估\n" +
                   "   - 整体发展状况良好\n" +
                   "   - 需要持续关注和引导\n\n" +
                   "2. 建议措施\n" +
                   "   - 家校配合，共同关注孩子成长\n" +
                   "   - 定期反馈，及时调整教育策略\n\n" +
                   "3. 后续跟进\n" +
                   "   - 建议2周后再次提交反馈\n" +
                   "   - 关注具体改善情况\n\n" +
                   "请老师结合在校表现，给予针对性指导。";
        }
    }

    private int estimateTokens(String text) {
        if (text == null) return 0;
        // Simple estimation: ~4 chars per token
        return text.length() / 4;
    }
}
