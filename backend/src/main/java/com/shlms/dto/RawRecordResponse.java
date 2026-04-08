package com.shlms.dto;

import com.shlms.enums.RecordStatus;
import com.shlms.enums.RecordType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RawRecordResponse {

    private String id;
    private String studentId;
    private String studentName;
    private RecordType type;
    private String typeDisplayName;
    private String content;
    private List<String> images;
    private RecordStatus status;
    private String statusDisplayName;
    private String digitalSignature;
    private String ipAddress;
    private LocalDateTime submittedAt;
}
