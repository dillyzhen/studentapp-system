package com.shlms.repository;

import com.shlms.entity.DiffRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiffRecordRepository extends JpaRepository<DiffRecord, String> {

    List<DiffRecord> findByInterpretationIdOrderByEditedAtDesc(String interpretationId);

    List<DiffRecord> findByEditorIdOrderByEditedAtDesc(String editorId);
}
