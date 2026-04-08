package com.shlms.service;

import com.shlms.dto.RawRecordRequest;
import com.shlms.dto.RawRecordResponse;
import com.shlms.entity.RawRecord;
import com.shlms.entity.Student;
import com.shlms.entity.User;
import com.shlms.enums.RecordStatus;
import com.shlms.exception.ResourceNotFoundException;
import com.shlms.exception.UnauthorizedException;
import com.shlms.repository.RawRecordRepository;
import com.shlms.repository.StudentRepository;
import com.shlms.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class RawRecordService {

    private final RawRecordRepository rawRecordRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<RawRecordResponse> getByStudentId(String studentId) {
        return rawRecordRepository.findByStudentIdOrderBySubmittedAtDesc(studentId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<RawRecordResponse> getBySubmitterId(String submitterId) {
        return rawRecordRepository.findBySubmitterIdOrderBySubmittedAtDesc(submitterId)
                .stream()
                .map(this::convertToResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public RawRecordResponse getById(String id) {
        RawRecord record = rawRecordRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("记录不存在"));
        return convertToResponse(record);
    }

    @Transactional
    public RawRecordResponse create(RawRecordRequest request, String submitterId, String ipAddress, String userAgent) {
        Student student = studentRepository.findById(request.getStudentId())
                .orElseThrow(() -> new ResourceNotFoundException("学生不存在"));

        User submitter = userRepository.findById(submitterId)
                .orElseThrow(() -> new ResourceNotFoundException("提交者不存在"));

        RawRecord record = RawRecord.builder()
                .student(student)
                .submitter(submitter)
                .type(request.getType())
                .content(request.getContent())
                .images(request.getImages() != null ? String.join(",", request.getImages()) : null)
                .status(RecordStatus.PENDING)
                .ipAddress(ipAddress)
                .deviceFingerprint(generateDeviceFingerprint(userAgent))
                .build();

        // Generate digital signature for tamper-proofing
        String signature = generateDigitalSignature(record);
        record.setDigitalSignature(signature);

        RawRecord saved = rawRecordRepository.save(record);
        return convertToResponse(saved);
    }

    @Transactional
    public String uploadImage(String recordId, MultipartFile file) {
        // For now, return a mock URL. In production, this would upload to COS/MinIO
        String fileName = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
        String mockUrl = "/uploads/" + fileName;

        log.info("Image uploaded for record {}: {}", recordId, mockUrl);
        return mockUrl;
    }

    @Transactional(readOnly = true)
    public void verifyOwnership(String recordId, String userId) {
        RawRecord record = rawRecordRepository.findById(recordId)
                .orElseThrow(() -> new ResourceNotFoundException("记录不存在"));

        if (!record.getSubmitter().getId().equals(userId)) {
            throw new UnauthorizedException("无权访问该记录");
        }
    }

    private RawRecordResponse convertToResponse(RawRecord record) {
        return RawRecordResponse.builder()
                .id(record.getId())
                .studentId(record.getStudent().getId())
                .studentName(record.getStudent().getName())
                .type(record.getType())
                .typeDisplayName(record.getType().getDisplayName())
                .content(record.getContent())
                .images(record.getImages() != null ? Arrays.asList(record.getImages().split(",")) : null)
                .status(record.getStatus())
                .statusDisplayName(record.getStatus().getDisplayName())
                .digitalSignature(record.getDigitalSignature())
                .ipAddress(record.getIpAddress())
                .submittedAt(record.getSubmittedAt())
                .build();
    }

    private String generateDigitalSignature(RawRecord record) {
        try {
            String data = record.getStudent().getId() + ":" +
                         record.getSubmitter().getId() + ":" +
                         record.getContent() + ":" +
                         System.currentTimeMillis();

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash);
        } catch (Exception e) {
            log.error("Failed to generate digital signature", e);
            return "SIG_ERROR";
        }
    }

    private String generateDeviceFingerprint(String userAgent) {
        if (userAgent == null) return "UNKNOWN";
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(userAgent.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hash).substring(0, 16);
        } catch (Exception e) {
            return "FP_ERROR";
        }
    }
}
