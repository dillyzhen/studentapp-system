package com.shlms.ai;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.StringTokenizer;

@Slf4j
@Component
public class TokenCounter {

    @Value("${ai.token-limit.input-max:1500}")
    private int inputMaxTokens;

    @Value("${ai.token-limit.history-summary-max:500}")
    private int historySummaryMaxTokens;

    @Value("${ai.token-limit.current-content-max:1000}")
    private int currentContentMaxTokens;

    @Value("${ai.token-limit.output-max:2000}")
    private int outputMaxTokens;

    /**
     * Estimate token count using a simple approximation
     * English: ~4 characters per token
     * Chinese: ~1.5 characters per token (rough estimate)
     */
    public int estimateTokens(String text) {
        if (text == null || text.isEmpty()) {
            return 0;
        }

        int chineseCount = 0;
        int otherCount = 0;

        for (char c : text.toCharArray()) {
            if (isChinese(c)) {
                chineseCount++;
            } else {
                otherCount++;
            }
        }

        // Chinese: ~1 char per token, Others: ~4 chars per token
        return chineseCount + (otherCount / 4) + 1;
    }

    /**
     * Check if input exceeds the token limit
     */
    public TokenCheckResult checkInputTokens(String historySummary, String currentContent) {
        int historyTokens = estimateTokens(historySummary);
        int contentTokens = estimateTokens(currentContent);
        int totalTokens = historyTokens + contentTokens;

        TokenCheckResult result = new TokenCheckResult();
        result.setHistoryTokens(historyTokens);
        result.setContentTokens(contentTokens);
        result.setTotalTokens(totalTokens);
        result.setValid(totalTokens <= inputMaxTokens);
        result.setLimit(inputMaxTokens);

        if (!result.isValid()) {
            result.setErrorMessage(String.format(
                "输入Token数(%d)超过限制(%d)。历史摘要: %d, 当前内容: %d。请精简内容或归档历史数据。",
                totalTokens, inputMaxTokens, historyTokens, contentTokens
            ));
        }

        return result;
    }

    /**
     * Calculate estimated cost based on token usage
     * @param inputTokens Input token count
     * @param outputTokens Output token count
     * @param model Model name (claude-3-sonnet, gpt-4, etc.)
     * @return Estimated cost in USD
     */
    public double calculateCost(int inputTokens, int outputTokens, String model) {
        // Pricing per 1K tokens (approximate)
        double inputPricePer1K;
        double outputPricePer1K;

        if (model != null && model.contains("claude")) {
            // Claude 3 Sonnet pricing
            inputPricePer1K = 0.003;
            outputPricePer1K = 0.015;
        } else {
            // Default GPT-4 pricing
            inputPricePer1K = 0.03;
            outputPricePer1K = 0.06;
        }

        double inputCost = (inputTokens / 1000.0) * inputPricePer1K;
        double outputCost = (outputTokens / 1000.0) * outputPricePer1K;

        return inputCost + outputCost;
    }

    private boolean isChinese(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A
                || block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B
                || block == Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS
                || block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION;
    }

    public int getInputMaxTokens() {
        return inputMaxTokens;
    }

    public int getOutputMaxTokens() {
        return outputMaxTokens;
    }

    public static class TokenCheckResult {
        private int historyTokens;
        private int contentTokens;
        private int totalTokens;
        private boolean valid;
        private int limit;
        private String errorMessage;

        // Getters and setters
        public int getHistoryTokens() { return historyTokens; }
        public void setHistoryTokens(int historyTokens) { this.historyTokens = historyTokens; }
        public int getContentTokens() { return contentTokens; }
        public void setContentTokens(int contentTokens) { this.contentTokens = contentTokens; }
        public int getTotalTokens() { return totalTokens; }
        public void setTotalTokens(int totalTokens) { this.totalTokens = totalTokens; }
        public boolean isValid() { return valid; }
        public void setValid(boolean valid) { this.valid = valid; }
        public int getLimit() { return limit; }
        public void setLimit(int limit) { this.limit = limit; }
        public String getErrorMessage() { return errorMessage; }
        public void setErrorMessage(String errorMessage) { this.errorMessage = errorMessage; }
    }
}
