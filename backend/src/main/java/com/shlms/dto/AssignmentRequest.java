package com.shlms.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AssignmentRequest {

    @NotBlank(message = "老师ID不能为空")
    private String teacherId;

    @NotBlank(message = "学生ID不能为空")
    private String studentId;

    private String notes;
}
