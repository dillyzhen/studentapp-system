package com.shlms.repository;

import com.shlms.entity.TimelineEvent;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TimelineEventRepository extends JpaRepository<TimelineEvent, String> {

    List<TimelineEvent> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<TimelineEvent> findByStudentIdOrderByCreatedAtDesc(String studentId, Pageable pageable);

    List<TimelineEvent> findByStudentIdAndEventTypeOrderByCreatedAtDesc(String studentId, String eventType);
}
