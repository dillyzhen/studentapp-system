package com.shlms.repository;

import com.shlms.entity.TeacherStudentAssignment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeacherStudentAssignmentRepository extends JpaRepository<TeacherStudentAssignment, String> {

    List<TeacherStudentAssignment> findByTeacherId(String teacherId);

    List<TeacherStudentAssignment> findByStudentId(String studentId);

    Optional<TeacherStudentAssignment> findByTeacherIdAndStudentId(String teacherId, String studentId);

    boolean existsByTeacherIdAndStudentId(String teacherId, String studentId);

    void deleteByTeacherIdAndStudentId(String teacherId, String studentId);
}
