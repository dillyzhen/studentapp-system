package com.shlms.enums;

public enum AuditActionType {
    // Auth
    LOGIN("用户登录"),
    LOGOUT("用户登出"),
    TOKEN_REFRESH("Token刷新"),

    // Parent operations
    SUBMISSION_CREATE("提交原始记录"),
    SUBMISSION_VIEW("查看提交记录"),

    // Teacher operations
    INTERPRETATION_START("开始AI解读"),
    INTERPRETATION_GENERATE("生成AI解读"),
    INTERPRETATION_EDIT("编辑AI建议"),
    REPORT_APPROVE("审核通过报告"),
    REPORT_REJECT("拒绝报告"),
    REPORT_DISTRIBUTE("分发报告"),

    // Parent report operations
    REPORT_VIEW("查看报告"),
    REPORT_DOWNLOAD("下载PDF报告"),

    // Admin operations
    USER_CREATE("创建用户"),
    USER_UPDATE("更新用户"),
    USER_DELETE("删除用户"),
    STUDENT_ASSIGN("分配学生"),
    STUDENT_UNASSIGN("取消分配"),

    // Data operations
    STUDENT_CREATE("创建学生"),
    STUDENT_UPDATE("更新学生"),
    STUDENT_DELETE("删除学生"),

    // System
    SYSTEM_CONFIG_UPDATE("更新系统配置");

    private final String displayName;

    AuditActionType(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
