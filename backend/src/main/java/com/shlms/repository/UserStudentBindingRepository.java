package com.shlms.repository;

import com.shlms.entity.UserStudentBinding;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserStudentBindingRepository extends JpaRepository<UserStudentBinding, String> {

    List<UserStudentBinding> findByUserId(String userId);

    List<UserStudentBinding> findByStudentId(String studentId);

    Optional<UserStudentBinding> findByUserIdAndStudentId(String userId, String studentId);

    boolean existsByUserIdAndStudentId(String userId, String studentId);

    long countByStudentId(String studentId);
}
