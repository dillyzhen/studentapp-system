package com.shlms.dto;

import com.shlms.enums.RecordType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class RawRecordRequest {

    @NotBlank(message = "学生ID不能为空")
    private String studentId;

    @NotNull(message = "记录类型不能为空")
    private RecordType type;

    @NotBlank(message = "内容不能为空")
    @Size(max = 4000, message = "内容不能超过4000字符")
    private String content;

    private List<String> images;
}
