package com.shlms.repository;

import com.shlms.entity.AdviceReport;
import com.shlms.enums.ReportStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AdviceReportRepository extends JpaRepository<AdviceReport, String> {

    List<AdviceReport> findByStudentIdOrderByCreatedAtDesc(String studentId);

    List<AdviceReport> findByStatus(ReportStatus status);

    List<AdviceReport> findByStudentIdAndStatusOrderByCreatedAtDesc(String studentId, ReportStatus status);

    Optional<AdviceReport> findByInterpretationId(String interpretationId);

    @Query("SELECT ar FROM AdviceReport ar JOIN TeacherStudentAssignment tsa ON ar.student.id = tsa.student.id WHERE tsa.teacher.id = :teacherId AND ar.status = :status ORDER BY ar.createdAt DESC")
    List<AdviceReport> findByTeacherIdAndStatus(@Param("teacherId") String teacherId, @Param("status") ReportStatus status);

    long countByStudentId(String studentId);

    long countByStatus(ReportStatus status);
}
