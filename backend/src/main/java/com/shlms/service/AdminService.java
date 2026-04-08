package com.shlms.service;

import com.shlms.dto.*;
import com.shlms.entity.AiInterpretation;
import com.shlms.entity.Student;
import com.shlms.entity.TeacherStudentAssignment;
import com.shlms.entity.User;
import com.shlms.enums.AuditActionType;
import com.shlms.enums.UserRole;
import com.shlms.exception.ResourceNotFoundException;
import com.shlms.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AdminService {

    private final UserRepository userRepository;
    private final StudentRepository studentRepository;
    private final TeacherStudentAssignmentRepository assignmentRepository;
    private final RawRecordRepository rawRecordRepository;
    private final AdviceReportRepository adviceReportRepository;
    private final AiInterpretationRepository aiInterpretationRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(UserRole role) {
        List<User> users;
        if (role != null) {
            users = userRepository.findByRole(role);
        } else {
            users = userRepository.findAll();
        }
        return users.stream()
                .map(this::convertToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .name(request.getName())
                .role(request.getRole())
                .phone(request.getPhone())
                .email(request.getEmail())
                .enabled(true)
                .build();

        User saved = userRepository.save(user);

        auditLogService.log(AuditActionType.USER_CREATE, "admin", "ADMIN",
                "USER", saved.getId(), "Created user: " + request.getUsername());

        return convertToUserResponse(saved);
    }

    @Transactional
    public UserResponse updateUser(String userId, UpdateUserRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        if (request.getName() != null) {
            user.setName(request.getName());
        }
        if (request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }
        if (request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }
        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        User saved = userRepository.save(user);

        auditLogService.log(AuditActionType.USER_UPDATE, "admin", "ADMIN",
                "USER", userId, "Updated user: " + user.getUsername());

        return convertToUserResponse(saved);
    }

    @Transactional
    public void deleteUser(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("用户不存在"));

        userRepository.delete(user);

        auditLogService.log(AuditActionType.USER_DELETE, "admin", "ADMIN",
                "USER", userId, "Deleted user: " + user.getUsername());
    }

    @Transactional
    public void assignStudentToTeacher(String teacherId, String studentId, String notes) {
        User teacher = userRepository.findById(teacherId)
                .orElseThrow(() -> new ResourceNotFoundException("老师不存在"));

        if (teacher.getRole() != UserRole.TEACHER) {
            throw new RuntimeException("指定的用户不是老师");
        }

        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        if (assignmentRepository.existsByTeacherIdAndStudentId(teacherId, studentId)) {
            throw new RuntimeException("该学生已分配给此老师");
        }

        TeacherStudentAssignment assignment = TeacherStudentAssignment.builder()
                .teacher(teacher)
                .student(student)
                .notes(notes)
                .build();

        assignmentRepository.save(assignment);

        auditLogService.log(AuditActionType.STUDENT_ASSIGN, "admin", "ADMIN",
                "STUDENT", studentId, "Assigned to teacher: " + teacherId);
    }

    @Transactional
    public void unassignStudentFromTeacher(String teacherId, String studentId) {
        TeacherStudentAssignment assignment = assignmentRepository
                .findByTeacherIdAndStudentId(teacherId, studentId)
                .orElseThrow(() -> new ResourceNotFoundException("分配关系不存在"));

        assignmentRepository.delete(assignment);

        auditLogService.log(AuditActionType.STUDENT_UNASSIGN, "admin", "ADMIN",
                "STUDENT", studentId, "Unassigned from teacher: " + teacherId);
    }

    @Transactional(readOnly = true)
    public List<StudentBriefResponse> getTeacherStudents(String teacherId) {
        return assignmentRepository.findByTeacherId(teacherId)
                .stream()
                .map(assignment -> {
                    Student student = assignment.getStudent();
                    return StudentBriefResponse.builder()
                            .id(student.getId())
                            .name(student.getName())
                            .age(student.getAge())
                            .gender(student.getGender())
                            .genderDisplayName(student.getGender() != null ? student.getGender().getDisplayName() : null)
                            .studentNo(student.getStudentNo())
                            .className(student.getClassName())
                            .avatarUrl(student.getAvatarUrl())
                            .notes(assignment.getNotes())
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public DashboardStatsResponse getDashboardStats() {
        long totalUsers = userRepository.count();
        long totalTeachers = userRepository.findByRole(UserRole.TEACHER).size();
        long totalParents = userRepository.findByRole(UserRole.PARENT).size();
        long totalStudents = studentRepository.count();

        long totalSubmissions = rawRecordRepository.count();
        long pendingSubmissions = rawRecordRepository.findByStatus(com.shlms.enums.RecordStatus.PENDING).size();
        long completedSubmissions = rawRecordRepository.findByStatus(com.shlms.enums.RecordStatus.COMPLETED).size();

        long totalReports = adviceReportRepository.count();
        long draftReports = adviceReportRepository.findByStatus(com.shlms.enums.ReportStatus.DRAFT).size();
        long approvedReports = adviceReportRepository.findByStatus(com.shlms.enums.ReportStatus.APPROVED).size();
        long distributedReports = adviceReportRepository.findByStatus(com.shlms.enums.ReportStatus.DISTRIBUTED).size();

        long totalInterpretations = aiInterpretationRepository.count();
        double totalAiCost = aiInterpretationRepository.findAll()
                .stream()
                .mapToDouble(i -> i.getCostUsd() != null ? i.getCostUsd().doubleValue() : 0.0)
                .sum();
        long totalAiTokens = aiInterpretationRepository.findAll()
                .stream()
                .mapToLong(i -> (i.getInputTokens() != null ? i.getInputTokens() : 0)
                        + (i.getOutputTokens() != null ? i.getOutputTokens() : 0))
                .sum();

        return DashboardStatsResponse.builder()
                .totalUsers(totalUsers)
                .totalTeachers((long) totalTeachers)
                .totalParents((long) totalParents)
                .totalStudents(totalStudents)
                .totalSubmissions(totalSubmissions)
                .pendingSubmissions(pendingSubmissions)
                .completedSubmissions(completedSubmissions)
                .totalReports(totalReports)
                .draftReports(draftReports)
                .approvedReports(approvedReports)
                .distributedReports(distributedReports)
                .totalInterpretations(totalInterpretations)
                .totalAiCostUsd(totalAiCost)
                .totalAiTokens(totalAiTokens)
                .build();
    }

    @Transactional(readOnly = true)
    public AiCostStatsResponse getAiCostStats(String startDate, String endDate) {
        // Default to last 30 days if not specified
        LocalDate start = startDate != null ? LocalDate.parse(startDate) : LocalDate.now().minusDays(30);
        LocalDate end = endDate != null ? LocalDate.parse(endDate) : LocalDate.now();

        List<AiInterpretation> interpretations = aiInterpretationRepository.findAll();

        BigDecimal totalCost = interpretations.stream()
                .map(i -> i.getCostUsd() != null ? i.getCostUsd() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        long totalTokens = interpretations.stream()
                .mapToLong(i -> (i.getInputTokens() != null ? i.getInputTokens() : 0)
                        + (i.getOutputTokens() != null ? i.getOutputTokens() : 0))
                .sum();

        BigDecimal avgCost = interpretations.isEmpty() ? BigDecimal.ZERO
                : totalCost.divide(BigDecimal.valueOf(interpretations.size()), 6, RoundingMode.HALF_UP);

        return AiCostStatsResponse.builder()
                .startDate(start.toString())
                .endDate(end.toString())
                .totalCostUsd(totalCost)
                .totalTokens(totalTokens)
                .totalInterpretations((long) interpretations.size())
                .averageCostPerInterpretation(avgCost)
                .teacherCosts(List.of()) // TODO: Implement per-teacher stats
                .build();
    }

    private UserResponse convertToUserResponse(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .name(user.getName())
                .role(user.getRole())
                .roleDisplayName(user.getRole() != null ? user.getRole().getDisplayName() : null)
                .phone(user.getPhone())
                .email(user.getEmail())
                .enabled(user.getEnabled())
                .lastLoginAt(user.getLastLoginAt())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
