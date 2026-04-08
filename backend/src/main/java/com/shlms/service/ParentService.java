package com.shlms.service;

import com.shlms.dto.StudentBriefResponse;
import com.shlms.entity.Student;
import com.shlms.entity.UserStudentBinding;
import com.shlms.repository.RawRecordRepository;
import com.shlms.repository.UserStudentBindingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParentService {

    private final UserStudentBindingRepository bindingRepository;
    private final RawRecordRepository rawRecordRepository;

    @Transactional(readOnly = true)
    public List<StudentBriefResponse> getMyStudents(String parentId) {
        List<UserStudentBinding> bindings = bindingRepository.findByUserId(parentId);

        return bindings.stream()
                .map(binding -> {
                    Student student = binding.getStudent();
                    Long submissionCount = rawRecordRepository.countByStudentId(student.getId());

                    return StudentBriefResponse.builder()
                            .id(student.getId())
                            .name(student.getName())
                            .age(student.getAge())
                            .gender(student.getGender())
                            .genderDisplayName(student.getGender() != null ? student.getGender().getDisplayName() : null)
                            .studentNo(student.getStudentNo())
                            .className(student.getClassName())
                            .avatarUrl(student.getAvatarUrl())
                            .relationship(binding.getRelationship())
                            .submissionCount(submissionCount)
                            .build();
                })
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public void verifyParentStudentAccess(String parentId, String studentId) {
        boolean hasAccess = bindingRepository.existsByUserIdAndStudentId(parentId, studentId);
        if (!hasAccess) {
            throw new RuntimeException("无权访问该学生");
        }
    }
}
