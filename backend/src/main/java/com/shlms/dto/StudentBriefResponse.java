package com.shlms.dto;

import com.shlms.enums.Gender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StudentBriefResponse {

    private String id;
    private String name;
    private Integer age;
    private Gender gender;
    private String genderDisplayName;
    private String studentNo;
    private String className;
    private String avatarUrl;
    private String relationship;
    private Long submissionCount;
    private String notes;
}
