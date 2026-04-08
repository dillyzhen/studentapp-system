package com.shlms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InterpretationResponse {

    private String sessionId;
    private String studentId;
    private String studentName;
    private String rawRecordId;
    private String rawContent;
    private String historySummary;
    private long sessionRemainingMinutes;
}
