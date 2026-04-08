package com.shlms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class InterpretationEditRequest {

    @NotBlank(message = "内容不能为空")
    @Size(max = 4000, message = "内容不能超过4000字符")
    private String content;

    @Size(max = 500, message = "编辑原因不能超过500字符")
    private String editReason;
}
