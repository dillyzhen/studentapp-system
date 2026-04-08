package com.shlms.repository;

import com.shlms.entity.AiInterpretation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AiInterpretationRepository extends JpaRepository<AiInterpretation, String> {

    List<AiInterpretation> findByRawRecordId(String rawRecordId);

    Optional<AiInterpretation> findBySessionId(String sessionId);

    List<AiInterpretation> findByTraceId(String traceId);
}
