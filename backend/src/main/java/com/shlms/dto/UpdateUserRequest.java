package com.shlms.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserRequest {

    @Size(max = 50, message = "姓名不能超过50字符")
    private String name;

    private String phone;

    private String email;

    private Boolean enabled;
}
