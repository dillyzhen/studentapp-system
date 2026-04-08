package com.shlms.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.shlms.entity.Student;
import com.shlms.entity.TimelineEvent;
import com.shlms.repository.TimelineEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineEventRepository timelineEventRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    @SneakyThrows
    public void recordSubmissionReceived(Student student, String rawRecordId, String submitterId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("recordId", rawRecordId);
        eventData.put("submitterId", submitterId);

        TimelineEvent event = TimelineEvent.builder()
                .student(student)
                .eventType("SUBMISSION_RECEIVED")
                .eventTitle("收到家长提交记录")
                .eventData(objectMapper.writeValueAsString(eventData))
                .sourceType("RAW_RECORD")
                .sourceId(rawRecordId)
                .build();

        timelineEventRepository.save(event);
        log.info("Timeline: Recorded submission received for student {}", student.getId());
    }

    @Transactional
    @SneakyThrows
    public void recordInterpretationGenerated(Student student, String interpretationId, String teacherId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("interpretationId", interpretationId);
        eventData.put("teacherId", teacherId);

        TimelineEvent event = TimelineEvent.builder()
                .student(student)
                .eventType("INTERPRETATION_GENERATED")
                .eventTitle("AI 解读已生成")
                .eventData(objectMapper.writeValueAsString(eventData))
                .sourceType("AI_INTERPRETATION")
                .sourceId(interpretationId)
                .build();

        timelineEventRepository.save(event);
        log.info("Timeline: Recorded interpretation generated for student {}", student.getId());
    }

    @Transactional
    @SneakyThrows
    public void recordReportDistributed(Student student, String reportId, String teacherId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("reportId", reportId);
        eventData.put("teacherId", teacherId);

        TimelineEvent event = TimelineEvent.builder()
                .student(student)
                .eventType("REPORT_DISTRIBUTED")
                .eventTitle("建议报告已分发")
                .eventData(objectMapper.writeValueAsString(eventData))
                .sourceType("ADVICE_REPORT")
                .sourceId(reportId)
                .build();

        timelineEventRepository.save(event);
        log.info("Timeline: Recorded report distributed for student {}", student.getId());
    }

    @Transactional
    @SneakyThrows
    public void recordReportViewed(Student student, String reportId, String parentId) {
        Map<String, Object> eventData = new HashMap<>();
        eventData.put("reportId", reportId);
        eventData.put("parentId", parentId);

        TimelineEvent event = TimelineEvent.builder()
                .student(student)
                .eventType("REPORT_VIEWED")
                .eventTitle("家长查看报告")
                .eventData(objectMapper.writeValueAsString(eventData))
                .sourceType("ADVICE_REPORT")
                .sourceId(reportId)
                .build();

        timelineEventRepository.save(event);
        log.info("Timeline: Recorded report viewed for student {}", student.getId());
    }
}
