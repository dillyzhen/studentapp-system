package com.shlms.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TimelineEventResponse {

    private String id;
    private String eventType;
    private String eventTitle;
    private String eventData;
    private String sourceType;
    private String sourceId;
    private LocalDateTime createdAt;
}
