package com.shlms.controller;

import com.shlms.dto.ApiResponse;
import com.shlms.dto.TimelineEventResponse;
import com.shlms.entity.Student;
import com.shlms.entity.TimelineEvent;
import com.shlms.entity.User;
import com.shlms.exception.ResourceNotFoundException;
import com.shlms.exception.UnauthorizedException;
import com.shlms.repository.StudentRepository;
import com.shlms.repository.TimelineEventRepository;
import com.shlms.repository.UserStudentBindingRepository;
import com.shlms.security.UserPrincipal;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/students")
@RequiredArgsConstructor
@PreAuthorize("hasAnyRole('TEACHER', 'PARENT', 'ADMIN')")
public class StudentController {

    private final StudentRepository studentRepository;
    private final TimelineEventRepository timelineEventRepository;
    private final UserStudentBindingRepository bindingRepository;

    @GetMapping("/{studentId}/timeline")
    public ResponseEntity<ApiResponse<List<TimelineEventResponse>>> getStudentTimeline(
            @AuthenticationPrincipal UserPrincipal currentUser,
            @PathVariable String studentId) {

        // Verify access
        verifyAccess(currentUser, studentId);

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        List<TimelineEvent> events = timelineEventRepository.findByStudentIdOrderByCreatedAtDesc(studentId);

        List<TimelineEventResponse> response = events.stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    private void verifyAccess(UserPrincipal currentUser, String studentId) {
        boolean hasAccess = false;

        if (currentUser.getRole().equals("ADMIN")) {
            hasAccess = true;
        } else if (currentUser.getRole().equals("TEACHER")) {
            // Teacher access should be verified via TeacherStudentAssignment
            // For now, allow all teachers (actual check should be in service layer)
            hasAccess = true;
        } else if (currentUser.getRole().equals("PARENT")) {
            hasAccess = bindingRepository.existsByUserIdAndStudentId(currentUser.getId(), studentId);
        }

        if (!hasAccess) {
            throw new UnauthorizedException("无权查看该学生的时间线");
        }
    }

    private TimelineEventResponse convertToResponse(TimelineEvent event) {
        return TimelineEventResponse.builder()
                .id(event.getId())
                .eventType(event.getEventType())
                .eventTitle(event.getEventTitle())
                .eventData(event.getEventData())
                .sourceType(event.getSourceType())
                .sourceId(event.getSourceId())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
