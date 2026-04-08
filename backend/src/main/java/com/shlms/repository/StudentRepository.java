package com.shlms.repository;

import com.shlms.entity.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface StudentRepository extends JpaRepository<Student, String> {

    List<Student> findByClassId(String classId);

    List<Student> findByEnabledTrue();

    @Query("SELECT s FROM Student s JOIN TeacherStudentAssignment tsa ON s.id = tsa.student.id WHERE tsa.teacher.id = :teacherId AND s.enabled = true")
    List<Student> findByTeacherId(@Param("teacherId") String teacherId);

    @Query("SELECT s FROM Student s JOIN UserStudentBinding usb ON s.id = usb.student.id WHERE usb.user.id = :parentId AND s.enabled = true")
    List<Student> findByParentId(@Param("parentId") String parentId);

    boolean existsByStudentNo(String studentNo);
}
