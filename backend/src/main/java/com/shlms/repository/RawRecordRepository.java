package com.shlms.repository;

import com.shlms.entity.RawRecord;
import com.shlms.enums.RecordStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawRecordRepository extends JpaRepository<RawRecord, String> {

    List<RawRecord> findByStudentIdOrderBySubmittedAtDesc(String studentId);

    List<RawRecord> findBySubmitterIdOrderBySubmittedAtDesc(String submitterId);

    List<RawRecord> findByStatus(RecordStatus status);

    @Query("SELECT rr FROM RawRecord rr JOIN TeacherStudentAssignment tsa ON rr.student.id = tsa.student.id WHERE tsa.teacher.id = :teacherId AND rr.status = :status ORDER BY rr.submittedAt DESC")
    List<RawRecord> findPendingByTeacherId(@Param("teacherId") String teacherId, @Param("status") RecordStatus status);

    long countByStudentId(String studentId);
}
