package com.shlms.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class AiSessionManager {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    // In-memory fallback when Redis is not available
    private final Map<String, AiSession> memorySessions = new ConcurrentHashMap<>();

    private static final String SESSION_PREFIX = "ai:session:";
    private static final long SESSION_TTL_HOURS = 2;
    private boolean useRedis = true;

    @Data
    public static class AiSession {
        private String sessionId;
        private String studentId;
        private String teacherId;
        private String historySummary;
        private String contextSnapshot;
        private LocalDateTime createdAt;
        private LocalDateTime expiresAt;
    }

    public AiSession createSession(String studentId, String teacherId, String historySummary) {
        String sessionKey = generateSessionKey(studentId, teacherId);

        // Clear any existing session for this student-teacher pair
        clearExistingSession(studentId, teacherId);

        AiSession session = new AiSession();
        session.setSessionId(UUID.randomUUID().toString());
        session.setStudentId(studentId);
        session.setTeacherId(teacherId);
        session.setHistorySummary(historySummary);
        session.setContextSnapshot("");
        session.setCreatedAt(LocalDateTime.now());
        session.setExpiresAt(LocalDateTime.now().plusHours(SESSION_TTL_HOURS));

        if (useRedis) {
            try {
                String redisKey = SESSION_PREFIX + sessionKey;
                redisTemplate.opsForValue().set(redisKey, session, SESSION_TTL_HOURS, TimeUnit.HOURS);
            } catch (Exception e) {
                log.warn("Redis unavailable, using in-memory storage");
                useRedis = false;
                memorySessions.put(sessionKey, session);
            }
        } else {
            memorySessions.put(sessionKey, session);
        }

        log.info("Created AI session for student {} and teacher {}, sessionId: {}",
                studentId, teacherId, session.getSessionId());

        return session;
    }

    public AiSession getSession(String studentId, String teacherId) {
        String sessionKey = generateSessionKey(studentId, teacherId);

        if (useRedis) {
            try {
                String redisKey = SESSION_PREFIX + sessionKey;
                Object sessionObj = redisTemplate.opsForValue().get(redisKey);

                if (sessionObj == null) {
                    return null;
                }

                return objectMapper.convertValue(sessionObj, AiSession.class);
            } catch (Exception e) {
                log.warn("Redis unavailable, using in-memory storage");
                useRedis = false;
                return memorySessions.get(sessionKey);
            }
        }

        return memorySessions.get(sessionKey);
    }

    public void updateContext(String studentId, String teacherId, String newContext) {
        String sessionKey = generateSessionKey(studentId, teacherId);
        AiSession session = getSession(studentId, teacherId);

        if (session != null) {
            session.setContextSnapshot(newContext);

            if (useRedis) {
                try {
                    String redisKey = SESSION_PREFIX + sessionKey;
                    redisTemplate.opsForValue().set(redisKey, session, SESSION_TTL_HOURS, TimeUnit.HOURS);
                } catch (Exception e) {
                    log.warn("Redis unavailable, using in-memory storage");
                    useRedis = false;
                    memorySessions.put(sessionKey, session);
                }
            } else {
                memorySessions.put(sessionKey, session);
            }
        }
    }

    public void clearSession(String studentId, String teacherId) {
        String sessionKey = generateSessionKey(studentId, teacherId);

        if (useRedis) {
            try {
                String redisKey = SESSION_PREFIX + sessionKey;
                redisTemplate.delete(redisKey);
            } catch (Exception e) {
                log.warn("Redis unavailable");
            }
        }

        memorySessions.remove(sessionKey);
        log.info("Cleared AI session for student {} and teacher {}", studentId, teacherId);
    }

    public boolean isSessionValid(String studentId, String teacherId) {
        return getSession(studentId, teacherId) != null;
    }

    public void validateSessionIsolation(String studentId, String teacherId, String requestedStudentId) {
        AiSession session = getSession(studentId, teacherId);

        if (session != null && !session.getStudentId().equals(requestedStudentId)) {
            throw new RuntimeException("AI Session 隔离违规：当前 Session 绑定其他学生，请先切换 Session");
        }
    }

    private String generateSessionKey(String studentId, String teacherId) {
        return studentId + ":" + teacherId;
    }

    private void clearExistingSession(String studentId, String teacherId) {
        String sessionKey = generateSessionKey(studentId, teacherId);

        if (useRedis) {
            try {
                String redisKey = SESSION_PREFIX + sessionKey;
                redisTemplate.delete(redisKey);
            } catch (Exception e) {
                // Ignore
            }
        }

        memorySessions.remove(sessionKey);
    }

    public long getSessionRemainingTime(String studentId, String teacherId) {
        String sessionKey = generateSessionKey(studentId, teacherId);

        if (useRedis) {
            try {
                String redisKey = SESSION_PREFIX + sessionKey;
                Long expire = redisTemplate.getExpire(redisKey, TimeUnit.MINUTES);
                return expire != null ? expire : 0;
            } catch (Exception e) {
                // Fallback to memory check
            }
        }

        AiSession session = memorySessions.get(sessionKey);
        if (session != null && session.getExpiresAt() != null) {
            return java.time.Duration.between(LocalDateTime.now(), session.getExpiresAt()).toMinutes();
        }
        return 0;
    }
}
