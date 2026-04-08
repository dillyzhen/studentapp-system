package com.shlms.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "ai_interpretations")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class AiInterpretation {

    @Id
    @Column(name = "id", nullable = false, updatable = false)
    private String id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "raw_record_id", nullable = false)
    private RawRecord rawRecord;

    @Column(name = "session_id", nullable = false, length = 100)
    private String sessionId;

    @Column(name = "system_prompt", length = 4000)
    private String systemPrompt;

    @Column(name = "user_prompt", length = 4000)
    private String userPrompt;

    @Column(name = "raw_response", length = 8000)
    private String rawResponse;

    @Column(name = "interpretation_content", length = 4000)
    private String interpretationContent;

    @Column(name = "input_tokens")
    private Integer inputTokens;

    @Column(name = "output_tokens")
    private Integer outputTokens;

    @Column(name = "cost_usd", precision = 10, scale = 6)
    private java.math.BigDecimal costUsd;

    @Column(name = "model_version", length = 50)
    private String modelVersion;

    @Column(name = "trace_id", length = 100)
    private String traceId;

    @Column(name = "duration_ms")
    private Long durationMs;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void prePersist() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
    }
}
