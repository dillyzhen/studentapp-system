package com.shlms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ReportApproveRequest {

    @NotBlank(message = "审核意见不能为空")
    @Size(max = 500, message = "审核意见不能超过500字符")
    private String comment;
}
